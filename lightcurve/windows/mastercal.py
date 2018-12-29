#
# mastercal.py
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
from decimal import Decimal
from astropy.io import fits
from astropy import wcs

from astropy import log
log.setLevel('ERROR')

from astropy import units as u
import ccdproc

import numpy as np

from collections import defaultdict


def logme(str):
    log.write(str + "\n")
    print str
    return


# MODIFY THESE FIELDS AS NEEDED!
# input path *with* ending forward slash
input_path = './'
# output path *with* ending forward slash
output_path = './'
# log file name
log_fname = 'log.mastercal.txt'
# suffix for output files, if any...
output_suffix = '.calibrated'

# used in master calibration filenames
date_suffix = datetime.datetime.now().strftime('%Y%m%d.%H%M%S')

# master bias frame
# folder with bias component frames *including* ending forward slash
bias_path = './bias/'

# master dark frame
# folder with dark component frames *including* ending forward slash
dark_path = './dark/'
dark_is_bias_corrected = False
dark_bias = None

# master flat frame
# folder with bias component frames *including* ending forward slash
flat_path = './flat/'
flat_is_bias_corrected = False
flat_bias = None
flat_is_dark_corrected = False
flat_dark = None
flat_ave_exptime = 0

# name of exposure variable in FITS header file
exposure_label = 'EXPTIME'
image_type_label = 'IMAGETYP'
filter_label = 'FILTER'
bin_label = 'XBIN'

# mean count for flats
mean_flat_count = 20000

log = open(log_fname, 'a+')

# does output directory exist? If not, create it
try:
    os.mkdir(output_path)
except:
    pass

# identify cal frames
filter = ''
type = ''
exposure = 0
cal = defaultdict(dict)
im = glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
#print im
if(len(im) <= 0):
    logme('Error. No calibration frame(s) found (%s).' % input_path)
    log.close()
    sys.exit(-1)
for i in range(0, len(im)):
    try:
        fits_file = fits.open('%s' % im[i])
        fits_file.close()
        header = fits_file[0].header
    except:
        logme('Error. Could not open FITS file (%s).' % im[i])
        continue
    try:
        filter = header[filter_label]
        type = header[image_type_label]
        exposure = header[exposure_label]
        bin = header[bin_label]
    except:
        logme('Error. Invalid FITS header (%s).' % im[i])
        continue
    if(type.strip().lower() == 'dark'):
        exp = int(exposure)*10
        # for SEO, bias frames are darks with 0.1s or less exposure
        if(exp <= 1):
            #logme('BIAS FRAME')
            if not cal.get('bias', {}).has_key(bin):
                cal['bias'][bin] = []
            cal['bias'][bin].append(im[i])
        else:
            #logme('DARK FRAME')
            if not cal.get('dark', {}).has_key(bin):
                cal['dark'][bin] = {}
            if not cal['dark'].get(bin, {}).has_key(int(exposure)):
                cal['dark'][bin][int(exposure)] = []
            cal['dark'][bin][int(exposure)].append(im[i])
    elif(type.strip().lower() == 'bias'):
        #logme('BIAS FRAME')
        if not cal.get('bias', {}).has_key(bin):
            cal['bias'][bin] = []
        cal['bias'][bin].append(im[i])
    # object type is an error in the old flatmatt or fatmmat script
    elif(type.strip().lower() == 'flat' or type.strip().lower() == 'object'):
        #logme('FLAT FRAME')
        if not cal.get('flat', {}).has_key(bin):
            cal['flat'][bin] = {}
        if not cal['flat'].get(bin, {}).has_key(filter):
            cal['flat'][bin][filter] = []
        cal['flat'][bin][filter].append(im[i])
    else:
        logme('Error. Unknown calibration image type (%s).' % type)
        continue

# create bias master frames
logme('Creating master bias frame(s)...')
for bin in cal['bias']:
    bias_master = '%smbias.bin%d.fits' % (output_path, bin)
    biases = None
    logme('Combining %s bias frames for bin=%d...' %
          (len(cal['bias'][bin]), bin))
    for fits in cal['bias'][bin]:
        if(biases):
            biases += ','+fits
        else:
            biases = fits
    # if there is just one, make it two of the same for the combine!
    if (len(cal['bias'][bin]) == 1):
        biases += ','+fits
    # use average to avoid memory issue with argsort and 'median'
    bias = ccdproc.combine(biases, method='average',
                           unit='adu', add_keyword=False)
    # write master frame to file
    hdulist = bias.to_hdu()
    header = hdulist[0].header
    header[image_type_label] = 'BIAS    '
    hdulist.writeto(bias_master, clobber=True)
    logme('Created master bias frame (%s) from %d bias frames.' %
          (bias_master, len(cal['bias'][bin])))

# create dark frames
logme('Creating master dark frame(s)...')
for bin in cal['dark']:
    for exp in cal['dark'][bin]:
        dark_master = '%smdark.bin%d.exp%ds.fits' % (
            output_path, bin, exp)
        darks = None
        for fits in cal['dark'][bin][exp]:
            if(darks):
                darks += ','+fits
            else:
                darks = fits
        # if there is just one, make it two of the same for the combine!
        if (len(cal['dark'][bin][exp]) == 1):
            darks += ','+fits
        #print darks
        dark = ccdproc.combine(
            darks, method='median', unit='adu', add_keyword=False, **{'verify': 'ignore'})
        # write master dark frame
        hdulist = dark.to_hdu()
        # add bias correction to header
        header = hdulist[0].header
        header[image_type_label] = 'DARK    '
        hdulist.writeto(dark_master, clobber=True)
        logme('Created master dark frame (%s) from %d dark frames.' %
              (dark_master, len(cal['dark'][bin][exp])))

# create flat frames
logme('Creating master flat frame(s)...')
for bin in cal['flat']:
    for filt in cal['flat'][bin]:
        flat_master = '%smflat.bin%d.%s.fits' % (
            output_path, bin, filt)
        flats = None
        for fits in cal['flat'][bin][filt]:
            if(flats):
                flats += ','+fits
            else:
                flats = fits
        # if there is just one, make it two of the same for the combine!
        if (len(cal['flat'][bin][filt]) == 1):
            flats += ','+fits
        # scale the flat component frames to have the same mean value, 10000.0

        def scaling_func(arr): return mean_flat_count/np.ma.median(arr)
        # combine them
        flat = ccdproc.combine(
            flats, method='median', scale=scaling_func, unit='adu', add_keyword=False)
        # write master flat frame
        hdulist = flat.to_hdu()
        # add bias correction to header
        header = hdulist[0].header
        header[image_type_label] = 'FLAT    '
        hdulist.writeto(flat_master, clobber=True)
        logme('Created master flat frame (%s) from %d flat frames.' %
              (flat_master, len(cal['flat'][bin][filt])))

log.close()
