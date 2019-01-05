#
# calibrate.py
#
# calibrate fits images using darks, flats, and bias frames
# corrected image = (image - bias - k(dark-bias))/flat
# for k=1, i.e. image exp = dark exp, corrected image = (image - dark)/flat

import os
import glob
import math
import subprocess
import re
import sys
import datetime
import shutil
import os.path
from decimal import Decimal
from astropy.io import fits
from astropy import wcs

from astropy import log
log.setLevel('ERROR')

from astropy import units as u
import ccdproc

import numpy as np


def logme(str):
    log.write(str + "\n")
    print str
    return


# MODIFY THESE FIELDS AS NEEDED!
# input path *with* ending forward slash
input_path = './'
# output path *with* ending forward slash
output_path = './bdf/'
# log file name
log_fname = 'log.calibrate.txt'
# suffix for output files, if any...
output_suffix = '.cal'

# used in master calibration filenames
date_suffix = datetime.datetime.now().strftime('%Y%m%d.%H%M%S')

# name of exposure variable in FITS header file
exposure_label = 'EXPTIME'
image_type_label = 'IMAGETYP'
filter_label = 'FILTER'
bin_label = 'XBIN'

# folder where all calibration files are located
cal_path = './cal/'

# master bias frame
# folder with bias component frames *including* ending forward slash
bias_path = cal_path

# master dark frame
# folder with dark component frames *including* ending forward slash
dark_path = cal_path
dark_is_bias_corrected = False
dark_bias = None

# master flat frame
# folder with bias component frames *including* ending forward slash
flat_path = cal_path
flat_is_bias_corrected = False
flat_bias = None
flat_is_dark_corrected = False
flat_dark = None
flat_ave_exptime = 0

# name of exposure variable in FITS header file
exposure_label = 'EXPTIME'

log = open(log_fname, 'a+')

# does output directory exist? If not, create it
try:
    os.mkdir(output_path)
except:
    pass

# get a list of all FITS files in the input directory
fits_files = glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
# loop through all qualifying files and perform plate-solving
logme('Calibrating images in %s' % input_path)
for fits_file in fits_files:
    # dos fix
    fits_file = fits_file.replace('\\', '/')
    # what do we need to calibrate this file?
    # open file
    try:
        f = fits.open('%s' % fits_file)
        f.close()
        header = f[0].header
    except:
        logme('Error. Could not open FITS file (%s).' % fits_file)
        raise
    # get binning, filter, and exposure
    try:
        filter = header[filter_label]
        exposure = header[exposure_label]
        bin = header[bin_label]
    except:
        logme('Error. Invalid FITS header (%s).' % fits_file)
        raise

    # open the master bias frame
    bias_fname = 'mbias.bin%s.fits' % bin
    try:
        bias = ccdproc.CCDData.read(cal_path+bias_fname, unit='adu')
    except:
        logme('Error. Could not open master bias file (%s).' %
              cal_path+bias_fname)
        raise

    # open the master flat frame
    flat_fname = 'mflat.bin%s.%s.fits' % (bin, filter)
    try:
        flat = ccdproc.CCDData.read(cal_path+flat_fname, unit='adu')
    except:
        logme('Error. Could not open master flat file (%s).' %
              cal_path+flat_fname)
        raise

    # open the master dark frame with the *longest* exposure
    master_dark_files = glob.glob(cal_path+'mdark.bin%d.*.fits' % bin)
    max_exposure = 0
    for master_dark_file in master_dark_files:
        match = re.search(
            'mdark.bin%d.exp([0-9]+)s.fits' % bin, master_dark_file)
        if match:
            exposure = float(match.group(1).strip())
            if exposure > max_exposure:
                max_exposure = exposure
    if max_exposure == 0:
        logme('Error. Could not find a master dark frame (bin=%s) in %s!' %
              (cal_path, bin))
        raise
    dark_fname = 'mdark.bin%d.exp%ds.fits' % (bin, int(max_exposure))
    try:
        dark = ccdproc.CCDData.read(cal_path+dark_fname, unit='adu')
    except:
        logme('Error. Could not open master dark file (%s).' %
              cal_path+dark_fname)
        raise

    # subtract bias from dark
    #logme('Subtracting master bias frame from master dark frame...')
    dark = ccdproc.subtract_bias(dark, bias, add_keyword=False)

    # subtrack bias from flat
    #logme('Subtracting master bias frame from flat frame...')
    flat = ccdproc.subtract_bias(flat, bias, add_keyword=False)

    # subtract dark from flat
    #logme('Subtracting master dark frame from flat frame...')
    flat = ccdproc.subtract_dark(
        flat, dark, scale=True, exposure_time=exposure_label, exposure_unit=u.second, add_keyword=False)

    # subtract bias from light, dark, and flat frames
    # open image
    image = ccdproc.CCDData.read(fits_file, unit='adu', relax=True)
    image = ccdproc.subtract_bias(image, bias, add_keyword=False)
    image = ccdproc.subtract_dark(
        image, dark, scale=True, exposure_time=exposure_label, exposure_unit=u.second, add_keyword=False)
    image = ccdproc.flat_correct(image, flat, add_keyword=False)
    # save calibrated image
    output_file = "%s" % (fits_file.rsplit('.', 1)[0])+output_suffix+".fits"
    output_file = output_file.rsplit('/', 1)[1]
    output_file = output_path+output_file
    # scale calibrated image back to int32, some FITS programs don't like float
    hdulist = image.to_hdu()
    hdulist[0].scale('int32', bzero=1)
    hdulist[0].header['BIASCORR'] = bias_fname
    hdulist[0].header['DARKCORR'] = dark_fname
    hdulist[0].header['FLATCORR'] = flat_fname
    hdulist.writeto(output_file, clobber=True)

logme('Calibrated %d images and saved to %s.' % (len(fits_files), output_path))
log.close()
