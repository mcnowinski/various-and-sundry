import os
import glob
import math
import subprocess
import re
import sys
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
output_path='./asterized/'
#log file name
log_fname = 'log.asterized.txt'
#suffix for output files, if any...
output_suffix='.asterized'
#path to astrometry.net solve_field executable
solve_field_path='/usr/local/astrometry/bin/solve-field'
#plate solve this image
solve_field=True
#remove COMMENT and HISTORY header fields? prevent MPO Canopus crashes caused by large FITS headers
remove_comment_history=True
#convert WCS parameters from CD to ROTA to help with Maxim DL v5.x
convert_to_crota=False
#update the OBJRA and OBJDEC with the calculated field center?
update_ra_dec=False
#watch the field rotation angle for a > 90 deg change; mark as flipped
detect_meridian_flip=True
last_rotation_angle=None
flip_count=0
#add date to final filename?
add_date_to_fname=True

dt_start = None
dt_end = None
session_count=1

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
    if(solve_field == True):
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
        output = subprocess.check_output(solve_field_path + ' --no-verify --overwrite --downsample 2 --scale-units arcsecperpix --scale-low 0.55 --scale-high 2.0 --ra %s --dec %s --radius 1.0 --cpulimit 60 --no-plots '%(ra,dec)+'%s'%(new), shell=True)
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
    else:
        logme("\Duplicating %s"%(new))
        os.system("cp %s %s.new"%(new,new.rsplit('.',1)[0]))
    
    #see if meridian flipped; parse solve-field "Field rotation angle" output; I am sure there is a better way, i.e. use resulting FITS header
    if(solve_field == True and detect_meridian_flip==True):
        #Field rotation angle: up is 88.1839 degrees E of N
        match = re.search('Field rotation angle\: up is ([0-9\-\.]+) degrees', output)	
        if match:
            rotation_angle=match.group(1).strip()
            if(last_rotation_angle == None):
                pass
            else:
                if(abs(Decimal(last_rotation_angle)-Decimal(rotation_angle)) > 90):
                    logme("Meridian flipped detected!")
                    flip_count = flip_count + 1
                    #output_suffix += ".flip%d"%(flip_count)
                    if(add_date_to_fname == True):
                        #print (dt_start+(dt_end-dt_start)/2)
                        dt = dt_start+(dt_end-dt_start)/2
                        session = dt.strftime("%Y-%m-%dT%Hh%Mm%Ss")
                        session = "session.%02d.%s"%(session_count,session)
                        try:
                            os.mkdir(output_path+session)
                        except:
                            pass
                        os.system("mv %s*.fits %s%s"%(output_path,output_path,session))
                        logme("Created new session. Moved .fits files to %s%s."%(output_path,session)) 
                        session_count += 1                        
                        dt_start = None
            last_rotation_angle = rotation_angle				
        else:
            logme("Warning. Field rotation angle not found in solve-field output!")
    
    #create final plate-solved FITS file
    if(add_date_to_fname == True):
        #pull out DATE-OBS from the FITS header, if they exist
        d1=fits.open('%s'%(new))
        d1.close()
        h1=d1[0].header
        try:
            date_obs=h1['DATE-OBS']
        except KeyError:
            logme("Error! Observation date/time not found in FITS header for %s."%(new))
            quit()
        if(dt_start == None):
            dt_start = parser.parse(date_obs)
            #print dt_start
        dt_end  = parser.parse(date_obs)    
        #print dt_end
        date_obs = date_obs.replace(":","_")
        date_obs = date_obs.replace("-","_")
        date_obs = "." + date_obs
        output_file = "%s"%(new.rsplit('.',1)[0])+date_obs+output_suffix+".flip%d"%(flip_count)+".fits"		
    else:
        output_file = "%s"%(new.rsplit('.',1)[0])+output_suffix++".flip%d"%(flip_count)+".fits"
    
    output_file = output_file.rsplit('/',1)[1]
    output_file = output_path+output_file
    logme("Writing solution to "+output_file)
    os.system("mv %s.new "%(new.rsplit('.',1)[0])+output_file)
    
    #remove COMMENT and HISTORY lines to help with MPO Canopus crashes
    if(remove_comment_history==True):
        logme("Removing COMMENT and HISTORY FITS header fields...")
        #add legacy WCS parameters (e.g., to support MaximDL 5.x)
        data, header = fits.getdata(output_file, header=True)
        header.remove("COMMENT",True,True)
        header.remove("HISTORY",True,True)
        fits.writeto(output_file, data, header, clobber=True, output_verify='warn')
        
    #convert CDxx transformation matrix to old CDELT,CROTA format (e.g., to support MaximDL v5.x)
    # AUTHOR:  M.G.R. Vogelaar, University of Groningen, The Netherlands
    # DATE:    April 17, 2008	
    if(convert_to_crota==True):
        logme("Adding WCS CDELTx and CROTAx FITS header fields...")
        data, header = fits.getdata(output_file, header=True)
        
        cd11=header.get('CD1_1')
        cd12=header.get('CD1_2')
        cd21=header.get('CD2_1')
        cd22=header.get('CD2_2')
        
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
        skew = math.degrees(abs(rot1_cd-rot2_cd))
        
        #print "Angles from cd matrix:", math.degrees(rot1_cd), math.degrees(rot2_cd), crota_cd
        #print "Cdelt's from cd matrix:", cdeltlon_cd, cdeltlat_cd
        #print "Difference in angles (deg)", skew

        header.set('CDELT1',cdeltlon_cd)
        header.set('CDELT2',cdeltlat_cd)
        header.set('CROTA1',crota_cd)
        header.set('CROTA2',crota_cd)

        fits.writeto(output_file, data, header, clobber=True, output_verify='warn')
        
    if(solve_field == True and update_ra_dec==True):	
        logme("Updating OBJRA and OBJDEC FITS header fields...")
        #extra field center RA and DEC
        #Field center: (RA H:M:S, Dec D:M:S) = (23:46:47.050, -16:24:36.684).
        match = re.search('Field center\: \(RA H\:M:S\, Dec D\:M\:S\) \= \(([0-9\:\-\.\s]+)\,([0-9\:\-\.\s]+)\)\.', output)
        if match:
            centerRA=match.group(1).strip()
            centerDEC=match.group(2).strip()
            data, header = fits.getdata(output_file, header=True)
            header.set('OBJRA',centerRA)
            header.set('OBJDEC',centerDEC)
            fits.writeto(output_file, data, header, clobber=True, output_verify='warn')			
            #print centerRA
            #print centerDEC
        else:
            logme("Error. Field center RA/DEC not found in solve-field output!")
            quit()
 
if(add_date_to_fname == True):
    dt = dt_start+(dt_end-dt_start)/2
    session = dt.strftime("%Y-%m-%dT%Hh%Mm%Ss")
    session = "session.%02d.%s"%(session_count,session)
    try:
        os.mkdir(output_path+session)
    except:
        pass
    os.system("mv %s*.fits %s%s"%(output_path,output_path,session))
    logme("Created new session. Moved .fits files to %s%s."%(output_path,session))

log.close()    