#
#calibrate.py
#
#calibrate fits images using darks, flats, and bias frames
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

import numpy as np

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
flat_is_debiased = False
flat_ave_exptime = 0
flat_path='./flat/'
#or folder with flats *with* ending forward slash
#flat=./flats/'
#name of exposure variable in FITS header file
exposure_label='EXPTIME'

log=open(log_fname, 'a+')

#trim image? set range here, or set to '' to disable
trim_range = ''
if(len(sys.argv) == 5):
   naxis1_start = int(sys.argv[1])
   naxis1_end = int(sys.argv[2])
   naxis2_start = int(sys.argv[3])
   naxis2_end = int(sys.argv[4])  
   trim_range = '[%d:%d, %d:%d]'%(naxis1_start, naxis1_end, naxis2_start, naxis2_end) #starts at 1, inclusive
   logme('Trimming images to NAXIS1=%d to %d, NAXIS2=%d to %d.'%(naxis1_start, naxis1_end, naxis2_start, naxis2_end))

if(not have_dark and not have_bias and not have_flat):
    logme('Error. No calibrations specified.' % dark_path)
    log.close()
    sys.exit(-1)   

#does output directory exist? If not, create it
try:
    os.mkdir(output_path)
except:
    pass
 
#does dark frame exist?
if(have_dark):
    if not (os.path.isfile(dark_path) or os.path.isdir(dark_path)):
        logme('Error. Dark calibration frame(s) not found (%s).' % dark_path)
        log.close()
        sys.exit(-1)
    #open master dark
    if os.path.isfile(dark_path):
        logme('Opening master dark (%s)...'%(dark_path))
        dark = ccdproc.CCDData.read(dark_path, unit='adu')
    else:
        #create master dark frame
        im=glob.glob(dark_path+'*.fits')+glob.glob(dark_path+'*.fit')
        if(len(im) <= 0):
            logme('Error. Dark calibration frame(s) not found (%s).' % dark_path)
            log.close()
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
        logme('Creating master dark frame (%s)...'%(dark_path))
        dark = ccdproc.combine(darks, method='median', unit='adu', add_keyword=False)
        #trim it, if necessary    
        if(len(trim_range) > 0):
            #logme('Trimming dark image (%s)...'%(trim_range))
            dark = ccdproc.trim_image(dark, trim_range);
        hdulist = dark.to_hdu()
        hdulist.writeto(dark_path, clobber=True)
        #logme(hdulist[0].header)
        #if os.path.isfile(dark_path):
        #    os.system('rm %s' %dark_path)   
        #dark.write(dark_path, clobber=True)        

#does bias frame exist?
if(have_bias):
    if not (os.path.isfile(bias_path) or os.path.isdir(bias_path)):
        logme('Error. Bias calibration frame(s) not found (%s).' % bias_path)
        log.close()
        sys.exit(-1)    
    #open master bias
    if os.path.isfile(bias_path):
        logme('Opening master bias (%s)...'%(bias_path))
        bias = ccdproc.CCDData.read(bias_path, unit='adu')
    else:
        #create master bias frame
        im=glob.glob(bias_path+'*.fits')+glob.glob(bias_path+'*.fit')
        if(len(im) <= 0):
            logme('Error. Bias calibration frame(s) not found (%s).' % bias_path)
            log.close()
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
        
        logme('Creating master bias frame (%s)...'%(bias_path))
        bias = ccdproc.combine(biases, method='median', unit='adu', add_keyword=False)
        #trim it, if necessary    
        if(len(trim_range) > 0):
            #logme('Trimming bias image (%s)...'%(trim_range))
            bias = ccdproc.trim_image(bias, trim_range);
        hdulist = bias.to_hdu()
        hdulist.writeto(bias_path, clobber=True)
        #if os.path.isfile(bias_path):
        #    os.system('rm %s' %bias_path)   
        #bias.write(bias_path, clobber=True)
    
#does flat frame exist? 
if(have_flat):   
    if not (os.path.isfile(flat_path) or os.path.isdir(flat_path)):
        logme('Error. Flat calibration frame(s) not found (%s).' % flat_path)
        log.close()
        sys.exit(-1)    
    #open master flat
    if os.path.isfile(flat_path):
        logme('Opening master flat (%s)...'%(flat_path))
        flat = ccdproc.CCDData.read(flat_path, unit='adu')
    else:
        #create master flat frame
        im=glob.glob(flat_path+'*.fits')+glob.glob(flat_path+'*.fit')
        if(len(im) <= 0):
            logme('Error. Flat calibration frame(s) not found (%s).' % flat_path)
            log.close()
            sys.exit(-1)          
        flats = None
        count = 0
        #check a few things in these flat component frames
        for i in range(0,len(im)):
            #is this flat bias corrected?
            header = fits.getheader(im[i])
            if(header.get('BIAS') != None):
                flat_is_debiased = True 
            if(flats):
                flats += ','+im[i]
            else:
                flats = im[i]
            #calc average exposure time for dark correction
            if(header.get('EXPTIME') != None):
                flat_ave_exptime += float(header.get('EXPTIME'))
                count += 1
        #calc average exposure time
        if(count > 0):
            flat_ave_exptime = flat_ave_exptime/count
            logme("Average exposure time for flats is %f"%flat_ave_exptime)
        #if there is just one, make it two of the same!
        if (len(im) == 1):
            flats += ','+im[0]
        flat_path += 'master/'
        try:
            os.mkdir(flat_path)
        except:
            pass    
        flat_path += 'master_flat.fits'    
        
        if(flat_is_debiased):
            logme('Flat frames are bias corrected.')
        else:
            logme('Flat frames are NOT bias corrected.')        
        logme('Creating master flat frame (%s)...'%(flat_path))
        #scale the flat component frames to have the same mean value, 10000.0
        scaling_func = lambda arr: 10000.0/np.ma.median(arr)
        #combine them
        flat = ccdproc.combine(flats, method='median', scale=scaling_func, unit='adu', add_keyword=False)
        #trim it, if necessary    
        if(len(trim_range) > 0):
            #logme('Trimming flat image (%s)...'%(trim_range))
            flat = ccdproc.trim_image(flat, trim_range);
        hdulist = flat.to_hdu()
        if(flat_ave_exptime > 0):
            header=hdulist[0].header
            header['EXPTIME'] = flat_ave_exptime
        hdulist.writeto(flat_path, clobber=True)
    
#get a list of all FITS files in the input directory
fits_files=glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
#loop through all qualifying files and perform plate-solving
logme('Calibrating images in %s' %input_path)
for fit_file in fits_files:   
    #open image
    image = ccdproc.CCDData.read(fit_file, unit='adu', relax=True)

    if(len(trim_range) > 0):
        image = ccdproc.trim_image(image, trim_range);
    
    #subtract bias from light, dark, and flat frames    
    if(have_bias):
        image_bias_subtracted = ccdproc.subtract_bias(image, bias, add_keyword=False)
        if(have_dark):
            #subtract bias from dark
            dark_bias_subtracted = ccdproc.subtract_bias(dark, bias, add_keyword=False)
        if(have_flat):
            if(not flat_is_debiased):
                flat_bias_subtracted = ccdproc.subtract_bias(flat, bias, add_keyword=False)
            else:
                flat_bias_subtracted = flat
    else:
        image_bias_subtracted = image
        dark_bias_subtracted = dark
        flat_bias_subtracted = flat

    #subtract bias-corrected dark from bias-corrected light and flat images; scale if necessary
    if(have_dark):    
        #for image exp = dark exp, the bias frames cancel
        image_dark_subtracted = ccdproc.subtract_dark(image_bias_subtracted, dark_bias_subtracted, scale=True, exposure_time=exposure_label, exposure_unit=u.second, add_keyword=False)
        #subtact dark from flat if possible
        if(have_flat):
            if(flat_ave_exptime > 0):
                flat_dark_subtracted = ccdproc.subtract_dark(flat_bias_subtracted, dark_bias_subtracted, scale=True, exposure_time=exposure_label, exposure_unit=u.second, add_keyword=False)
            else:
                flat_dark_subtracted = flat_bias_subtracted
    else:
        image_dark_subtracted = image_bias_subtracted
        flat_dark_subtracted = flat_bias_subtracted

    #divide by normalized (and bias corrected) flat image        
    if(have_flat):
        image_calibrated = ccdproc.flat_correct(image_dark_subtracted, flat_dark_subtracted, add_keyword=False)
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
    if(len(trim_range) > 0):
        hdulist[0].header['NAXIS1'] = '%d'%((naxis1_end-naxis1_start))
        hdulist[0].header['NAXIS2'] = '%d'%((naxis2_end-naxis2_start))        
    hdulist.writeto(output_file, clobber=True)
    
logme('Calibrated %d images and saved to %s.' %(len(fits_files),output_path))
log.close()