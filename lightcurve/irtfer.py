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

# MODIFY THESE FIELDS AS NEEDED!
# input path *with* ending forward slash
input_path = './'

# ignore warnings
warnings.filterwarnings('ignore', category=VerifyWarning, append=True)

print 'OBJECT' + ',' + 'ITIME' + ',' + 'COADDS' + \
    ',' + 'CYCLES' + ',' + 'FITS' + ',' + 'MJD'

# get a list of all FITS files in the input directory
fitsfiles = glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
# loop through all qualifying files and perform plate-solving
for fitsfile in sorted(fitsfiles):
    d1 = fits.open('%s' % fitsfile)
    d1.close()
    h1 = d1[0].header
    try:
        object = str(h1['OBJECT'])
        itime = str(h1['ITIME'])
        coadds = str(h1['CO_ADDS'])
        irafname = str(h1['IRAFNAME'])
        cycles = str(h1['CYCLES'])
        mjd = str(h1['MJD_OBS'])
        print object + ',' + itime + ',' + coadds + \
            ',' + cycles + ',' + irafname + ',' + mjd
    except KeyError:
        pass
