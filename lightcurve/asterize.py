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
    #log.write(str + "\n")
    print str
    return


# MODIFY THESE FIELDS AS NEEDED!
# input path *with* ending forward slash
input_path = './'
# output path *with* ending forward slash
output_path = './wcs/'
# log file name
log_fname = 'log.asterized.txt'
# suffix for output files, if any...
output_suffix = '.wcs'
# path to astrometry.net solve_field executable
solve_field_path = '/usr/local/astrometry/bin/solve-field'
# plate solve this image
solve_field = True
# remove COMMENT and HISTORY header fields? prevent MPO Canopus crashes caused by large FITS headers
remove_comment_history = True
# convert WCS parameters from CD to ROTA to help with Maxim DL v5.x
convert_to_crota = False
# update the OBJRA and OBJDEC with the calculated field center?
update_ra_dec = False
# add date to final filename?
add_date_to_fname = True
# search radius in decimal degrees for plate solving
radius = 5
# upper limit on plate scale in arsecperpixel
scale_high = 2.0
# lower limit on plate scale in arsecperpixel
scale_low = 0.5

dt_start = None
dt_end = None
session_count = 1

count = 0

# ignore warnings
warnings.filterwarnings('ignore', category=VerifyWarning, append=True)

# does output directory exist? If not, create it
try:
    os.mkdir(output_path)
except:
    pass

input_fits = sys.argv[1]

error_flag = False

# remove spaces from filename
input_fits_nospace = string.replace(input_fits, ' ', '_')
input_fits_nospace = string.replace(input_fits_nospace, '(', '')
input_fits_nospace = string.replace(input_fits_nospace, ')', '')
os.rename(input_fits, input_fits_nospace)
input_fits = input_fits_nospace
if(solve_field == True):
    logme("\nSolving %s" % (input_fits))
    # pull out RA/DEC from the FITS header, if they exist
    d1 = fits.open('%s' % (input_fits))
    d1.close()
    h1 = d1[0].header
    try:
        ra = h1['RA']
        dec = h1['DEC']
    except KeyError:
        ra = h1['OBJCTRA']
        dec = h1['OBJCTDEC']
        raA = ''
        decA = ''
        for j in range(0, len(ra)):
            if ra[j] == ' ':
                raA += ':'
            else:
                raA += ra[j]

        for j in range(0, len(dec)):
            if dec[j] == ' ':
                decA += ':'
            else:
                decA += dec[j]
        ra = raA
        dec = decA

    # plate solve this image, using RA/DEC from FITS header
    # --crpix-center move CRVAL1 and CRVAL2 to center
    # print solve_field_path + \
    #    ' --no-fits2fits --overwrite --downsample 2 --crpix-center --guess-scale --ra %s --dec %s --radius 10.0 --cpulimit 30 --no-plots ' % (
    #        ra, dec) + '"%s"' % (input_fits)
#    output = subprocess.check_output(
#        solve_field_path + ' --no-fits2fits --overwrite --downsample 2 --crpix-center --guess-scale --ra %s --dec %s --radius %f --cpulimit 30 --no-plots ' % (ra, dec, radius)+'"%s"' % (input_fits), shell=True)
    output = subprocess.check_output(
        solve_field_path + ' --no-fits2fits --overwrite --downsample 2 --crpix-center --scale-low %f --scale-high %f --scale-units arcsecperpix --ra %s --dec %s --radius %f --cpulimit 30 --no-plots ' % (scale_low, scale_high, ra, dec, radius)+'"%s"' % (input_fits), shell=True)
    # logme(output)
    #print output

    # remove astrometry.net temporary files
    #os.system("find . -name '*.xyls' -delete;")
    #os.system("find . -name '*.axy' -delete;")
    #os.system("find . -name '*.corr' -delete;")
    #os.system("find . -name '*.match' -delete;")
    #os.system("find . -name '*.rdls' -delete;")
    #os.system("find . -name '*.solved' -delete;")
    #os.system("find . -name '*.wcs' -delete;")
    #os.system("find . -name '*.png' -delete;")
else:
    logme("\Duplicating %s" % (input_fits))
    os.system("cp %s %s.new" % (input_fits, input_fits.rsplit('.', 1)[0]))

# create final plate-solved FITS file
# pull out DATE-OBS from the FITS header, if they exist
d1 = fits.open('%s.new' % (input_fits.rsplit('.', 1)[0]))
h1 = d1[0].header
# remove COMMENT and HISTORY lines to help with MPO Canopus crashes
if(remove_comment_history == True):
    logme("Removing COMMENT and HISTORY FITS header fields...")
    h1.remove("COMMENT", True, True)
    h1.remove("HISTORY", True, True)
    # otherwise we end up with doubles in the data
    d1[0].scale('int32', bzero=1)
    d1.writeto('%s.new' % (input_fits.rsplit('.', 1)[
               0]), clobber=True, output_verify='exception')
d1.close()
try:
    date_obs = h1['DATE-OBS']
except KeyError:
    logme("Error! Observation date/time not found in FITS header for %s." %
          (input_fits))
    # quit()
    pass
if(dt_start == None):
    dt_start = parser.parse(date_obs)
    #print dt_start
dt_end = parser.parse(date_obs)
#print dt_end
date_obs = date_obs.replace(":", "_")
date_obs = date_obs.replace("-", "_")
date_obs = "." + date_obs
output_file = "%s" % (input_fits.rsplit(
    '.', 1)[0])+date_obs+output_suffix+".fits"
output_file = output_file.rsplit('/', 1)[1]
output_file = output_path+output_file
logme("Writing solution to "+output_file)
os.system('mv %s.new %s' % (input_fits.rsplit('.', 1)[0], output_file))
count += 1
