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
# path to ImageMagick convert tool
convert_path = '/usr/bin/convert'

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
    output = subprocess.check_output(
        solve_field_path + ' --no-fits2fits --overwrite --downsample 2 --crpix-center --guess-scale --ra %s --dec %s --radius 10.0 --cpulimit 30 --no-plots ' % (ra, dec)+'"%s"' % (new), shell=True)
    log.write(output)

logme("\nComplete. Processed %d of %d files." % (count, len(im)))

log.close()
