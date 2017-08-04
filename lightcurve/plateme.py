import os
import glob
import math
import subprocess
import re
import sys
import string
from decimal import Decimal
from astropy.io import fits
from astropy import wcs
from dateutil import parser

def logme( str ):
   log.write(str + "\n")
   print str
   return

#MODIFY THESE FIELDS AS NEEDED!
#input path *with* ending forward slash
input_path='./'
#output path *with* ending forward slash
output_path='./wcs/'
#log file name
log_fname = 'log.plateme.txt'
#suffix for output files, if any...
output_suffix='.wcs'
#path to astrometry.net solve_field executable
solve_field_path='/usr/local/astrometry/bin/solve-field'
#image counter
count = 0

#does output directory exist? If not, create it
try:
    os.mkdir(output_path)
except:
    pass

log=open(log_fname, 'a+')	
    
#get a list of all FITS files in the input directory	
im=glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
#loop through all qualifying files and perform plate-solving
for new in sorted(im):
    error_flag = False

    #remove spaces from filename
    new_nospace = string.replace(new, ' ', '_')
    new_nospace = string.replace(new_nospace, '(', '')
    new_nospace = string.replace(new_nospace, ')', '')
    os.rename(new, new_nospace)
    new = new_nospace

    logme("\nSolving %s"%(new))
    #pull out RA/DEC from the FITS header, if they exist
    d1=fits.open('%s'%(new))
    d1.close()
    h1=d1[0].header
    try:
        ra=h1['RA']
        dec=h1['DEC']
    except KeyError:
        ra=h1['OBJCTRA']
        dec=h1['OBJCTDEC']
        raA=''
        decA=''
        for j in range(0,len(ra)):
            if ra[j]==' ':
                raA+=':'
            else:
                raA+=ra[j]

        for j in range(0,len(dec)):
            if dec[j]==' ':
                decA+=':'
            else:
                decA+=dec[j]
        ra=raA
        dec=decA
    
    #plate solve this image, using RA/DEC from FITS header
    output = subprocess.check_output(solve_field_path + ' --no-fits2fits --overwrite --downsample 2 --guess-scale --ra %s --dec %s --radius 10.0 --cpulimit 30 --no-plots '%(ra,dec)+'"%s"'%(new), shell=True)
    log.write(output)
    #print output
    
    #remove astrometry.net temporary files
    os.system("find . -name '*.xyls' -delete;")
    os.system("find . -name '*.axy' -delete;")
    os.system("find . -name '*.corr' -delete;")
    os.system("find . -name '*.match' -delete;")
    os.system("find . -name '*.rdls' -delete;")
    os.system("find . -name '*.solved' -delete;")
    os.system("find . -name '*.wcs' -delete;")
    os.system("find . -name '*.png' -delete;")
    
    output_file = "%s"%(new.rsplit('.',1)[0])+output_suffix+".fits"
    output_file = output_file.rsplit('/',1)[1]
    output_file = output_path+output_file
    logme("Writing solution to "+output_file)
    os.system('mv %s.new %s'%(new.rsplit('.',1)[0],output_file))
    
    count += 1
    
logme("\nComplete. Processed %d of %d files."%(count, len(im)))

log.close()    