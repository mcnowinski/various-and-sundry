import os
import glob
import math
import subprocess
import re
from decimal import Decimal
from astropy.io import fits
from astropy import wcs

#MODIFY THESE FIELDS AS NEEDED!
#input path *with* ending forward slash
input_path='./'
#output path *with* ending forward slash
#output_path='./renamed/'
output_path='./'

#does output directory exist? If not, create it
try:
    os.mkdir(output_path)
except:
    pass

count=0
#track image angle, mark as meridian flipped if detected
crota_cd_last = None
flip=0
    
#get a list of all FITS files in the input directory	
im=glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
#loop through all qualifying files and perform plate-solving
print '\nProcessing %d FITS files...'%(len(im))
for i in range(0,len(im)):
    prev=im[i]
    new=''
    for j in range(0,len(im[i])):
        if im[i][j]==' ':
            new+='_'
        else:
            new+=im[i][j]			
    os.chdir(".")
    os.rename(prev, new)
    #print("\nRenaming %s"%(new))
    #pull out RA/DEC from the FITS header, if they exist
    d1=fits.open('%s'%(new))
    d1.close()
    h1=d1[0].header
    try:
        date_obs=h1['DATE-OBS']
        cd11=h1['CD1_1']
        cd12=h1['CD1_2']		
        cd21=h1['CD2_1']		
        cd22=h1['CD2_2']
        
    except KeyError:
        print "Error! Date/time and WCS not found in %s."%(new)
        continue
    
    date_obs = date_obs.replace(":","_")
    date_obs = date_obs.replace("-","_")
    date_obs = "." + date_obs
    
    cdeltlon_cd = math.sqrt(cd11*cd11+cd21*cd21)
    cdeltlat_cd = math.sqrt(cd12*cd12+cd22*cd22)
    det = cd11*cd22 - cd12*cd21
    if det == 0.0:
      raise ValueError("Determinant of CD matrix == 0")
    sign = 1.0
    if det < 0.0:
      cdeltlon_cd = -cdeltlon_cd
      sign = -1.0
    rot1_cd = math.atan2(-cd21, sign*cd11)
    rot2_cd = math.atan2(sign*cd12, cd22)
    rot_av = (rot1_cd+rot2_cd)/2.0
    crota_cd = math.degrees(rot_av)
    if crota_cd_last == None:
        crota_cd_last = crota_cd
    else:
        if abs(crota_cd - crota_cd_last) > 90:
            flip = flip + 1
            print 'Meridian flip detected at %s.'%(new)
        crota_cd_last = crota_cd
    
    #create renamed FITS file
    if flip > 0:
        output_file = "%s"%(new.rsplit('.',1)[0])+date_obs+".flip%d"%(flip)+".fits"	
    else:
        output_file = "%s"%(new.rsplit('.',1)[0])+date_obs+".fits"
    output_file = output_file.rsplit('/',1)[1]
    output_file = output_path+output_file
    #print("Writing renamed file to "+output_file)
    os.system("mv %s "%(new)+output_file)
    count = count + 1
    
print 'Successfully renamed %d of %d files.'%(count,len(im))