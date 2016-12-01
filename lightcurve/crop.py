#
#crop.py
#
#crop fits images

import os
import glob
import math
import subprocess
import re
import sys
from decimal import Decimal
from astropy.io import fits
from astropy import wcs

from astropy import units as u
import ccdproc

def logme( str ):
   log.write(str + "\n")
   print str
   return

#MODIFY THESE FIELDS AS NEEDED!
#input path *with* ending forward slash
input_path='./'
#output path *with* ending forward slash
output_path='./cropped/'
#log file name
log_fname = 'log.crop.txt'
#suffix for output files, if any...
output_suffix='.cropped'

log=open(log_fname, 'a+') 

#be sure crop parameters were provided
if(len(sys.argv) != 5):
    logme('Error. Crop parameters are invalid.')
    log.close()
    sys.exit(-1) 

#trim image? set range here, or set to '' to disable
naxis1_start = int(sys.argv[1])
naxis1_end = int(sys.argv[2])
naxis2_start = int(sys.argv[3])
naxis2_end = int(sys.argv[4])  
trim_range = '[%d:%d, %d:%d]'%(naxis1_start, naxis1_end, naxis2_start, naxis2_end) #starts at 1, inclusive
logme('Trimming images to NAXIS1=%d to %d, NAXIS2=%d to %d.'%(naxis1_start, naxis1_end, naxis2_start, naxis2_end)) 

#does output directory exist? If not, create it
try:
    os.mkdir(output_path)
except:
    pass
    
#get a list of all FITS files in the input directory
fits_files=glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
#loop through all qualifying files and perform plate-solving
print 'Cropping images in %s' %input_path
for fit_file in fits_files:   
    #open image
    image = ccdproc.CCDData.read(fit_file, unit='adu', relax=True)

    if(len(trim_range) > 0):
        image = ccdproc.trim_image(image, trim_range);
    
    #save cropped image
    output_file = "%s"%(fit_file.rsplit('.',1)[0])+output_suffix+".fits"
    output_file = output_file.rsplit('/',1)[1]
    output_file = output_path+output_file
    #scale calibrated image back to int16, some FITS programs don't like float    
    hdulist = image.to_hdu()
    hdulist[0].scale('int16', bzero=32768)
    if(len(trim_range) > 0):
        hdulist[0].header['NAXIS1'] = '%d'%((naxis1_end-naxis1_start))
        hdulist[0].header['NAXIS2'] = '%d'%((naxis2_end-naxis2_start))        
    hdulist.writeto(output_file, clobber=True)
    
logme('Cropped %d images and saved to %s.' %(len(fits_files),output_path))

log.close()