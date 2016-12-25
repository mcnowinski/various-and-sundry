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
import datetime
import shutil
from decimal import Decimal
from astropy.io import fits
from astropy import wcs

from astropy import log
log.setLevel('ERROR')

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

#used in master calibration filenames   
date_suffix = datetime.datetime.now().strftime('%Y%m%d.%H%M%S')

#master bias frame
#folder with bias component frames *including* ending forward slash
bias_path='./bias/'
bias_master = 'mbias.' + date_suffix + '.fits' 

#master dark frame
#folder with dark component frames *including* ending forward slash
dark_path='./dark/'
dark_is_bias_corrected = False
dark_bias = None
dark_master = 'mdark.' + date_suffix + '.fits' 

#master flat frame
#folder with bias component frames *including* ending forward slash
flat_path='./flat/'
flat_is_bias_corrected = False
flat_bias = None
flat_is_dark_corrected = False
flat_dark = None
flat_ave_exptime = 0
flat_master = 'mflat.' + date_suffix + '.fits' 

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

#does output directory exist? If not, create it
try:
    os.mkdir(output_path)
except:
    pass

#bias    
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
#if there is just one, make it two of the same for the combine!
if (len(im) == 1):
    biases += ','+im[0]
bias_path += 'master/'
try:
    os.mkdir(bias_path)
except:
    pass    
bias_path += bias_master
logme('Creating master bias frame (%s)...'%(bias_path))
bias = ccdproc.combine(biases, method='median', unit='adu', add_keyword=False)
#trim it, if necessary    
if(len(trim_range) > 0):
    bias = ccdproc.trim_image(bias, trim_range);
#write master frame to file    
hdulist = bias.to_hdu()
hdulist.writeto(bias_path, clobber=True)
    
#dark
#create master dark frame
im=glob.glob(dark_path+'*.fits')+glob.glob(dark_path+'*.fit')
if(len(im) <= 0):
    logme('Error. Dark calibration frame(s) not found (%s).' % dark_path)
    log.close()
    sys.exit(-1)
darks = None
bias_header = None
for i in range(0,len(im)):
    #is (any) dark bias corrected?
    header = fits.getheader(im[i])
    if(header.get('BIAS') != None):
        dark_is_bias_corrected = True
        dark_bias = header.get('BIAS')
    elif(header.get('BIASCORR') != None):
        dark_is_bias_corrected = True
        dark_bias = header.get('BIASCORR')
    if(darks):
        darks += ','+im[i]
    else:
        darks = im[i]
#if there is just one, make it two of the same for the combine!
if (len(im) == 1):
    darks += ','+im[0]
dark_path += 'master/'
try:
    os.mkdir(dark_path)
except:
    pass    
dark_path += dark_master
logme('Creating master dark frame (%s)...'%(dark_path))
dark = ccdproc.combine(darks, method='median', unit='adu', add_keyword=False, **{'verify': 'ignore'})
#trim it, if necessary    
if(len(trim_range) > 0):
    dark = ccdproc.trim_image(dark, trim_range);
#bias correct, if necessary
if(not dark_is_bias_corrected):
    #logme('Subtracting master bias frame from master dark frame...')
    dark = ccdproc.subtract_bias(dark, bias, add_keyword=False)
    dark_bias = bias_master
else:
    logme('Master dark frame is *already* bias corrected (%s).'%dark_bias)        
#write master dark frame    
hdulist = dark.to_hdu()
#add bias correction to header
header=hdulist[0].header
header['BIASCORR'] = dark_bias      
hdulist.writeto(dark_path, clobber=True)

#flat
#create master flat frame
im=glob.glob(flat_path+'*.fits')+glob.glob(flat_path+'*.fit')
if(len(im) <= 0):
    logme('Error. Flat calibration frame(s) not found (%s).' % flat_path)
    log.close()
    sys.exit(-1)          
flats = None
count = 0
flat_corrected = None
#check a few things in these flat component frames
for i in range(0,len(im)):
    header = fits.getheader(im[i])
    #is this flat bias corrected?
    if(header.get('BIAS') != None):
        flat_is_bias_corrected = True
        flat_bias = header.get('BIAS')
    elif(header.get('BIASCORR') != None):
        flat_is_bias_corrected = True
        flat_bias = header.get('BIASCORR')
    #is this flat dark corrected?
    if(header.get('DARK') != None):
        flat_is_dark_corrected = True
        flat_dark = header.get('DARK')
    elif(header.get('DARKCORR') != None):
        flat_is_dark_corrected = True
        flat_dark = header.get('DARKCORR')
    flat_corrected = "%s"%(im[i].rsplit('.',1)[0])+".corrected"	
    shutil.copy(im[i], flat_corrected)
	#trim as necessary
    if(len(trim_range) > 0):
        flat = ccdproc.CCDData.read(flat_corrected, unit='adu', relax=True)
        flat = ccdproc.trim_image(flat, trim_range)
        hdulist = flat.to_hdu()
        hdulist.writeto(flat_corrected, clobber=True)
    #bias correct, if necessary
    if(not flat_is_bias_corrected):
        #logme('Subtracting master bias frame from flat frame...')
        flat = ccdproc.CCDData.read(flat_corrected, unit='adu', relax=True)
        #trim it, if necessary    
        #if(len(trim_range) > 0):
        #    flat = ccdproc.trim_image(flat, trim_range);
        #flat = ccdproc.subtract_bias(flat, bias, add_keyword=False)
        hdulist = flat.to_hdu()
        #add bias correction to header
        header=hdulist[0].header
        header['BIASCORR'] = flat_bias     
        hdulist.writeto(flat_corrected, clobber=True)        
        flat_bias = bias_master
    else:
        logme('Flat frame (%s) is *already* bias corrected (%s).'%(im[i],flat_bias))
    #dark correct, if necessary
    if(not flat_is_dark_corrected):
        #logme('Subtracting master dark frame from flat frame...')
        flat = ccdproc.CCDData.read(flat_corrected, unit='adu', relax=True)
        ##trim it, if necessary    
        #if(len(trim_range) > 0):
        #    flat = ccdproc.trim_image(flat, trim_range);
        flat = ccdproc.subtract_dark(flat, dark, scale=True, exposure_time=exposure_label, exposure_unit=u.second, add_keyword=False)
        hdulist = flat.to_hdu()
        #add bias correction to header
        header=hdulist[0].header
        header['DARKCORR'] = dark_bias    
        hdulist.writeto(flat_corrected, clobber=True)       
        flat_dark = dark_master
    else:
        logme('Flat frame (%s) is *already* dark corrected (%s).'%(im[i],flat_dark)  )      
    if(flats):
        flats += ','+flat_corrected
    else:
        flats = flat_corrected
    #calc average exposure time for potential dark correction
    if(header.get('EXPTIME') != None):
        #print header.get('EXPTIME')
        try:
            exptime = float(header.get('EXPTIME'))
            flat_ave_exptime += exptime
        except ValueError:
            logme('Exposure time (EXPTIME) is not a float (%s).'%(header.get('EXPTIME')))
        count += 1
#calc average exposure time
#if(count > 0):
#    flat_ave_exptime = flat_ave_exptime/count
#    flat.header['EXPTIME'] = flat_ave_exptime
#    logme("Average exposure time for flats is %f"%flat_ave_exptime)
#if there is just one, make it two of the same!
if (len(im) == 1):
    flats += ','+flat_corrected
flat_path += 'master/'
try:
    os.mkdir(flat_path)
except:
    pass    
flat_path += flat_master    
logme('Creating master flat frame (%s)...'%(flat_path))
#scale the flat component frames to have the same mean value, 10000.0
scaling_func = lambda arr: 10000.0/np.ma.median(arr)
#combine them
flat = ccdproc.combine(flats, method='median', scale=scaling_func, unit='adu', add_keyword=False)
##trim it, if necessary    
#if(len(trim_range) > 0):
#    #logme('Trimming flat image (%s)...'%(trim_range))
#    flat = ccdproc.trim_image(flat, trim_range);    
#write master flat frame    
hdulist = flat.to_hdu()
#add bias correction to header
header=hdulist[0].header
header['BIASCORR'] = flat_bias 
header['DARKCORR'] = flat_dark
if(count > 0):
    flat_ave_exptime = flat_ave_exptime/count
    header['EXPTIME'] = flat_ave_exptime  
hdulist.writeto(flat_path, clobber=True)
 
#get a list of all FITS files in the input directory
fits_files=glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
#loop through all qualifying files and perform plate-solving
logme('Calibrating images in %s' %input_path)
for fits_file in fits_files:   
    #open image
    image = ccdproc.CCDData.read(fits_file, unit='adu', relax=True)
    #trim it, if necessary 
    if(len(trim_range) > 0):
        image = ccdproc.trim_image(image, trim_range);
    #subtract bias from light, dark, and flat frames    
    image = ccdproc.subtract_bias(image, bias, add_keyword=False)
    image = ccdproc.subtract_dark(image, dark, scale=True, exposure_time=exposure_label, exposure_unit=u.second, add_keyword=False)
    image = ccdproc.flat_correct(image, flat, add_keyword=False)    
    #save calibrated image
    output_file = "%s"%(fits_file.rsplit('.',1)[0])+output_suffix+".fits"
    output_file = output_file.rsplit('/',1)[1]
    output_file = output_path+output_file
    #scale calibrated image back to int16, some FITS programs don't like float    
    hdulist = image.to_hdu()
    hdulist[0].scale('int16', bzero=32768)
    hdulist[0].header['BIASCORR'] = bias_master
    hdulist[0].header['DARKCORR'] = dark_master        
    hdulist[0].header['FLATCORR'] = flat_master
    if(len(trim_range) > 0):
        hdulist[0].header['NAXIS1'] = '%d'%((naxis1_end-naxis1_start))
        hdulist[0].header['NAXIS2'] = '%d'%((naxis2_end-naxis2_start))        
    hdulist.writeto(output_file, clobber=True)
    
logme('Calibrated %d images and saved to %s.' %(len(fits_files),output_path))
log.close()