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

#get a list of all FITS files in the input directory	
im=glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
#loop through all qualifying files and perform plate-solving
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
	print("\nRenaming %s"%(new))
	#pull out RA/DEC from the FITS header, if they exist
	d1=fits.open('%s'%(new))
	d1.close()
	h1=d1[0].header
	try:
		date_obs=h1['DATE-OBS']
	except KeyError:
		print "Error! Observation date/time not found in FITS header for %s."%(new)
		quit()
	
	date_obs = date_obs.replace(":","_")
	date_obs = date_obs.replace("-","_")
	date_obs = "." + date_obs
	#create renamed FITS file
	output_file = "%s"%(new.rsplit('.',1)[0])+date_obs+".fits"
	output_file = output_file.rsplit('/',1)[1]
	output_file = output_path+output_file
	print("Writing renamed file to "+output_file)
	os.system("mv %s "%(new)+output_file)