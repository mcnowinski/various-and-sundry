import callhorizons
import datetime
import os
import subprocess
import time
from astropy import units as u
from astropy.coordinates import Angle
import logging
from logging.handlers import RotatingFileHandler
import unicodedata
import string
import re

#make sure filename is valid
def cleanFilename(filename):
    validFilenameChars = "-_.() %s%s" % (string.ascii_letters, string.digits)
    normalizedFilename = unicodedata.normalize('NFKD', filename).encode('ASCII', 'ignore')
    cleanFilename = ''.join(c for c in normalizedFilename if c in validFilenameChars)
    #replace spaces with underscore
    cleanFilename = cleanFilename.replace(' ', '_').lower()
    return cleanFilename

#run external process; track output, errors, and pid
def runSubprocess(command_array):
    #command array is array with command and all required parameters
    try:
        sp = subprocess.Popen(command_array, stderr=subprocess.PIPE, stdout=subprocess.PIPE)
        logger.info('Running subprocess ("%s" %s)...'%(' '.join(command_array), sp.pid))
        output, error = sp.communicate()
        logger.debug(output)
        if error:
            logger.error(error)
        return (output, error, sp.pid)
    except:
        logger.error('Error. Subprocess ("%s" %d) failed.'%(' '.join(command_array)))
        return ('', '', 0)

#send alert message to slack        
def slackdebugalert(msg):
    msg = datetime.datetime.utcnow().strftime('%m-%d-%Y %H:%M:%S ') + msg
    (output, error, pid) = runSubprocess(['slackalert',msg])
        
#send debug message to slack        
def slackdebug(msg):
    msg = datetime.datetime.utcnow().strftime('%m-%d-%Y %H:%M:%S ') + msg
    (output, error, pid) = runSubprocess(['slackdebug',msg])

#send preview of fits image to Slack    
def slackpreview(fits):
    (output, error, pid) = runSubprocess(['stiffy',fits,'image.tif'])
    (output, error, pid) = runSubprocess(['convert','-resize','50%','-normalize','-quality','75','image.tif','image.jpg'])
    (output, error, pid) = runSubprocess(['slackpreview','image.jpg', fits])
    (output, error, pid) = runSubprocess(['rm','image.jpg', 'image.tif'])

#check slit
def checkSlit():
    (output, error, pid) = runSubprocess(['tx','slit'])
    match = re.search('slit=open', output)
    if match:
        logger.debug('Slit is open.')
        return True
    else:
        logger.debug('Slit has unexpectedly closed.')
        slackalert('Slit is closed.')
        return False
 
#check clouds 
def checkClouds(max_clouds):
    (output, error, pid) = runSubprocess(['tx','taux'])
    match = re.search('cloud=([\\-\\.0-9]+)', output)
    if match:
        clouds = float(match.group(1))
        logger.debug('Cloud cover is %d%%.'%int(clouds*100))
    if clouds >= max_clouds:
        slackalert('Too many clouds (%d%%).'%int(clouds*100))
        return True
    else:
        return False

#
# main
#    
 
# 
#CHANGE AS NEEDED!
#Some should eventually move to command line parameters or a config file... 
#
#the target
target = '2014JO25'
#the observatory
observatory = 'G52' #seo
#exposure time in seconds
t_exposure = 15
#filter
filter = 'clear'
#binning
bin=2
#time alloted to perform telescope (pin)pointing in seconds
t_pointing = 40
#user, hardcode for now
user='mcnowinski'
#max. cloud cover, 0-1
max_clouds = 0.4

#configure logging
log_file='/home/mcnowinski/var/log/seo.log'
logger = logging.getLogger('neosnap')
logger.setLevel(logging.DEBUG)
handler = RotatingFileHandler(log_file, maxBytes=5*1024*1024, backupCount=10)
handler.setFormatter(logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s'))
logger.addHandler(handler)
logger.info('Starting neosnap...')

f=open('/home/mcnowinski/giterdone/seo/bin/coords.txt', 'r')
coords = f.readlines()
#print len(coords)
#os.sys.exit(1)

#where is the asteroid going to be at the *midpoint* of the exposure?
#first, calculate the *time* of the *midpoint* of the exposure: t = now + t_pointing (in sec) + t_exposure/2 (in sec)
#second, calculate the RA/DEC of the target at *that* time using JPL Horizons (via callhorizons)
start = datetime.datetime.utcnow()
start += datetime.timedelta(seconds=t_pointing)
start += datetime.timedelta(seconds=t_exposure/2.0)

midnight = start.replace(hour=0, minute=0, second=0, microsecond=0)

minutes = int((start-midnight).total_seconds()/60)
#print minutes
#os.sys.exit(1)

#JPL Horizons requires start *and* end times where end > start (by at least 1 minute!)
#end = start + datetime.timedelta(seconds=60)
#print start, end

#get ephemerides for target in JPL Horizons from start to end times
#assume it is a small body, e.g. asteroid or comet!
#slackdebug('Calculating position of target (%s) at %s...'%(target, start.strftime("%Y/%m/%d %H:%M:%S")))
#ch=callhorizons.query(target.upper(), smallbody=True)
#ch.set_epochrange(start.strftime("%Y/%m/%d %H:%M:%S"), end.strftime("%Y/%m/%d %H:%M:%S"), '1m')
#ch.get_ephemerides(observatory)

coord = coords[minutes].split('\t')
#print coord
#os.sys.exit(1)
dt=coord[0].strip()
ra=coord[1].strip()
dec=coord[2].strip()
#print dt, ra, dec
#os.sys.exit(1)

##check results
#if len(ch) == 2:
#    logger.debug('name=%s,dt=%s,RA=%s,DEC=%s,EL=%s,AZ=%s'%(target, coords, ch['RA'][0], ch['DEC'][0], ch['EL'][0], ch['AZ'][0]))
#else:
#    logger.error('Error. Could not obtain ephemerides for target (%s).'%target)
#    os.sys.exit(1)

logger.debug('name=%s,dt=%s,RA=%s,DEC=%s'%(target, dt, ra, dec))
    
#ensure tracking is on, tx track on
(output, error, pid) = runSubprocess(['tx','track','on'])

#main loop
#keep elevation above 25 deg
count = 0
while 1:
    count += 1

    #convert ra,dec from decimal degrees to hms and dms
    name=target
    #ra=Angle(float(ch['RA'][0]), u.degree).to_string(unit=u.hour, sep=':')
    #dec=Angle(float(ch['DEC'][0]), u.degree).to_string(unit=u.degree, sep=':')
    logger.debug('RA=%s,DEC=%s'%(ra,dec))
    slackdebug('Pointing telescope to %s (RA=%s, DEC=%s)...'%(name, ra, dec))
    
    #point the telescope
    #(output, error, pid) = runSubprocess(['tx','point','ra=%s'%ra, 'dec=%s'%dec])
    #pinpoint the telescope
    start_pointing = datetime.datetime.utcnow()
    (output, error, pid) = runSubprocess(['pinpoint','%s'%ra,'%s'%dec])
    end_pointing = datetime.datetime.utcnow()
    #calculate pointing time in seconds
    dt_pointing = (end_pointing-start_pointing).total_seconds()
    logger.info('Pinpointing telescope required %d seconds.'%dt_pointing)
    #check the current telescope position
    (output, error, pid) = runSubprocess(['tx','where'])
    
    checkSlit()
    checkClouds(max_clouds)

    #get image
    fits = '%s_%s_%dsec_bin%d_%s_%s_num%d_seo.fits'%(name, filter, t_exposure, bin, user, datetime.datetime.utcnow().strftime('%Y%b%d_%Hh%Mm%Ss'), count)
    slackdebug('Taking image (%s)...'%(fits))
    #(output, error, pid) = runSubprocess(['image','dark','time=%d'%t_exposure,'bin=%d'%bin, 'outfile=%s'%fits])
    (output, error, pid) = runSubprocess(['image','time=%d'%t_exposure,'bin=%d'%bin, 'outfile=%s'%fits])    
    if not error:
        slackdebug('Got image (%s).'%fits)
        slackpreview(fits)        
    else:
        slackdebug('Error. Image command failed (%s).'%fits) 
    #time.sleep(t_exposure+5)
    
    start = datetime.datetime.utcnow()
    start += datetime.timedelta(seconds=t_pointing)
    start += datetime.timedelta(seconds=t_exposure/2.0)

    midnight = start.replace(hour=0, minute=0, second=0, microsecond=0)

    minutes = int((start-midnight).total_seconds()/60)

    coord = coords[minutes].split('\t')
    dt=coord[0].strip()
    ra=coord[1].strip()
    dec=coord[2].strip()
    
    slackdebug('Calculating new position for %s (%s)...'%(name, start.strftime("%Y/%m/%d %H:%M:%S")))
    logger.debug('name=%s,dt=%s,RA=%s,DEC=%s'%(name, dt, ra, dec))
    
logger.info('Stopping neosnap...')    
    