import callhorizons2
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
import pytz
import time

#the datetimes
#Aug.  7 Mo  02:30 06:15         X  023 Nowinski             SpeX/MORIS           SJB First Night
#Oct.  9 Mo  00:05 05:55         X  023 Nowinski             SpeX/MORIS           SJB 
#Nov. 25 Sa  00:35 05:55         X  023 Nowinski             SpeX/MORIS           SJB
#Nov. 26 Su  18:35 01:05         X  023 Nowinski             SpeX/MORIS           SJB 
#Dec. 29 Fr  18:45 23:20         X  023 Nowinski             SpeX/MORIS           ACB
#convert to UTC (+10)
dts = [ [datetime.datetime(2017, 8, 7, 12, 30, 0), datetime.datetime(2017, 8, 7, 16, 15, 0)],
        [datetime.datetime(2017, 10, 9, 10, 5, 0), datetime.datetime(2017, 10, 9, 15, 55, 0)],
        [datetime.datetime(2017, 11, 25, 10, 35, 0), datetime.datetime(2017, 11, 25, 15, 55, 0)],
        [datetime.datetime(2017, 11, 27, 4, 35, 0), datetime.datetime(2017, 11, 27, 11, 5, 0)],
        [datetime.datetime(2017, 12, 30, 4, 45, 0), datetime.datetime(2017, 12, 30, 9, 20, 0)]       
      ]

fname_targets = 'targets2.txt'
fname_observables = 'observables2.txt'

#the target using spkid
#targets = ['2009633']
targets = open(fname_targets, "r").readlines()

#output file
f = open(fname_observables, "w")
f.write("spkid,name,minutes\n")
f.close()

#the observatory
observatory = '568'

#min elevation in degrees
min_elev = 30;

#min minutes observable per night
min_minutes = 90;

#faintest apparent magnitude of target
faintest_apmag = 18;

#get ephemerides for targets in JPL Horizons from start to end times
#for spkid, use DES=spkid; for target name
#loop thru all targets
for target in targets:
    days_available = 0
    total_minutes = 0
    ch=callhorizons2.query('DES='+target.strip()+";", smallbody=True)
    print 'Processing %s...'%(target.strip())
    #f = open(fname_observables, "a")	
    #f.write('#Processing %s...\n'%(target.strip()))
    #f.close()
    #loop thru all datetimes
    count = 0
    minutes = [0] * len(dts)
    phase = [0.0] * len(dts)
    startdt = [''] * len(dts)
    enddt = [''] * len(dts)
    apmag = [0.0] * len(dts)    
    glxlat = [''] * len(dts)
    for dt in dts:
        #get ephemerides for specified observation windows in 1m increments
        ch.set_epochrange(dt[0].strftime("%Y/%m/%d %H:%M:%S"), dt[1].strftime("%Y/%m/%d %H:%M:%S"), '1m')
        ch.get_ephemerides(observatory)
        #count minutes where targets is at an elevation > min_elev
        for i in range(0,len(ch)):
            if ch['EL'][i] >= min_elev and ch['V'][i] <= faintest_apmag:
                minutes[count] += 1
        #if there is an hour of observing time for this target, mark it!
        if minutes >= min_minutes:
            startdt[count] = pytz.utc.localize(dt[0]).astimezone(pytz.timezone("US/Hawaii"))
            enddt[count] = pytz.utc.localize(dt[1]).astimezone(pytz.timezone("US/Hawaii"))
            print '%s to %s'%(startdt[count].strftime("%Y/%m/%d %H:%M:%S"), enddt[count].strftime("%Y/%m/%d %H:%M:%S"))
            days_available += 1
            total_minutes += minutes[count]
            phase[count] = ch['alpha'][0]
	    apmag[count] = ch['V'][0]
	    glxlat[count] = ch['GlxLat'][0]
            print 'Phase = %f deg'%(phase[count])
            print 'Minutes = %d min'%minutes[count]
	    print 'ApMag = %f'%(apmag[count])
	    print 'GlxLat = %s'%(glxlat[count])
        count += 1
        ##give the server a little rest
        #time.sleep(1)
    #is it available for each of the observation windows? record it!
    print '%s is observable for %d of the %d available sessions.'%(ch['targetname'][0], days_available, len(dts))
    if days_available == len(dts):
        print '\tspkid=%s name=%s avemin=%f'%(target.strip(), ch['targetname'][0], total_minutes/len(dts))
        f = open(fname_observables, "a")
        f.write('"%s","%s",%f\n'%(target.strip(), ch['targetname'][0], total_minutes/len(dts)))
        f.close()
