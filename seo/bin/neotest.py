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
def slitIsOpen():
    (output, error, pid) = runSubprocess(['tx','slit'])
    match = re.search('slit=open', output)
    if match:
        logger.debug('Slit is open.')
        return True
    else:
        logger.debug('Slit is closed.')
        return False
 
#check clouds 
def tooCloudy(max_clouds):
    (output, error, pid) = runSubprocess(['tx','taux'])
    match = re.search('cloud=([\\-\\.0-9]+)', output)
    if match:
        clouds = float(match.group(1))
        logger.debug('Cloud cover is %d%%.'%int(clouds*100))
    return (clouds >= max_clouds)

#
# main
#    
 
# 
#CHANGE AS NEEDED!
#Some should eventually move to command line parameters or a config file... 
#
#the target
target = '22'
#the observatory
observatory = 'G52' #seo
#exposure time in seconds
t_exposure = 30
#filter
filter = 'clear'
#binning
bin=2
#time alloted to perform telescope (pin)pointing in seconds
t_pointing = 30
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

print slitIsOpen()

print tooCloudy(max_clouds)    
    