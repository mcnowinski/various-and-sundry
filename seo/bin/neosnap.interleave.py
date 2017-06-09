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
from astropy.coordinates import SkyCoord, EarthLocation, AltAz, Angle, get_sun
from astropy.time import Time

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
        while True:
            slackalert('Slit is closed.')
            time.sleep(20)
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
target = '2014 JO25'
#the observatory
observatory = 'G52' #seo
#exposure time in seconds
t_exposure = 30
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
#min elevation (degrees)
min_elevation=25

#wait time between primary target exposures?
interleave_delay_s=300
#primary target
primary_name='V343 UMA'
primary_ra='10:45:54.61'
primary_dec='52:16:26.4'
primary_t_exposure = 25
#filter
primary_filters = ['g-band', 'r-band']
#binning
primary_bin=2
primary_user='epjmm15'

#observatory location
#seo
observatory_code = 'G52'
observatory_lat = 38.259 #deg
observatory_lon = -122.440 #deg
observatory_elev = 63.8 #m
object_observer = EarthLocation(lat=observatory_lat*u.deg, lon=observatory_lon*u.deg, height=observatory_elev*u.m) 

#configure logging
log_file='/home/mcnowinski/var/log/seo.log'
logger = logging.getLogger('neosnap.interleave')
logger.setLevel(logging.DEBUG)
handler = RotatingFileHandler(log_file, maxBytes=5*1024*1024, backupCount=10)
handler.setFormatter(logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s'))
logger.addHandler(handler)
logger.info('Starting neosnap...')

count = 0
primary_count = 0
while True:
    #
    #observe primary target (celestial)
    #
    c = SkyCoord(Angle(primary_ra + ' hours').degree, Angle(primary_dec + ' degrees').degree, unit="deg")
    #print object, c
    #transform RA,DEC to alt, az for this object from the observatory
    object_observer_now = Time(datetime.datetime.utcnow(), scale='utc') # + object_observer_utc_offset 
    altaz = c.transform_to(AltAz(obstime=object_observer_now, location=object_observer))
    if(altaz >= min_elevation):
        slackdebug('Pointing telescope to %s (RA=%s, DEC=%s, AZ=%s, ALT=%s)...'%(primary_name, primary_ra, primary_dec, altaz.az.degree, altaz.alt.degree))
        (output, error, pid) = runSubprocess(['pinpoint','%s'%primary_ra, '%s'%primary_dec])
        #check the current telescope position
        (output, error, pid) = runSubprocess(['tx','where'])
        
        checkSlit()
        checkClouds(max_clouds) 

        #get image(s)
        for primary_filter in primary_filters:
            fits = '%s_%s_%dsec_bin%d_%s_%s_num%d_seo.fits'%(primary_name.replace(' ',''), primary_filter, primary_t_exposure, primary_bin, primary_user, datetime.datetime.utcnow().strftime('%Y%b%d_%Hh%Mm%Ss'), primary_count)
            slackdebug('Taking image (%s)...'%(fits))
            #(output, error, pid) = runSubprocess(['image','dark','time=%d'%t_exposure,'bin=%d'%bin, 'outfile=%s'%fits])
            (output, error, pid) = runSubprocess(['pfilter','%s'%primary_filter])
            (output, error, pid) = runSubprocess(['image','time=%d'%primary_t_exposure,'bin=%d'%primary_bin, 'outfile=%s'%fits])    
            if not error:
                slackdebug('Got image (%s).'%fits)
                slackpreview(fits)        
            else:
                slackdebug('Error. Image command failed (%s).'%fits)

        primary_count += 1

    else:
        slackdebug('The target (%s) is too low in the sky (ALT=%s deg)'%(primary_name, altaz.alt.degree))
        
    #
    #now look at secondary target (asteroid)
    #
    #where is the asteroid going to be at the *midpoint* of the exposure?
    #first, calculate the *time* of the *midpoint* of the exposure: t = now + t_pointing (in sec) + t_exposure/2 (in sec)
    #second, calculate the RA/DEC of the target at *that* time using JPL Horizons (via callhorizons)
    start = datetime.datetime.utcnow()
    start += datetime.timedelta(seconds=t_pointing)
    start += datetime.timedelta(seconds=t_exposure/2.0)

    #JPL Horizons requires start *and* end times where end > start (by at least 1 minute!)
    end = start + datetime.timedelta(seconds=60)
    #print start, end

    #get ephemerides for target in JPL Horizons from start to end times
    #assume it is a small body, e.g. asteroid or comet!
    #slackdebug('Calculating position of target (%s) at %s...'%(target, start.strftime("%Y/%m/%d %H:%M:%S")))
    ch=callhorizons.query(target.upper(), smallbody=True)
    ch.set_epochrange(start.strftime("%Y/%m/%d %H:%M:%S"), end.strftime("%Y/%m/%d %H:%M:%S"), '1m')
    ch.get_ephemerides(observatory)

    #check results
    if len(ch) == 2:
        logger.debug('name=%s,dt=%s,RA=%s,DEC=%s,EL=%s,AZ=%s'%(ch['targetname'][0], ch['datetime'][0], ch['RA'][0], ch['DEC'][0], ch['EL'][0], ch['AZ'][0]))
    else:
        logger.error('Error. Could not obtain ephemerides for target (%s).'%target)
        os.sys.exit(1)
        
    #ensure tracking is on, tx track on
    (output, error, pid) = runSubprocess(['tx','track','on'])

    #main loop
    #keep elevation above 25 deg
    if float(ch['EL'][0]) >= min_elevation:
        count += 1

        #convert ra,dec from decimal degrees to hms and dms
        name=ch['targetname'][0]
        ra=Angle(float(ch['RA'][0]), u.degree).to_string(unit=u.hour, sep=':')
        dec=Angle(float(ch['DEC'][0]), u.degree).to_string(unit=u.degree, sep=':')
        logger.debug('RA=%s,DEC=%s'%(ra,dec))
        slackdebug('Pointing telescope to %s (RA=%s, DEC=%s, AZ=%s, ALT=%s)...'%(name, ra, dec, ch['AZ'][0], ch['EL'][0]))
        
        #point the telescope
        #(output, error, pid) = runSubprocess(['tx','point','ra=%s'%ra, 'dec=%s'%dec])
        #pinpoint the telescope
        start_pointing = datetime.datetime.utcnow()
        (output, error, pid) = runSubprocess(['pinpoint','%s'%ra, '%s'%dec])
        end_pointing = datetime.datetime.utcnow()
        #calculate pointing time in seconds
        dt_pointing = (end_pointing-start_pointing).total_seconds()
        logger.info('Pinpointing telescope required %d seconds.'%dt_pointing)
        #check the current telescope position
        (output, error, pid) = runSubprocess(['tx','where'])
        
        checkSlit()
        checkClouds(max_clouds)

        #get image
        fits = '%s_%s_%dsec_bin%d_%s_%s_num%d_seo.fits'%(name.replace(' ',''), filter, t_exposure, bin, user, datetime.datetime.utcnow().strftime('%Y%b%d_%Hh%Mm%Ss'), count)
        slackdebug('Taking image (%s)...'%(fits))
        #(output, error, pid) = runSubprocess(['image','dark','time=%d'%t_exposure,'bin=%d'%bin, 'outfile=%s'%fits])
        (output, error, pid) = runSubprocess(['pfilter','%s'%filter])
        (output, error, pid) = runSubprocess(['image','time=%d'%t_exposure,'bin=%d'%bin, 'outfile=%s'%fits])    
        if not error:
            slackdebug('Got image (%s).'%fits)
            slackpreview(fits)        
        else:
            slackdebug('Error. Image command failed (%s).'%fits) 
        #time.sleep(t_exposure+5)
        
        ##calc new position
        #start = datetime.datetime.utcnow()
        #start += datetime.timedelta(seconds=t_pointing)
        #start += datetime.timedelta(seconds=t_exposure/2.0)
        #end = start + datetime.timedelta(seconds=60)
        #slackdebug('Calculating new position for %s (%s)...'%(name, start.strftime("%Y/%m/%d %H:%M:%S")))
        #ch.set_epochrange(start.strftime("%Y/%m/%d %H:%M:%S"), end.strftime("%Y/%m/%d %H:%M:%S"), '1m')
        #ch.get_ephemerides(observatory)
        #logger.debug('name=%s,dt=%s,RA=%s,DEC=%s,EL=%s,AZ=%s'%(name, ch['datetime'][0], ch['RA'][0], ch['DEC'][0], ch['EL'][0], ch['AZ'][0]))
    else:
        slackdebug('The target (%s) is too low in the sky (ALT=%s deg)'%(ch['targetname'][0], ch['EL'][0]))
        slackdebug('Pausing for %s seconds...'%(interleave_delay_s)) 
        time.sleep(interleave_delay_s)
        
logger.info('Stopping neosnap...')    
    