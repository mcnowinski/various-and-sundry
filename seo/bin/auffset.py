import os
import glob
import math
import subprocess
import re
import sys
from decimal import Decimal
import datetime
##gain access to local astropy module
#sys.path.append('/home/mcnowinski/astropy/lib/python2.7/site-packages/astropy-1.2.1-py2.7-linux-x86_64.egg')
from astropy.io import fits
from astropy import wcs
from astropy.io.fits import getheader
import astropy.coordinates as coord
import astropy.units as u

def logme( str ):
   log.write(str + "\n")
   print >> sys.stderr, str
   return

##check command line parameters
#if len(sys.argv) > 3:
#    print 'usage: auffset <RA true (HH:MM:SS.SS, optional)> <DEC true (DD:MM:SS.SS, optional)>'
#    sys.exit(1)

#get target ra and dec from command line, if available
ra_target = None
dec_target = None
if len(sys.argv) >= 3:
    #convert to decimal degrees
    ra_target = coord.Angle(sys.argv[1], unit=u.hour).degree
    dec_target = coord.Angle(sys.argv[2], unit=u.deg).degree
    
#MODIFY THESE FIELDS AS NEEDED!
#log file name
log_fname = '/tmp/'+datetime.datetime.now().strftime("%Y%m%d.%H%M%S%3N.")+'log.auffset.txt'
#path to astrometry.net solve_field executable
solve_field_path='/home/mcnowinski/astrometry/bin/solve-field'
#astrometry parameters
downsample=2
scale_low=0.55
scale_high=2.00
radius=1.0
cpu_limit=30
#offset limits (deg)
max_ra_offset=2.0
max_dec_offset=2.0
min_ra_offset=0.05
min_dec_offset=0.05
#how many pointing iterations to allow?
max_tries=5
#image command parameters
time=10
bin=2
fits_fname='/tmp/'+datetime.datetime.now().strftime("%Y%m%d.%H%M%S%3N.")+'pointing.fits'

log=open(log_fname, 'a+')	

#fits_fname = sys.argv[1]   

ra_offset = 2.0
dec_offset = 2.0
iteration = 0
while((abs(ra_offset) > min_ra_offset or abs(dec_offset) > min_dec_offset) and iteration < max_tries):
    iteration += 1
    
    logme('Performing adjustment #%d...'%(iteration))

    #get pointing image
    os.system('image time=%d bin=%d outfile=%s' % (time, bin, fits_fname));
    if not os.path.isfile(fits_fname):
        logme('Error. File (%s) not found.' % fits_fname)
        log.close()
        sys.exit(1)

    #get FITS header, pull RA and DEC for cueing the plate solving
    if(ra_target == None or dec_target == None):
        header = getheader(fits_fname)
        try:
            ra_target = header['RA']
            ra_target = coord.Angle(ra_target, unit=u.hour).degree # create an Angle object
            dec_target = header['DEC']
            dec_target = coord.Angle(dec_target, unit=u.deg).degree
        except KeyError:
            logme("Error. RA/DEC not found in input FITS header (%s)." % fits_fname)
            log.close()
            sys.exit(1)
            
    #plate solve this image, using RA/DEC from FITS header
    output = subprocess.check_output(solve_field_path + ' --no-verify --overwrite --no-remove-lines --downsample %d --scale-units arcsecperpix --scale-low %f --scale-high %f --ra %s --dec %s --radius %f --cpulimit %d --no-plots '%(downsample,scale_low,scale_high,ra_target,dec_target,radius,cpu_limit)+fits_fname, shell=True)
    #output = subprocess.check_output(solve_field_path + ' --no-verify --overwrite --downsample %d --cpulimit %d --no-plots '%(downsample,cpu_limit)+fits_fname, shell=True)
    log.write(output)

    #look for field center in solve-field output
    match = re.search('Field center\: \(RA,Dec\) \= \(([0-9\-\.\s]+)\,([0-9\-\.\s]+)\) deg\.', output)
    if match:
        RA_image=match.group(1).strip()
        DEC_image=match.group(2).strip()		
    else:
        logme("Error. Field center RA/DEC not found in solve-field output!")
        sys.exit(1)

    ra_offset=float(ra_target)-float(RA_image)
    dec_offset=float(dec_target)-float(DEC_image)

    if(abs(ra_offset) <= max_ra_offset and abs(dec_offset) <=max_dec_offset):
        os.system('tx offset ra=%f dec=%f cos > /dev/null' % (ra_offset, dec_offset))
        #print "Offset complete (tx offset ra=%f dec=%f)." % (ra_offset, dec_offset)
        logme("...complete (dRA=%f deg, dDEC=%f deg)."%(ra_offset, dec_offset))
    else:
        logme("Error. Calculated offsets too large (tx offset ra=%f dec=%f)! Pinpoint aborted." % (ra_offset, dec_offset))   
        sys.exit(1)
    #hms = coord.Angle(RA_image, unit=u.degree).hms
    #dms = coord.Angle(DEC_image, unit=u.degree).dms
    #print "center ra=%d:%d:%f dec=%d:%d:%f" % (hms.h, hms.m, hms.s, dms.d, dms.m, dms.s)

logme('BAM! Your target has been pinpoint-ed!')
log.close()
os.remove(fits_fname)
sys.exit(0)