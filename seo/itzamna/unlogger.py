import os
import glob
import datetime
import re
from collections import defaultdict
import matplotlib.pyplot as plt
import matplotlib.dates as dates
from matplotlib.dates import date2num
from dateutil import parser

#track clouds
clouds = []

#
#this script parses aster log files, e.g. tserver.log
#

#regular expressions to identify aster log messages
re_doCCD = '^ccd time=(\S+) nbytes=[0-9]+ bin=([0-9]+) bzero=[0-9\\-]+\s?(dark)?'
re_doLock = '^done lock user=(\S+) email=(\S+)'
re_doSlit = '^slit (open|close)'
re_doPoint = '^point ra=(\S+) dec=(\S+)'
re_doWeather = '^done taux ovolts=[\S]+ irvolts=[\S]+ cloud=([0-9\\-\\+\\.]+) rain=(0|1) dew=(\S+)'
re_doWhere = '^done where ra=([\S]+) dec=([\S]+) equinox=[\S]+ ha=([\S]+) secz=([\S]+) alt=([\S]+) az=([\S]+)'

def doWhere(msg, dt):
    match = re.search(re_doWhere, msg)
    if match:
        ra=match.group(1)
        dec=match.group(2)
        ha=match.group(3)
        secz=match.group(4)
        alt=match.group(5)
        az=match.group(6)
        #print 'Telescope was pointed to RA=%s, DEC=%s, ha=%s, secz=%s, alt=%s, az=%s.'%(match.group(1), match.group(2), match.group(3), match.group(4), match.group(5), match.group(6)) 
 
#re_doWeather = '^done taux ovolts=[\S]+ irvolts=[\S]+ cloud=([0-9\\-\\+\\.]+) rain=(0|1) dew=(\S+)'        
def doWeather(msg, dt):
    match = re.search(re_doWeather, msg)
    if match:
        #print match.group(1)
        cloudiness = int(float(match.group(1))*100)
        #set negative cloudiness to zero
        if cloudiness < 0:
            cloudiness = 0
        #print 'Clouds=%s, rain=%s, dew=%s.'%(match.group(1), match.group(2), match.group(3))
        clouds.append((dt, cloudiness))         

def doPoint(msg, dt):
    match = re.search(re_doPoint, msg)
    if match:
        ra = match.group(1)
        dec = match.group(2)
        #print 'Telescope was pointed to RA=%s, DEC=%s.'%(match.group(1), match.group(2))         

def doSlit(msg, dt):
    match = re.search(re_doSlit, msg)
    if match:
        slit_status = match.group(1)
        #if match.group(1) == 'open':
        #    print 'Slit was opened at %s.'%dt.strftime("%Y-%m-%d %H:%M:%S")
        #else:
        #    print 'Slit was closed at %s.'%dt.strftime("%Y-%m-%d %H:%M:%S")            

def doLock(msg, dt):
    match = re.search(re_doLock, msg)
    if match:
        user=match.group(1)
        email=match.group(2)
        #print 'User=%s. Email=%s.'%(match.group(1), match.group(2))                

def doCCD(msg, dt):
    match = re.search(re_doCCD, msg)
    if match:
        exposure=match.group(1)
        binning=match.group(2)
    #if match:
    #    #print msg
    #    if match.group(3):
    #        print 'Exposure time =\t%s sec. Binning = %s. Dark frame.'%(match.group(1), match.group(2))              
    #    else:
    #        print 'Exposure time =\t%s sec. Binning = %s. Light frame.'%(match.group(1), match.group(2))

def parse_message(msg, dt):
    match = False
    for msg_type in msg_types:
        #match the command first, then look for
        match = re.search(msg_type[0], msg)
        if match: #compare with list of known msgs
            #call associated function
            msg_type[1](msg, dt)
            break;
    #if not match: #did not recognize this command
    #    logme('Warning. Unrecognized message (%s).'%msg)

#valid message types
#element 1 is a regular expression describing the message (and its parameters)
#element 2 is the function called when this message is found
msg_types = [

#ccd time=2.310 nbytes=2097152 bin=2 bzero=32768
[re_doCCD, doCCD],
#done lock user=mcnowinski email=mcnowinski@gmail.com phone=7032869140 comment=fatflats timestamp=2017-02-28T02:02:23Z
[re_doLock, doLock],
#slit close
#slit open
[re_doSlit, doSlit],
#point ra=3:19:48.16 dec=41:30:42.103 equinox=2000.0
[re_doPoint, doPoint],
#done taux ovolts=2.867 irvolts=0.150 cloud=0.25 rain=0 dew=2.89
[re_doWeather, doWeather],
#done where ra=16:48:02.19 dec=-01:53:50.7 equinox=2017.166 ha=-35.853 secz=1.63 alt=38.0 az=132.0 slewing=0
[re_doWhere, doWhere],

]

#log the unlogger
def logme( str ):
   log.write(str + "\n")
   print str
   return 

#path to log files
input_path = './'
#mask for log files
input_mask = 'tserver.*'
#log file name
log_fname = 'log.unlogger.txt'

log=open(log_fname, 'w')

log_files=glob.glob(input_path+input_mask)
for log_file in log_files:
    try:
        lf=open(log_file, 'r')
    except:
        logme('Error. Failed to open log file (%s).'%log_file)
        continue
    lines=lf.readlines()
    for line in lines:
        #logme(line)
        #grab only lines with a timestamp (for now)
        match = re.search('^([0-9]{4}\\-[0-9]{2}\\-[0-9]{2}T[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}Z)', line) 
        #if it's a match, grab the datetimestamp and the log message   
        if match:
            #logme(match.group(1))
            dt = parser.parse(match.group(1))
            msg = line[len(match.group(1)):len(line)].strip()
            #logme('%s\t%s'%(dt.strftime("%Y-%m-%d %H:%M:%S"),msg))
            #now let's parse the msg!
            #
            parse_message(msg, dt)
                
    lf.close()    

#print clouds
x = [date2num(date) for (date, cloudiness) in clouds]
y = [cloudiness for (date, cloudiness) in clouds]

fig = plt.figure()
graph = fig.add_subplot(111)
# Plot the data as a red line with round markers
#graph.plot(x,y,'bo',markersize=1)
graph.bar(x, y, width=0.05, color='b')
# Set the xtick locations to correspond to just the dates you entered.
graph.set_xticks(x)
# Set the xtick labels to correspond to just the dates you entered.
graph.set_xticklabels(
        [date.strftime("%m-%d") for (date, cloudiness) in clouds]
        )

x_major_lct = dates.AutoDateLocator(minticks=2, maxticks=10, interval_multiples=True)
#x_minor_lct = dates.HourLocator(byhour = range(0,25,1))
x_fmt = dates.AutoDateFormatter(x_major_lct)
graph.xaxis.set_major_locator(x_major_lct)
#graph.xaxis.set_minor_locator(x_minor_lct)
graph.xaxis.set_major_formatter(x_fmt)

plt.show()

log.close()



