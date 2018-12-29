# this program requires the 32 bit version of Python!!

import os
import glob
import math
import subprocess
import re
import sys
import string
from decimal import Decimal
from astropy.io import fits
from astropy.wcs import WCS
import matplotlib.pyplot as plt
import numpy as np
import numpy.ma as ma
from scipy.ndimage import median_filter
#from pyds9 import DS9
import argparse
import pandas as pd
import ch  # custom callHorizons library
import dateutil
from datetime import datetime
from datetime import timedelta
from astropy.wcs import WCS
from astropy.coordinates import SkyCoord
import pandas as pd
from astropy.time import Time
import shutil

#
# START SETTINGS
# MODIFY THESE FIELDS AS NEEDED!
#
# input path *with* ending forward slash
input_path = './'
# output path *with* ending forward slash
sex_output_path = './firstlook/'
# bad path
bad_path = './bad/'
# suffix for output files, if any...
sex_output_suffix = '.sex'
# log file name
log_fname = './log.firstlook.txt'
# path to sextractor executable and config files (incl. the filenames!)
sextractor_bin_fname = os.path.dirname(
    os.path.realpath(__file__)) + '\\' + 'sextractor.exe'
sextractor_cfg_fname = os.path.dirname(
    os.path.realpath(__file__)) + '\\' + 'sexcurve.sex'
sextractor_param_fname = os.path.dirname(
    os.path.realpath(__file__)) + '\\' + 'sexcurve.param'
sextractor_filter_fname = os.path.dirname(
    os.path.realpath(__file__)) + '\\' + 'sexcurve.conv'
# tolerance for object matching
dRa = 0.00062
dDec = 0.00062
# target/comp list
comps_fname = './comps.in.txt'
targets_out_fname = './targets.out.csv'
counts_out_fname = './counts.out.csv'
# mask file that identifies bad pixels
bad_pixels_fname = './bad_pixels.txt'
cleaned_output_path = './cor/'
# observatory code
obs_code = 'G52'
# panstarrs
# panstarrs ref magnitude
pso_ref_mag = 'rPSFMag'
# panstarrs max magnitude
pso_max_mag = 16
# panstarrs min magnitude
pso_min_mag = 0

#
# END SETTINGS
#

# logger


def logme(str):
    log.write(str + "\n")
    print str
    return


def exit():
    logme('Program execution halted.')
    log.close()
    os.sys.exit(1)

# run external process


def runSubprocess(command_array):
    # command array is array with command and all required parameters
    try:
        with open(os.devnull, 'w') as fp:
            sp = subprocess.Popen(command_array, stderr=fp, stdout=fp)
        # logme('Running subprocess ("%s" %s)...'%(' '.join(command_array), sp.pid))
        sp.wait()
        output, error = sp.communicate()
        return (output, error, sp.pid)
    except:
        logme('Error. Subprocess ("%s" %d) failed.' %
              (' '.join(command_array), sp.pid))
        return ('', '', 0)

# get current ra/dec of target asteroid


def getAsteroidRaDec(name, dt):
    ra = ''
    dec = ''
    start = dt
    end = dt + timedelta(minutes=1)
    # get ephemerides for target in JPL Horizons from start to end times
    result = ch.query(name.upper(), smallbody=True)
    result.set_epochrange(start.isoformat(), end.isoformat(), '1m')
    result.get_ephemerides(obs_code)
    if result and len(result['EL']):
        ra = result['RA'][0]
        dec = result['DEC'][0]
    else:
        logme('Error. Asteroid (%s) not found for %s.' %
              (name, start.isoformat()))
        exit()
    return (ra, dec)


def jdToYYMMDD_HHMMSS(jd):
    t = Time(jd, format='mjd', scale='utc')
    return t.iso


# open log file
log = open(log_fname, 'a+')

# set up the command line argument parser
parser = argparse.ArgumentParser(
    description='Perform lightcurve photometry using sextractor.')
# parser.add_argument('asteroid', metavar='asteroid#', type=int,
#                    help='Target asteroid number')
args = parser.parse_args()

# make sure input files and folder exist
inputs = [input_path, sextractor_bin_fname, sextractor_cfg_fname,
          sextractor_param_fname,  sextractor_filter_fname, comps_fname]
for input in inputs:
    if not os.path.exists(input_path):
        logme('Error. The file or path (%s) does not exist.' % input)
        exit()

# does output directory exist? If not, create it...
outputs = [sex_output_path, cleaned_output_path, bad_path]
for output in outputs:
    try:
        os.mkdir(output)
    except:
        pass

image_data = []
# get a list of all FITS files in the input directory
fits_files = glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
# loop through all qualifying files and perform sextraction
for fits_file in sorted(fits_files):
    fits_data = fits.open(fits_file)
    header = fits_data[0].header
    wcs = WCS(header)
    airmass = header['AIRMASS']
    try:
        dt_obs = dateutil.parser.parse(header['DATE-OBS'])
    except:
        logme('Error. Invalid observation date found in %s.' % fits_file)
        exit()
    try:
        naxis1 = header['NAXIS1']
        naxis2 = header['NAXIS2']
    except:
        logme('Error. Invalid CCD pixel size found in %s.' % fits_file)
        exit()
    try:
        ra = header['CRVAL1']
        dec = header['CRVAL2']
    except:
        logme('Error. Invalid RA/DEC found in %s.' % fits_file)
        exit()
    try:
        JD = header['MJD-OBS']
    except KeyError:
        JD = header['JD']
    # calculate image corners in ra/dec
    ra1, dec1 = wcs.all_pix2world(0, 0, 0)
    ra2, dec2 = wcs.all_pix2world(naxis1, naxis2, 0)
    # calculate search radius in degrees from the center!
    c1 = SkyCoord(ra1, dec1, unit="deg")
    c2 = SkyCoord(ra2, dec2, unit="deg")
    # estimate radius of FOV in arcmin
    r_arcmin = '%f' % (c1.separation(c2).deg*60/2)
    logme("Sextracting %s" % (fits_file))
    output_file = sex_output_path + \
        fits_file.replace('\\', '/').rsplit('/', 1)[1]
    output_file = '%s%s.txt' % (output_file, sex_output_suffix)
    # add input filename, output filename, airmass, and jd to sex_file list
    image_data.append(
        {'image': fits_file, 'sex': output_file, 'jd': JD, 'airmass': airmass, 'ra': ra, 'dec': dec, 'dt_obs': dt_obs, 'r_arcmin': r_arcmin})
    # sextract this file
    (output, error, id) = runSubprocess([sextractor_bin_fname, fits_file, '-c', sextractor_cfg_fname, '-catalog_name',
                                         output_file, '-parameters_name', sextractor_param_fname, '-filter_name', sextractor_filter_fname])
    if error:
        logme('Error. Sextractor failed: %s' % output)
        exit()
logme('Sextracted %d files.' % len(image_data))

# build list of comparison stars in comps_fname using
# PanSTARRS Stack Object Catalog Search
logme('Searching for comparison stars in the PANSTARRS catalog (ra=%s deg, dec=%s deg, radius=%s min)...' %
      (image_data[0]['ra'], image_data[0]['dec'], image_data[0]['r_arcmin']))
pso_url_base = 'http://archive.stsci.edu/panstarrs/stackobject/search.php'
pso_url_parms = '?resolver=Resolve&radius=%s&ra=%s&dec=%s&equinox=J2000&nDetections=&selectedColumnsCsv=objname%%2Cobjid%%2Cramean%%2Cdecmean%%2Cgpsfmag%%2Crpsfmag%%2Cipsfmag' + \
    '&coordformat=dec&outputformat=CSV_file&skipformat=on' + \
    '&max_records=50001&action=Search'
url = pso_url_base + \
    pso_url_parms % (image_data[0]['r_arcmin'], image_data[0]['ra'], image_data[0]
                     ['dec'])
# get the results of the REST query
comps = pd.read_csv(url)
if len(comps) <= 0:
    logme('Error. No comparison stars found!')
    exit()
# remove dupes, keep first
comps.drop_duplicates(subset=['objName'], keep='first', inplace=True)
# make sure magnitudes are treated as floats
comps[pso_ref_mag] = pd.to_numeric(comps[pso_ref_mag], errors='coerce')
# remove spaces from obj names
comps['objName'] = comps['objName'].str.replace('PSO ', '')
# filter based on ref (r?) magnitude!
comps = comps.query("%s > %f & %s < %f" %
                    (pso_ref_mag, pso_min_mag, pso_ref_mag, pso_max_mag))
if len(comps) <= 0:
    logme('Error. No comparison stars meet the criteria (%s > %f & %s < %f)!' %
          (pso_ref_mag, pso_min_mag, pso_ref_mag, pso_max_mag))
    exit()
logme('A total of %d comparison star(s) met the criteria (%s > %f & %s < %f)!' %
      (len(comps), pso_ref_mag, pso_min_mag, pso_ref_mag, pso_max_mag))
# output objects to comps_fname in sextract input format
comps_for_sex = comps[['raMean', 'decMean', 'objName']]
comps_for_sex.to_csv(comps_fname, sep=' ', index=False, header=False)

# read ra/dec from target/comp stars list
# this is legacy and duplicative, but we will go with it
object_data = []
sfile = file('%s' % comps_fname, 'rt')
lines = [s for s in sfile if len(s) > 2 and s[0] != '#']
sfile.close()
count = 0
target_index = -1
for index, l in enumerate(lines):
    spl = l.split()
    ra = float(spl[0])
    dec = float(spl[1])
    name = spl[2]
    object_data.append(
        {'index': index, 'ra': ra, 'dec': dec, 'object_name': name, 'found': True})
# add the asteroid to the object list
# we don't know the ra/dec yet until we get the date/time from the FITS file
#target_index = index + 1
# object_data.append({'index': target_index, 'ra': '',
#                    'dec': '', 'object_name': '%d' % args.asteroid, 'found': True})

logme('Searching for %d objects in sextracted data.' % len(object_data))
ofile = file(counts_out_fname, 'wt')
# look for target/comp matches in sextracted files
counts = []
images = []
for image in image_data:
    num_found = 0
    lines = [s for s in file(image['sex'], 'rt') if len(s) > 2]
    # unless object is target, stop looking for it if it was not found in one of the images
    for s in (x for x in object_data):
        found = False
        # assign the asteroid ra/dec
        # if s['object_name'] == '%d' % args.asteroid:
        #    # get ra/dec of asteroid at the time image was taken
        #    (s['ra'], s['dec']) = getAsteroidRaDec(
        #        s['object_name'], image['dt_obs'])
        for l in lines:
            spl = l.split()
            ra = float(spl[0])
            dec = float(spl[1])
            if abs(ra-s['ra']) < dRa and abs(dec-s['dec']) < dDec:
                num_found += 1
                break
    images.append(image['image'])
    counts.append(num_found)
    ofile.write('%s,%d\n' % (image['sex'], num_found))
ofile.close()

mode = np.bincount(counts).argmax()
std = np.array(counts).std()
mask = np.array(counts) >= mode - std
logme('A total of %d stars were for found in %d (of %d) images.' %
      (mode, len(np.array(images)[mask]), len(images)))
mask = np.array(counts) < mode - std
bad_images = np.array(images)[mask]
for image in bad_images:
    head, tail = os.path.split(image)
    shutil.move(image, '%s%s' % (bad_path, tail))
logme('A total of %d images were moved to %s.' %
      (len(bad_images), bad_path))
