#
#calibrate.py
#
#calibrate fits images using darks and flats
#corrected image = (image - bias - k(dark-bias))/flat
#for k=1, i.e. image exp = dark exp, corrected image = (image - dark)/flat

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
output_path='./calibrated/'
#log file name
log_fname = 'log.calibrate.txt'
#suffix for output files, if any...
output_suffix='.calibrated'
#master dark frame
#dark='./darks/dark.fits'
#or folder with darks *with* ending forward slash
have_dark = True
dark_path='./dark/'
#master bias frame
#bias='./bias/bias.fits'
#or folder with bias *with* ending forward slash
have_bias = True
bias_path='./bias/'
#master flat frame
have_flat = True
flat_path='./flat/'
#or folder with flats *with* ending forward slash
#flat=./flats/'
#name of exposure variable in FITS header file
exposure_label='EXPTIME'

log=open(log_fname, 'a+')

if(not have_dark and not have_bias and not have_flat):
    print 'Error. No calibrations specified.' % dark_path
    sys.exit(-1)   

#does output directory exist? If not, create it
try:
    os.mkdir(output_path)
except:
    pass
 
#does dark frame exist?
if(have_dark):
    if not (os.path.isfile(dark_path) or os.path.isdir(dark_path)):
        print 'Error. Dark calibration frame(s) not found (%s).' % dark_path
        sys.exit(-1)
    #open master dark
    if os.path.isfile(dark_path):
        print 'Opening master dark (%s)...'%(dark_path)
        dark = ccdproc.CCDData.read(dark_path, unit='adu')
    else:
        #create master dark frame
        im=glob.glob(dark_path+'*.fits')+glob.glob(dark_path+'*.fit')
        if(len(im) <= 0):
            print 'Error. Dark calibration frame(s) not found (%s).' % dark_path
            sys.exit(-1)
        darks = None
        for i in range(0,len(im)):
            if(darks):
                darks += ','+im[i]
            else:
                darks = im[i]
        #if there is just one, make it two of the same!
        if (len(im) == 1):
            darks += ','+im[0]
        dark_path += 'master/'
        try:
            os.mkdir(dark_path)
        except:
            pass    
        dark_path += 'master_dark.fits'
        print 'Creating master dark frame (%s)...'%(dark_path)
        dark = ccdproc.combine(darks, method='median', unit='adu', add_keyword=False)
        hdulist = dark.to_hdu()
        hdulist.writeto(dark_path, clobber=True)
        #print hdulist[0].header
        #if os.path.isfile(dark_path):
        #    os.system('rm %s' %dark_path)   
        #dark.write(dark_path, clobber=True)        

#does bias frame exist?
if(have_bias):
    if not (os.path.isfile(bias_path) or os.path.isdir(bias_path)):
        print 'Error. Bias calibration frame(s) not found (%s).' % bias_path
        sys.exit(-1)    
    #open master bias
    if os.path.isfile(bias_path):
        print 'Opening master bias (%s)...'%(bias_path)
        bias = ccdproc.CCDData.read(bias_path, unit='adu')
    else:
        #create master bias frame
        im=glob.glob(bias_path+'*.fits')+glob.glob(bias_path+'*.fit')
        if(len(im) <= 0):
            print 'Error. Bias calibration frame(s) not found (%s).' % bias_path
            sys.exit(-1)
        biases = None
        for i in range(0,len(im)):
            if(biases):
                biases += ','+im[i]
            else:
                biases = im[i]
        #if there is just one, make it two of the same!
        if (len(im) == 1):
            biases += ','+im[0]
        bias_path += 'master/'
        try:
            os.mkdir(bias_path)
        except:
            pass    
        bias_path += 'master_bias.fits'    
        
        print 'Creating master bias frame (%s)...'%(bias_path)
        bias = ccdproc.combine(biases, method='median', unit='adu', add_keyword=False)
        hdulist = bias.to_hdu()
        hdulist.writeto(bias_path, clobber=True)
        #if os.path.isfile(bias_path):
        #    os.system('rm %s' %bias_path)   
        #bias.write(bias_path, clobber=True)
    
#does flat frame exist? 
if(have_flat):   
    if not (os.path.isfile(flat_path) or os.path.isdir(flat_path)):
        print 'Error. Flat calibration frame(s) not found (%s).' % flat_path
        sys.exit(-1)    
    #open master flat
    if os.path.isfile(flat_path):
        print 'Opening master flat (%s)...'%(flat_path)
        flat = ccdproc.CCDData.read(flat_path, unit='adu')
    else:
        #create master flat frame
        im=glob.glob(flat_path+'*.fits')+glob.glob(flat_path+'*.fit')
        if(len(im) <= 0):
            print 'Error. Flat calibration frame(s) not found (%s).' % flat_path
            sys.exit(-1)
        flats = None
        for i in range(0,len(im)):
            if(flats):
                flats += ','+im[i]
            else:
                flats = im[i]
        #if there is just one, make it two of the same!
        if (len(im) == 1):
            flats += ','+im[0]
        flat_path += 'master/'
        try:
            os.mkdir(flat_path)
        except:
            pass    
        flat_path += 'master_flat.fits'    
        
        print 'Creating master flat frame (%s)...'%(flat_path)
        flat = ccdproc.combine(flats, method='median', unit='adu', add_keyword=False)
        hdulist = flat.to_hdu()
        hdulist.writeto(flat_path, clobber=True)
    
#get a list of all FITS files in the input directory
fits_files=glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
#loop through all qualifying files and perform plate-solving
print 'Calibrating images in %s' %input_path
for fit_file in fits_files:   
    #open image
    image = ccdproc.CCDData.read(fit_file, unit='adu', relax=True)

    #subtract bias from image    
    if(have_bias):
        image_bias_subtracted = ccdproc.subtract_bias(image, bias, add_keyword=False)
        if(have_dark):
            #subtract bias from dark
            dark_bias_subtracted = ccdproc.subtract_bias(dark, bias, add_keyword=False)  
    else:
        image_bias_subtracted = image
        dark_bias_subtracted = dark

    #subtract bias-corrected dark from bias-corrected image; scale if necessary
    if(have_dark):    
        #for image exp = dark exp, the bias frames cancel
        image_dark_subtracted = ccdproc.subtract_dark(image_bias_subtracted, dark_bias_subtracted, scale=True, exposure_time=exposure_label, exposure_unit=u.second, add_keyword=False)
    else:
        image_dark_subtracted = image_bias_subtracted

    #divide by normalized (and bias corrected) flat image        
    if(have_flat):
        image_calibrated = ccdproc.flat_correct(image_dark_subtracted, flat, add_keyword=False)
    else:
        image_calibrated = image_dark_subtracted
    
    #save calibrated image
    output_file = "%s"%(fit_file.rsplit('.',1)[0])+output_suffix+".fits"
    output_file = output_file.rsplit('/',1)[1]
    output_file = output_path+output_file
    #scale calibrated image back to int16, some FITS programs don't like float    
    hdulist = image_calibrated.to_hdu()
    hdulist[0].scale('int16', bzero=32768)
    if(have_bias):
        hdulist[0].header['BIAS'] = os.path.basename(bias_path)
    if(have_dark):
        hdulist[0].header['DARK'] = os.path.basename(dark_path)        
    if(have_flat):
        hdulist[0].header['FLAT'] = os.path.basename(flat_path) 
    hdulist.writeto(output_file, clobber=True)
    
print 'Calibrated %d images and saved to %s.' %(len(fits_files),output_path)
log.close()