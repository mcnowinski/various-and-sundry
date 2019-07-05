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
from pyds9 import DS9
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
import struct
import logging

# logging
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(levelname)s - %(name)s - %(funcName)s - %(message)s',
    handlers=[
        logging.FileHandler("sexerizer.log"),
        logging.StreamHandler()
    ])
logger = logging.getLogger('sexerizer')

#
# START SETTINGS
# MODIFY THESE FIELDS AS NEEDED!
#
# input path *with* ending forward slash
input_path = './'
# output path *with* ending forward slash
sex_output_path = './sexerizer/'
# suffix for output files, if any...
sex_output_suffix = '.sex'
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
pso_max_mag = 15
# panstarrs min magnitude
pso_min_mag = 0
# figure count
figure_count = 1

#
# END SETTINGS
#

# run external process


def runSubprocess(command_array):
    # command array is array with command and all required parameters
    try:
        with open(os.devnull, 'w') as fp:
            sp = subprocess.Popen(command_array, stderr=fp, stdout=fp)
        # logger.info('Running subprocess ("%s" %s)...'%(' '.join(command_array), sp.pid))
        sp.wait()
        output, error = sp.communicate()
        return (output, error, sp.pid)
    except:
        logger.info('Error. Subprocess ("%s" %d) failed.' %
                    (' '.join(command_array), sp.pid))
        return ('', '', 0)


def jdToYYMMDD_HHMMSS(jd):
    t = Time(jd, format='mjd', scale='utc')
    return t.iso


def onclick(event):
    logger.info('xdata=%f, ydata=%f' % (event.xdata, event.ydata))


def onclick_mag_vs_JD(event):
    logger.info('JD=%s, mag=%f' %
                (jdToYYMMDD_HHMMSS(event.xdata), event.ydata))


# set up the command line argument parser
parser = argparse.ArgumentParser(
    description='Perform lightcurve photometry using sextractor.')
parser.add_argument('--plot_apd', action='store_true',
                    help='Plot average object magnitude vs. aperture diameter for all images.')
parser.add_argument('--plot_ds9', action='store_true',
                    help='Plot apertures for each image using DS9.')
parser.add_argument('--labels', action='store_true',
                    help='Add labels to magnitude plot(s).')
parser.add_argument("--apd", help="Perform analysis for one or more apertures (csv).",
                    type=str)
args = parser.parse_args()

# make sure input files and folder exist
inputs = [input_path, sextractor_bin_fname, sextractor_cfg_fname,
          sextractor_param_fname,  sextractor_filter_fname, comps_fname]
for input in inputs:
    if not os.path.exists(input_path):
        raise Exception('Error. The file or path (%s) does not exist.' % input)

# do output directories exist? If not, create them...
outputs = [sex_output_path, cleaned_output_path]
for output in outputs:
    try:
        os.mkdir(output)
    except:
        pass

# grab aperture settings from sextractor config file, hopefully it will match the magnitude array ;)
apertures = []
apertures_string = ''
with open(sextractor_cfg_fname) as f:
    lines = f.readlines()
    for line in lines:
        match = re.match(r'^PHOT_APERTURES([\s\.0-9\,]+)', line)
        if match:
            apertures_string = match.group(1).strip()
            apertures = np.array(
                [int(aperture) for aperture in apertures_string.split(',')])
logger.info('Photometry to be performed for %d aperture diameters: %s.' %
            (len(apertures), apertures_string))

image_data = []
# get a list of all FITS files in the input directory
fits_files = glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
# loop through all qualifying files and perform sextraction
for fits_file in sorted(fits_files):
    fits_data = fits.open(fits_file)
    header = fits_data[0].header
    wcs = WCS(header)
    try:
        airmass = header['AIRMASS']
        dt_obs = dateutil.parser.parse(header['DATE-OBS'])
        naxis1 = header['NAXIS1']
        naxis2 = header['NAXIS2']
        ra = header['CRVAL1']
        dec = header['CRVAL2']
        JD = header['MJD-OBS']
    except KeyError:
        raise Exception('Error. Invalid FITS header in %s.' % fits_file)
    # calculate image corners in ra/dec
    ra1, dec1 = wcs.all_pix2world(0, 0, 0)
    ra2, dec2 = wcs.all_pix2world(naxis1, naxis2, 0)
    # calculate search radius in degrees from the center!
    c1 = SkyCoord(ra1, dec1, unit="deg")
    c2 = SkyCoord(ra2, dec2, unit="deg")
    # estimate radius of FOV in arcmin
    r_arcmin = '%f' % (c1.separation(c2).deg*60/2)
    logger.info("Sextracting %s" % (fits_file))
    output_file = sex_output_path + \
        fits_file.replace('\\', '/').rsplit('/', 1)[1]
    output_file = '%s%s.txt' % (output_file, sex_output_suffix)
    # add input filename, output filename, airmass, and jd to sex_file list
    image_data.append(
        {'image': fits_file, 'sex': output_file, 'jd': JD, 'airmass': airmass, 'ra': ra, 'dec': dec, 'dt_obs': dt_obs, 'r_arcmin': r_arcmin, 'found_target': True})
    # sextract this file
    (output, error, id) = runSubprocess([sextractor_bin_fname, fits_file, '-c', sextractor_cfg_fname, '-catalog_name',
                                         output_file, '-parameters_name', sextractor_param_fname, '-filter_name', sextractor_filter_fname])
    if error:
        raise Exception('Error. Sextractor failed: %s' % output)
logger.info('Sextracted %d files.' % len(image_data))

# build list of comparison stars in comps_fname using
# PanSTARRS Stack Object Catalog Search
logger.info('Searching for comparison stars in the PANSTARRS catalog (ra=%s deg, dec=%s deg, radius=%s min)...' %
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
    raise Exception('Error. No comparison stars found!')
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
    logger.info('Error. No comparison stars meet the criteria (%s > %f & %s < %f)!' %
                (pso_ref_mag, pso_min_mag, pso_ref_mag, pso_max_mag))
    exit()
logger.info('A total of %d comparison star(s) met the criteria (%s > %f & %s < %f)!' %
            (len(comps), pso_ref_mag, pso_min_mag, pso_ref_mag, pso_max_mag))
# output objects to comps_fname in sextract input format
comps_for_sex = comps[['raMean', 'decMean',
                       'objName', 'gPSFMag',  'rPSFMag',  'iPSFMag']]
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
    G = float(spl[3])
    R = float(spl[4])
    I = float(spl[5])
    object_data.append(
        {'index': index, 'ra': ra, 'dec': dec, 'object_name': name, 'found': True, 'G': G, 'R': R, 'I': I})

logger.info('Searching for %d objects in sextracted data.' % len(object_data))
ofile = file(counts_out_fname, 'wt')
# look for target/comp matches in sextracted files
sex_data = []
for image in image_data:
    num_found = 0
    lines = [s for s in file(image['sex'], 'rt') if len(s) > 2]
    # unless object is target, stop looking for it if it was not found in one of the images
    for s in (x for x in object_data):
        found = False
        for l in lines:
            spl = l.split()
            ra = float(spl[0])
            dec = float(spl[1])
            if abs(ra-s['ra']) < dRa and abs(dec-s['dec']) < dDec:
                sex_data_element = {'object_index': s['index'], 'object_name': s['object_name'], 'object_ra': s['ra'], 'object_dec': s[
                    'dec'], 'jd': image['jd'], 'airmass': image['airmass'], 'image': image['image'], 'sex': image['sex']}
                sex_data_element['ra'] = spl[0]
                sex_data_element['dec'] = spl[1]
                sex_data_element['x'] = spl[2]
                sex_data_element['y'] = spl[3]
                sex_data_element['num_apertures'] = len(apertures)
                for i in range(0, len(apertures)):
                    sex_data_element['mag%02d' % apertures[i]] = spl[4+i]
                    sex_data_element['magerr%02d' %
                                     apertures[i]] = spl[4+len(apertures)+i]
                sex_data.append(sex_data_element)
                num_found += 1
                found = True
                break
    ofile.write('%s,%d\n' % (image['sex'], num_found))
ofile.close()
logger.info('Found %d observations of %d objects in %d sextracted files.' %
            (len(sex_data), len(object_data), len(image_data)))

# save compiled sex data to a new file
ofile = file(targets_out_fname, 'wt')
line = 'index,name,airmass,jd'
jd_index = 3
mag_start_index = len(line.split(','))
for i in range(0, len(apertures)):
    line += ',mag%02d' % apertures[i]
for i in range(0, len(apertures)):
    line += ',magerr%02d' % apertures[i]
ofile.write(line+'\n')
# sort by star desig, then JD
sex_data = sorted(sex_data, key=lambda x: (x['object_name'], x['jd']))
for o in sex_data:
    line = '%d,%s,%f,%f' % (
        o['object_index'], o['object_name'], o['airmass'], o['jd'])
    for i in range(0, len(apertures)):
        line += ',%s' % o['mag%02d' % apertures[i]]
    for i in range(0, len(apertures)):
        line += ',%s' % o['magerr%02d' % apertures[i]]
    ofile.write(line+'\n')
ofile.close()

# plot average mag vs aperture diameter if requested
if args.plot_apd:
    ofile = file(targets_out_fname, 'r')
    data = np.genfromtxt(ofile, delimiter=',', skip_header=1)
    for index, s in enumerate(object_data):
        filtered_array = np.array(filter(lambda row: row[0] == index, data))
        # ensure this object was detected!
        if len(filtered_array) == 0:
            continue
        fig = plt.figure(figure_count)
        cid = fig.canvas.mpl_connect('button_press_event', onclick)
        figure_count += 1
        magnitudes = np.mean(filtered_array, axis=0)[
            mag_start_index:mag_start_index+len(apertures)]
        # magnitude_stdevs = np.std(filtered_array, axis=0)[
        #    mag_start_index:mag_start_index+len(apertures)]
        # error of the mean mag is the quadrature of the original errors divided by the number of
        magnitude_errors = np.sum(filtered_array*filtered_array,
                                  axis=0)[mag_start_index+len(apertures):mag_start_index+2*len(apertures)]
        magnitude_errors = np.sqrt(magnitude_errors) / \
            np.ma.size(filtered_array, axis=0)
        plt.errorbar(apertures, magnitudes, yerr=magnitude_errors, marker='o',
                     color='black', elinewidth=0.5, linestyle='None', markersize=3)
        plt.gca().invert_yaxis()
        plt.xlabel('Aperture Diameter, D (pixels)')
        plt.ylabel('Ave. Instrumental Magnitude, m')
        plt.title(s['object_name'])

# plot target and comp stars in ds9
if args.plot_ds9:
    ds = DS9()
    ds.set('frame clear #all')
    for image in image_data:
        fits_file = image['image']
        fname = os.path.abspath(fits_file).replace('\\', '/')
        ds.set('file %s' % fname)
        ds.set('zscale')
        # ds.set('zoom to fit')
        w2 = WCS('%s' % fname)
        for s in object_data:
            # find a match in the sex data
            for o in sex_data:
                if o['image'] == fits_file and o['object_name'] == s['object_name']:
                    xp = int(float(o['x']))
                    yp = int(float(o['y']))
                    reg2 = 'regions command "point %s %s #color=lightgreen text=\'%s\' point=cross"' % (xp, yp, o['object_name'].replace(
                        '-', '').replace('.', ''))  # the times two because xvar is up and then again that value down
                    ds.set('%s' % (reg2))
        ds.set('frame new')
    ds.set('frame first')

if args.apd:
    logger.info(
        'Analyzing photometry for aperture diameter(s) = %s pixels.' % args.apd)
    apds = args.apd.split(',')
    apd_idxs = []
    for idx, apd in enumerate(apds):
        # make sure this aperture is in our data set!
        for index, aperture in enumerate(apertures):
            if aperture == int(apds[idx]):
                apd_idxs.append(index)
    if len(apd_idxs) != len(apds):
        logger.info(
            'Error. Could not match all apertures provided: %s.' % args.apd)
        os.sys.exit(1)
    # get color map
    cmap = plt.get_cmap('viridis')
    colors = cmap(np.linspace(0, 1, len(apd_idxs)))
    ofile = file(targets_out_fname, 'r')
    data = np.genfromtxt(ofile, delimiter=',', skip_header=1)
    for s in object_data:
        if s['index'] != target_index:
            continue
        fig = plt.figure(figure_count)
        cid = fig.canvas.mpl_connect('button_press_event', onclick_mag_vs_JD)
        figure_count += 1
        filtered_array = np.array(
            filter(lambda row: row[0] == s['index'], data))
        # ensure this object was detected!
        if len(filtered_array) == 0:
            continue
        jds = filtered_array[:, jd_index]
        for idx, apd_index in enumerate(apd_idxs):
            magnitudes = filtered_array[:, mag_start_index + apd_index]
            magnitude_errors = filtered_array[:,
                                              mag_start_index + apd_index + len(apertures)]
            plt.errorbar(jds, magnitudes, yerr=magnitude_errors, marker='o',
                         color=colors[idx], elinewidth=0.5, linestyle='None', markersize=3, label='%s px' % apds[idx])
        plt.gca().invert_yaxis()
        plt.xlabel('Julian Date')
        plt.ylabel('Instrumental Magnitude, m')
        plt.legend(loc='upper left')
        object_name = 'Object: ' + s['object_name']
        plt.title(object_name)

plt.show()
