import os
import glob
import math
import subprocess
import re
import sys
import string
import time
#from playsound import playsound
from decimal import Decimal
from astropy.io import fits
from astropy import wcs
from dateutil import parser
from os.path import expanduser

if(len(sys.argv) >= 2):
    obs_id = int(sys.argv[1])
else:
    print 'Error. Invalid observation id.'
    os.sys.exit(1)

#MODIFY THESE FIELDS AS NEEDED!
#image_number=17784327
login_url='https://skynet.unc.edu/user/login'
download_url='https://skynet.unc.edu/download/fits?image=r17784327&reducequiet=1&force_int=1'
obs_url='https://skynet.unc.edu/obs/view?id=%s'%obs_id
cookies_filepath='/tmp/skynet.cookies.txt'
username='matt.nowinski'
password='N0passwd'
#log file name
log_fname = 'skyeye.log'
wget_fname = 'skyeye.wget.log'
obs_fname = 'skeye.obs.htm'
#path to astrometry.net solve_field executable
solve_field_path='/usr/local/astrometry/bin/solve-field'
#path to home directory; in Windows 10, this is usually C:\users\<username>\
home=expanduser("~")
sound_filepath='%s/knock_brush.mp3'%(home)

log=open(log_fname, 'a+')

def logme( str ):
   log.write(str + "\n")
   print str
   return
def pause( seconds ):
    for i in range(0,seconds):
        time.sleep(1)

def playsound(repeat):
    for i in range(0,repeat):
        print "\a"
        pause(2)
   
#path to astrometry.net solve_field executable
solve_field_path='/usr/local/astrometry/bin/solve-field'     
def solveField( image ):
    logme("Solving %s..."%(image))
    #pull out RA/DEC from the FITS header, if they exist
    d1=fits.open('%s'%(image))
    d1.close()
    h1=d1[0].header
    #CD1_1   =   -7.50319666746E-06 / Coordinate transformation matrix element
    #CD1_2   =   -0.000242768603276 / Coordinate transformation matrix element
    #CD2_1   =    0.000242768603276 / Coordinate transformation matrix element
    #CD2_2   =   -7.50319666746E-06 / Coordinate transformation matrix element
    #if WCS transformation matrix is found, return
    #try:
    #    cd11=h1['CD1_1']
    #    cd12=h1['CD1_2']
    #    cd21=h1['CD2_1']
    #    cd22=h1['CD2_2']
    #    logme('Found WCS in FITS header!')
    #    return
    #except:
    #    pass
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
    output = subprocess.check_output(solve_field_path + ' --no-fits2fits --overwrite --downsample 2 --guess-scale --ra %s --dec %s --radius 10.0 --cpulimit 30 --no-plots '%(ra,dec)+'%s'%(image), shell=True)
    #output = subprocess.check_output(solve_field_path + ' --no-fits2fits --overwrite --downsample 2 --guess-scale --cpulimit 30 --no-plots %s'%(image), shell=True)
    log.write(output)
    #remove astrometry.net temporary files
    os.system("find . -name '*.xyls' -delete;")
    os.system("find . -name '*.axy' -delete;")
    os.system("find . -name '*.corr' -delete;")
    os.system("find . -name '*.match' -delete;")
    os.system("find . -name '*.rdls' -delete;")
    os.system("find . -name '*.solved' -delete;")
    os.system("find . -name '*.wcs' -delete;")
    os.system("find . -name '*.png' -delete;")
    os.system("find . -name '*.new' -delete;")   
    #look for solution
    match = re.search('Field rotation angle\: up is ([0-9\-\.]+) degrees', output)    
    if not match:
        for i in range(0,20):
            logme('Error! Could not solve image (%s).'%image)
            playsound(10)
            pause(5)
    else:
        logme('Solved!')

count = 0

#get Skynet login cookie
result = os.system('wget -a %s --save-cookies %s --keep-session-cookies --delete-after --post-data "username=%s&password=%s" %s'%(wget_fname,cookies_filepath,username,password,login_url))
#print result

playsound(2)

count=0
while(1):
    #query observation page for image count
    result = os.system('wget -a %s --load-cookies %s -O %s %s'%(wget_fname,cookies_filepath,obs_fname,obs_url))
    #read wget output
    obs=open(obs_fname,'r')
    contents=obs.read()
    obs.close()
    #print contents
    #look for image count
    match1 = re.search('([0-9]+) of [0-9]+ image\(s\) taken', contents)  
    num_images=count
    if match1:
        print 'Image count = %s.'%(match1.group(1).strip())
        num_images=int(match1.group(1).strip())
    else:
        print 'Error. Could not find current image count.'
    if(num_images <= 0):
        pause(60)
        continue 
    #look for id of first image in observation
    match2 = re.search('\<strong\>ID\:\<\/strong\>([\\s0-9]+)\<\/span\>', contents)
    if match2:
        print 'First image of this observation is %s.'%(match2.group(1).strip())
        image_number_first=int(match2.group(1).strip())
    else:
        print 'Error. Could not find the first image of this observation.'
    if(num_images > count):
        count = num_images-1
    if(match1 and match2):   
        for i in range(count,num_images):
            image_number=image_number_first+i
            print 'Downloading Image #%d...'%image_number
            result = os.system('wget -a %s -O %d.fits --load-cookies %s https://skynet.unc.edu/download/fits?image=r%d&reducequiet=1&force_int=1'%(wget_fname,image_number,cookies_filepath,image_number))
            pause(60)
            try:
                #test to see if fits file is valid
                d1=fits.open('%d.fits'%(image_number))
                d1.close()                
            except:
                print 'Error. Invalid FITS file (%d.fits).'%image_number
                continue
            #try:
            solveField('%d.fits'%image_number)
            #except:
            #    logme('Error. Could not solve image!')
            #    pass
        count = num_images    
    #wait before next check
    pause(60)
    
logme("\nComplete. Processed %d images."%(count))

log.close()    