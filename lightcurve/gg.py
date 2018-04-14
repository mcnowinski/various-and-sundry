import os
import glob
import sys
from astropy.io import fits
from datetime import datetime

def logme( str ):
   log.write(str + "\n")
   print str
   return

#MODIFY THESE FIELDS AS NEEDED!
#input path *with* ending forward slash
input_path='./'
#output path *with* ending forward slash
output_fname='./%s'%datetime.now().strftime('%Y%m%d_%H%M%S.csv')
#log file name
log_fname = 'log.asterized.txt'

log=open(log_fname, 'a+')	
    
#get a list of all FITS files in the input directory	
fits_files=glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
#loop through all qualifying files and perform plate-solving
count = 0
for fits_file in sorted(fits_files):
    #pull out RA/DEC from the FITS header, if they exist
    d1=fits.open('%s'%fits_file)
    d1.close()
    h1=d1[0].header
    try:
        airmass=h1['TCS_AM']
    except KeyError:
        logme("Could not find airmass (TCS_AM) in FITS header of %s."%fits_file)
    count += 1
    print airmass

logme("\nComplete. Processed %d of %d files."%(count, len(fits_files)))

log.close()    