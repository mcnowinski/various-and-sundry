import os
import stat
import glob
import math
import subprocess
import re
import sys
import string
from decimal import Decimal
from astropy.io import fits
from astropy import wcs
from astropy.coordinates import Angle
import astropy.units as u
from dateutil import parser
import numpy
import warnings
from astropy.io.fits.verify import VerifyWarning


def logme(str):
    log.write(str + "\n")
    print str
    return


# MODIFY THESE FIELDS AS NEEDED!
# input path *with* ending forward slash
input_path = './'
# output path *with* ending forward slash
output_path = './preview/'
# log file name
log_fname = 'log.previewed.txt'
# suffix for output files, if any...
output_suffix = '.wcs'
# path to stiff fits to tiff converter
stiff_path = '/usr/local/bin/stiff'
stiff_outfile_name = 'previewer.tif'
# path to ImageMagick convert tool
convert_path = '/usr/bin/convert'
# path to ds9
ds9_path = 'C:/ds9.new/ds9.exe'
# path to cygwin for Windows
cygwin_path = 'C:/cygwin64/bin/bash.exe'

count = 0

# ignore warnings
warnings.filterwarnings('ignore', category=VerifyWarning, append=True)

# does output directory exist? If not, create it
try:
    os.mkdir(output_path)
except:
    pass

log = open(log_fname, 'a+')

# get a list of all FITS files in the input directory
fits_files = glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
# loop through all qualifying files and perform plate-solving
for fits_file in sorted(fits_files):
    # output=subprocess.check_output(stiff_path + ' "%s -OUTFILE_NAME=%s%s -VERBOSE_TYPE=QUIET -WRITE_XML=NO -COPY_HEADER=N -IMAGE_TYPE=TIFF"' %
    #                                 (os.path.abspath(fits_file).replace('\\', '/'), os.path.abspath(output_path).replace('\\', '/'), fits_file+'.tif'), shell=True, stderr=subprocess.STDOUT)
    # output = subprocess.check_output(ds9_path + ' -file "%s" -zoom to fit -scale zscale -export jpg %s%s 100 -exit' %
    #                                 (fits_file, output_path, fits_file+'.jpg'), shell=True, stderr=subprocess.STDOUT)
    stiff_cmd = '%s %s -OUTFILE_NAME=%s/%s -VERBOSE_TYPE=QUIET -WRITE_XML=NO -COPY_HEADER=N -IMAGE_TYPE=TIFF' % (
        stiff_path, os.path.abspath(fits_file).replace('\\', '/'), os.path.abspath(output_path).replace('\\', '/'), fits_file.replace('\\', '/') + '.tif')
    output = subprocess.check_output(
        '%s -l -c "%s"' % (cygwin_path, stiff_cmd), shell=True, stderr=subprocess.STDOUT)

log.close()
