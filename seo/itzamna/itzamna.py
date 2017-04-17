#Web and Real-Time Messaging interface to Slack for use with the SEO telescope, a.k.a. itzamna

from slackclient import SlackClient
import time
import math
import os
import sys
import datetime
import json
import re
import urllib2
import requests
import subprocess
from astropy.coordinates import SkyCoord, EarthLocation, AltAz, Angle, get_sun
import astropy.units as u
from astropy.time import Time
#import callhorizons
#use custom callhorizons
import ch #callhorizons module edited by me
import numpy
import ephem #calculate satellite (natural and artifical) ephemerides
from astroquery.simbad import Simbad
#for plots
import matplotlib
matplotlib.use('Agg') #don't need display
import matplotlib.pyplot as plt
from astropy.visualization import astropy_mpl_style
plt.style.use(astropy_mpl_style)

#python timezones
import pytz

def getStats(command, user):
    logme('Retrieving telescope statistics...')  

    send_message('Retrieving telescope statistics...')
    send_message('This may take several minutes. Remember, Tikal was not built in a day!')    
    
    (output, error, pid) = runSubprocess(['/home/mcnowinski/anaconda2/bin/python','/home/mcnowinski/giterdone/seo/itzamna/unlogger.py'])    

    stats=open('/home/mcnowinski/giterdone/seo/itzamna/stats.txt', 'w')
    stats.write(output)
    stats.close()
    
    send_file('/home/mcnowinski/giterdone/seo/itzamna/stats.txt', 'Metric')      
    send_file('/home/mcnowinski/giterdone/seo/itzamna/cloud.png', 'Clouds')    
    send_file('/home/mcnowinski/giterdone/seo/itzamna/pointing.png', 'Pointing')  


def runSubprocess(command_array):
    #command array is array with command and all required parameters
    try:
        sp = subprocess.Popen(command_array, stderr=subprocess.PIPE, stdout=subprocess.PIPE)
        logme('Running subprocess ("%s" %s)...'%(' '.join(command_array), sp.pid))
        output, error = sp.communicate()    
        return (output, error, sp.pid)
    except:
        logme('Error. Subprocess ("%s" %d) failed.'%(' '.join(command_array)))
        return ('', '', 0)

def doTest(command, user):
    (output, error, pid) = runSubprocess(['date','-u'])
    send_message(output)
    (output, error, pid) = runSubprocess(['tx','taux'])
    send_message(output)
    (output, error, pid) = runSubprocess(['tx','slit'])
    send_message(output)
    (output, error, pid) = runSubprocess(['tx','lock']) 
    send_message(output)
    (output, error, pid) = runSubprocess(['tx','lamps']) 
    send_message(output)
    (output, error, pid) = runSubprocess(['who']) 
    send_message(output)
    (output, error, pid) = runSubprocess(['tx','dome']) 
    send_message(output) 
    (output, error, pid) = runSubprocess(['tx','track']) 
    send_message(output)    
    (output, error, pid) = runSubprocess(['tx','mets']) 
    send_message(output) 
    #send_message("", [{"fields": [{"title": "Priority","value": "<http://i.imgur.com/nwo13SM.png|test>","short": True},{"title": "Priority","value": "Low","short": True}]}])    

def getObservability(command, user):
    match = re.search('^\\\\(observe)(\\s[0-9]+)?', command)
    if(match):
        if len(match.groups()) >= 3:
            object_index = int(match.group(2).strip())
        else:
            logme('No object index provided in \\observe command. Choosing first object...')
            object_index = 1
    else:
        logme('Error. Unexpected command format (%s).'%command)
        return
     
    #if we don't have any objects from \find, go home
    if len(objects) <= 0:
        send_message('Itzamna does not remember any objects. Please run `\\find <object>` to remind me.')
        return
    
    #if specified index does not match our object list, go home
    if object_index <= 0 or object_index > len(objects):
        send_message('Itzamna only remembers %d object(s):'%len(objects))
        report = ''
        index = 1
        #calculate local time of observatory    
        object_observer_now = Time(datetime.datetime.utcnow(), scale='utc') # + object_observer_utc_offset 
        for object in objects:
            #create SkyCoord instance from RA and DEC
            c = SkyCoord(object['RA'], object['DEC'], unit="deg")
            #transform RA,DEC to alt, az for this object from the observatory
            altaz = c.transform_to(AltAz(obstime=object_observer_now, location=object_observer))
            report += '%d.\t%s object (%s) found at RA=%s, DEC=%s, ALT=%f, AZ=%f.\n'%(index, object['type'], object['name'], object['RA'], object['DEC'], altaz.alt.degree, altaz.az.degree)
            index += 1
        send_message(report)
        return
            
    #select specific object from the find list
    object = objects[object_index-1]

    logme('Checking observability of %s (%d)...'%(object['name'],object_index))    
    
    #calculate local time of observatory    
    #observatory_utc_offset = int(datetime.datetime.now(pytz.timezone(observatory_tz)).strftime('%z'))/100.0*u.hour 
    observer_now = Time(datetime.datetime.utcnow(), scale='utc') # + observatory_utc_offset    
    
    #for celestial objects, this is easy peasy
    if object['type'] == 'Celestial':
        c = SkyCoord(object['RA'], object['DEC'], unit="deg")
        #look 24 hours into the future
        delta_now = numpy.linspace(0, 24, 1000)*u.hour
        times_now_to_tomorrow = observer_now + delta_now
        #prep to calc altaz
        frame_now_to_tomorrow = AltAz(obstime=times_now_to_tomorrow, location=object_observer)
        object_altaz_now_to_tomorrow = c.transform_to(frame_now_to_tomorrow)    
        sun_altaz_now_to_tomorrow = get_sun(times_now_to_tomorrow).transform_to(frame_now_to_tomorrow)
        #plt.plot(delta_now, sun_altaz_now_to_tomorrow.alt, color='r', label='Sun')
        plt.scatter(delta_now, object_altaz_now_to_tomorrow.alt,
                    c=object_altaz_now_to_tomorrow.az, label=object['name'], lw=0, s=8,
                    cmap='viridis')
        plt.fill_between(delta_now.to('hr').value, 0, 90,
                         sun_altaz_now_to_tomorrow.alt < -0*u.deg, color='0.5', zorder=0)
        plt.fill_between(delta_now.to('hr').value, 0, 90,
                         sun_altaz_now_to_tomorrow.alt < -18*u.deg, color='k', zorder=0)
        plt.colorbar().set_label('Azimuth [deg]')
        plt.legend(loc='upper left')
        plt.xlim(0, 24)
        plt.xticks(numpy.arange(13)*2)
        plt.ylim(0, 90)
        plt.xlabel('Hours [from now]')
        plt.ylabel('Altitude [deg]')
        #plt.show()     
        plt.savefig('observe.png', bbox_inches='tight') 
        plt.close()
        send_file('observe.png', 'Target (%s) Visibility'%object['name'])
    elif object['type'] == 'Solar System':
        send_message('Itzamna does not recognize the object type (%s).'%object['type'])
    elif object['type'] == 'Satellite':
        send_message('Itzamna does not recognize the object type (%s).'%object['type'])
    else:
        send_message('Itzamna does not recognize the object type (%s).'%object['type'])
    
def getObject(command, user):
    global objects
    match = re.search('^\\\\(find) ([a-zA-Z0-9\\s\\-\\+\\*]+)', command)
    if(match):
        lookup = match.group(2)
    else:
        logme('Error. Unexpected command format (%s).'%command)
        return  
        
    logme('Searching for object (%s)...'%lookup)
    objects = [] #all objects found
    #    
    #search solar system small bodies using JPL HORIZONS
    #
    #list of matches
    object_names = []
    #two passes, one for major (and maybe small) and one for (only) small bodies
    lookups = [lookup, lookup + ';']
    for repeat in range(0,2):
        #user JPL Horizons batch to find matches
        f = urllib2.urlopen('https://ssd.jpl.nasa.gov/horizons_batch.cgi?batch=l&COMMAND="%s"'%urllib2.quote(lookups[repeat].upper()))
        output = f.read() #the whole enchilada
        #print output
        lines = output.splitlines() #line by line
        #no matches? go home
        if re.search('No matches found', output):
            logme('No matches found in JPL Horizons for %s.'%lookups[repeat].upper())
        elif re.search('Target body name:', output):        
            #just one match?
            #if major body search (repeat = 0), ignore small body results
            if repeat == 0 and re.search('Small-body perts:', output):
                continue
            logme('Single match found in JPL Horizons for %s.'%lookups[repeat].upper())
            #user search term is unique, so use it!
            object_names.append(lookups[repeat].upper())
        elif repeat == 1 and re.search('Matching small-bodies', output):
            logme('Multiple small bodies found in JPL Horizons for %s.'%lookups[repeat].upper())
            #Matching small-bodies: 
            #
            #    Record #  Epoch-yr  Primary Desig  >MATCH NAME<
            #    --------  --------  -------------  -------------------------
            #          4             (undefined)     Vesta
            #      34366             2000 RP36       Rosavestal
            match_count = 0
            for line in lines:
                search_string = line.strip()
                #look for small body list
                match = re.search('^-?\\d+', search_string)
                #parse out the small body parameters
                if match:
                    match_count += 1
                    record_number = line[0:12].strip()
                    epoch_yr = line[12:22].strip()
                    primary_desig = line[22:37].strip()
                    match_name = line[37:len(line)].strip()
                    #print record_number, epoch_yr, primary_desig, match_name
                    #add semicolon for small body lookups
                    object_names.append(record_number + ';')                
            #check our parse job
            match = re.search('(\\d+) matches\\.', output)
            if match:
                if int(match.group(1)) != match_count:
                    logme('Multiple JPL small body parsing error!')
                else:
                    logme('Multiple JPL small body parsing successful!')
        elif repeat == 0 and re.search('Multiple major-bodies', output):
            logme('Multiple major bodies found in JPL Horizons for %s.'%lookups[repeat].upper())
            # Multiple major-bodies match string "50*"
            #
            #  ID#      Name                               Designation  IAU/aliases/other   
            #  -------  ---------------------------------- -----------  ------------------- 
            #      501  Io                                              JI                   
            #      502  Europa                                          JII  
            match_count = 0
            for line in lines:
                search_string = line.strip()
                #look for major body list
                match = re.search('^-?\\d+', search_string)
                #parse out the major body parameters
                if match:
                    match_count += 1
                    record_number = line[0:9].strip()
                    #negative major bodies are spacecraft,etc. Skip those!
                    if int(record_number) >= 0:
                        name = line[9:45].strip()
                        designation = line[45:57].strip()
                        other = line[57:len(line)].strip()
                        #print record_number, name, designation, other
                        #NO semicolon for major body lookups
                        object_names.append(record_number)                
            #check our parse job
            match = re.search('Number of matches =([\\s\\d]+).', output)
            if match:
                if int(match.group(1)) != match_count:
                    logme('Multiple JPL major body parsing error!')
                else:
                    logme('Multiple JPL major body parsing successful!')     
    #print object_names    
    start = datetime.datetime.utcnow()
    end = start+datetime.timedelta(seconds=60)
    for object_name in object_names:
        try:
            result=ch.query(object_name.upper(), smallbody=False)
            result.set_epochrange(start.strftime("%Y/%m/%d %H:%M"), end.strftime("%Y/%m/%d %H:%M"), '1m')
            result.get_ephemerides(observatory_code)
            objects.append({'type':'Solar System', 'id':object_name.upper(), 'name':result['targetname'][0], 'RA':result['RA'][0], 'DEC':result['DEC'][0]})
            #print 'targetid=%s'%result['targetid'][12]    
            #found_object = True
        except:
            pass
    #    
    #search satellites
    #
    for sat in norad_sat_db:
        if sat[0].find(lookup.upper()) >= 0:
            sat_name = sat[0]
            sat_tle_line1 = sat[1]
            sat_tle_line2 = sat[2]
            sat_ephem = ephem.readtle(sat_name, sat_tle_line1, sat_tle_line2)
            sat_observer.date = datetime.datetime.utcnow()
            sat_ephem.compute(sat_observer)
            sat_coords = SkyCoord(ra='%s'%sat_ephem.ra, dec='%s'%sat_ephem.dec, unit=(u.hour, u.deg))
            objects.append({'type':'Satellite', 'id':sat_name, 'name':sat_name, 'RA':math.degrees(float(repr(sat_ephem.ra))), 'DEC':math.degrees(float(repr(sat_ephem.dec)))})            
            #alt = math.degrees(float(repr(sat_ephem.alt)))
            #lat = math.degrees(float(repr(sat_ephem.sublat)))
            #lon = math.degrees(float(repr(sat_ephem.sublong)))
            #if(alt > 5):
            #    print '%s: %s %s %f %s %f %f'%(name, sat_ephem.ra, sat_ephem.dec, alt, sat_ephem.az, lat, lon)
            
    logme('Found %d objects.'%len(objects))
    #calc current alt and az, then send information to Slack
    if len(objects) > 0:
        report = ''
        index = 1
        #calculate local time of observatory    
        #object_observer_utc_offset = int(datetime.datetime.now(pytz.timezone(observatory_tz)).strftime('%z'))/100.0*u.hour 
        object_observer_now = Time(datetime.datetime.utcnow(), scale='utc') # + object_observer_utc_offset 
        #print object_observer_now
        for object in objects:
            #create SkyCoord instance from RA and DEC
            c = SkyCoord(object['RA'], object['DEC'], unit="deg")
            #print object, c
            #transform RA,DEC to alt, az for this object from the observatory
            altaz = c.transform_to(AltAz(obstime=object_observer_now, location=object_observer))
            #print altaz.az.degree, altaz.alt.degree
            report += '%d.\t%s object (%s) found at RA=%s, DEC=%s, ALT=%f, AZ=%f.\n'%(index, object['type'], object['name'], object['RA'], object['DEC'], altaz.alt.degree, altaz.az.degree)
            index += 1
        send_message(report)        
    else:
        send_message('Sorry, Itzamna knows all but *still* could not find "%s".'%lookup)          
    
def isLocked():
    logme('Checking to see if the telescope is locked...')
    
    locked_by = (None, None)
    (output, error, pid) = runSubprocess(['tx','lock'])
    #print output
    #done lock user=mcnowinski email=mcnowinski@gmail.com phone=7032869140 comment=slac timestamp=2017-02-10T20:32:03Z
    match = re.search('^done lock user=([a-zA-Z]+) email=([a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+)', output)
    if(match):
        locked_by = (match.group(1), match.group(2))
        logme('Telescope is currently locked by %s (%s).'%(match.group(1),match.group(2)))
    return locked_by
    
#tx lock user=mcnowinski email=mcnowinski@gmail.com phone=7032869140 comment=slack
def doLock(command, user):
    logme('Locking the telescope...')
    
    #check to make sure the telescope isn't already locked
    (username, email) = isLocked()
    if username:
        send_message('The telescope is already locked by %s (%s)!'%(username,email))
        return False
    
    username = user['name']
    email = user['profile']['email']
    #only grab numbers from phone number
    num = re.compile(r'[^\d]+')
    phone = num.sub('', user['profile']['phone'])
    comment = 'itzamna'
    timestamp = datetime.datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%SZ')
    
    (output, error, pid) = runSubprocess(['tx','lock','user=%s'%username,'email=%s'%email,'phone=%s'%phone,'comment=%s'%comment])
    match = re.search('^done lock user=([a-zA-Z]+) email=([a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+)', output)
    if(match):
        send_message('Telescope successfully locked!')   
    else:
        logme('Error. Telescope could not be locked (%s)'%output)
        send_message('Telescope could *not* be locked!')

#tx lock user=mcnowinski email=mcnowinski@gmail.com phone=7032869140 comment=slack
def doUnLock(command, user):
    logme('Unlocking the telescope...')
    
    #check to make sure the telescope is locked and if so, locked by us!
    (username,email) = isLocked()
    #is telescope locked?
    if not username:
        send_message("The telescope is not locked.")
        return False
    #is telescope locked by us?
    if username != user['name']:
        logme("Warning. Can't unlock the telescope. It's locked by someone else (%s)!"%locked_by)
        send_message("Can't unlock the telescope. The telescope is locked by %s!"%locked_by)
        return False
        
    #clear the lock
    (output, error, pid) = runSubprocess(['tx','lock','clear'])
    match = re.search('^done lock$', output)
    if(match):
        send_message('Telescope successfully unlocked!')   
    else:
        logme('Error. Telescope could not be unlocked (%s)'%output)
        send_message('Telescope could *not* be unlocked!')
     
def getSlit(command, user):
    logme('Retrieving dome slit status...')  
    
    (output, error, pid) = runSubprocess(['tx','slit'])    
    #done slit slit=closed
    match = re.search('^done slit slit=([a-zA-Z]+)', output)
    if(match):
        send_message('Slit is %s.'%(match.group(1)))
        send_message('\n')
    else:
        send_message('Error. Command (%s) did not return a valid response.'%command)
        logme('Error. Command (%s) did not return a valid response (%s).'%(command,output))
    
def getSun(command, user):
    logme('Retrieving current sun information...')      

    (output, error, pid) = runSubprocess(['sun'])
    #21:30:51.07 -14:42:54.0 2017.107 sun alt=35.8
    match = re.search('^([\\-\\+0-9\\:\\.]+) ([\\-\\+0-9\\:\\.]+) ([\\-\\+0-9\\.]+) sun alt=([\\-\\+0-9\\.]+)', output)
    if(match):
        send_message('Sun: RA=%s, DEC=%s, Alt=%s deg'%(match.group(1),match.group(2),match.group(4)))
        send_message('\n')
    else:
        send_message('Error. Command (%s) did not return a valid response.'%command)
        logme('Error. Command (%s) did not return a valid response (%s).'%(command,output))
        
def getMoon(command, user):
    logme('Retrieving current moon information...')     
    
    (output, error, pid) = runSubprocess(['moon'])
    #07:38:11.68 +17:30:06.9 2017.107 moon alt=-21.1 phase=0.85 lunation=1164
    match = re.search('^([\\-\\+0-9\\:\\.]+) ([\\-\\+0-9\\:\\.]+) ([\\-\\+0-9\\.]+) moon alt=([\\-\\+0-9\\.]+) phase=([\\-\\+0-9\\.]+) lunation=([\\-\\+0-9]+)', output)
    if(match):
        send_message('Moon: Phase=%d%%, RA=%s, DEC=%s, Alt=%s deg'%(int(float(match.group(5))*100.0), match.group(1),match.group(2),match.group(4)))
        send_message('\n')
    else:
        send_message('Error. Command (%s) did not return a valid response.'%command)
        logme('Error. Command (%s) did not return a valid response (%s).'%(command,output))
        
def getWhere(command, user):
    logme('Retrieving the current telescope pointing information...')      

    (output, error, pid) = runSubprocess(['tx','where'])
    #done where ra=05:25:25.11 dec=+38:17:17.0 equinox=2017.105 ha=0.010 secz=1.00 alt=90.0 az=265.1 slewing=0
    match = re.search('^done where ra=([\\-\\+0-9\\:\\.]+) dec=([\\-\\+0-9\\:\\.]+) equinox=([\\-\\+0-9\\.]+) ha=([\\-\\+0-9\\.]+) secz=([\\-\\+0-9\\.]+) alt=([\\-\\+0-9\\.]+) az=([\\-\\+0-9\\.]+) slewing=([\\-\\+0-9\\.]+)', output)
    if(match):
        send_message('Telescope Pointing:')
        send_message('>RA: %s'%match.group(1))
        send_message('>DEC: %s'%match.group(2))
        send_message('>Alt: %s'%match.group(6))        
        send_message('>Az: %s'%match.group(7))
        is_slewing = int(match.group(8))
        if is_slewing == 0:
          send_message('>Slewing? No')
        else:
          send_message('>Slewing? Yes')
        ra_decimal = Angle(match.group(1) + '  hours')
        dec_decimal = Angle(match.group(2) + '  degrees')
        #skymap_dot_org_image_url='http://server3.sky-map.org/imgcut?survey=SDSS&img_id=all&angle=0.9375&ra=%f&de=%f&width=400&height=400&projection=tan&interpolation=bicubic&jpeg_quality=0.8&output_type=jpeg'%(ra_decimal.hour,dec_decimal.degree)
        #url='http://server3.wikisky.org/map?custom=1&language=EN&type=PART&w=500&h=500&angle=5.0&ra=%f&de=%f&rotation=0.0&mag=8&max_stars=100000&zoom=10&borders=1&border_color=400000&show_grid=0&grid_color=404040&grid_color_zero=808080&grid_lines_width=1.0&grid_ra_step=1.0&grid_de_step=15.0&show_const_lines=0&constellation_lines_color=006000&constellation_lines_width=1.0&show_const_names=&constellation_names_color=006000&const_name_font_type=PLAIN&const_name_font_name=SanSerif&const_name_font_size=15&show_const_boundaries=&constellation_boundaries_color=000060&constellation_boundaries_width=1.0&background_color=000000&output=PNG'%(ra_decimal.hour,dec_decimal.degree)
        url='http://server3.sky-map.org/imgcut?survey=DSS2&img_id=all&angle=0.5&ra=%f&de=%f&width=400&height=400&projection=tan&interpolation=bicubic&jpeg_quality=0.8&output_type=jpeg'%(ra_decimal.hour,dec_decimal.degree)
        send_message("", [{"image_url":"%s"%url, "title":"Sky Position (DSS2):"}])
        send_message('\n')
    else:
        send_message('Error. Command (%s) did not return a valid response.'%command)
        logme('Error. Command (%s) did not return a valid response (%s).'%(command,output))
        
    
def doWelcome():
    logme('Sending welcome message to Slack users...')  

    send_message("", [{"image_url":"%s"%welcome_giphy_url, "title":"Itzamna is here! Let your wishes be known..."}])
    #show help
    getHelp('\\help')

    isLocked()    

#get ClearDarkSky chart
def getClearDarkSky(command, user):
    logme('Retrieving the current Clear Sky charts for SEO...')  

    send_message("", [{"image_url":"http://www.cleardarksky.com/c/SonomaCAcsk.gif?c=640834", "title":"Lake Sonoma Clear Sky Chart"}])
    send_message("", [{"image_url":"http://www.cleardarksky.com/c/SmnCAcsk.gif?c=640834", "title":"Sonoma Clear Sky Chart"}])
    send_message("\n")    
    
#get weather from Wunderground
def getForecast(command, user):
    logme('Retrieving the hourly forecast from wunderground.com...')  
                
    f = urllib2.urlopen('http://api.wunderground.com/api/%s/geolookup/hourly/q/pws:%s.json'%(wunderground_token, wunderground_station))
    json_string = f.read()
    parsed_json = json.loads(json_string)
    #print json_string
    hourly_forecasts = parsed_json['hourly_forecast']
    count = 0
    send_message("Weather Forecast:")
    for hourly_forecast in hourly_forecasts:
        count += 1
        if count > wunderground_max_forecast_hours:
            break
        send_message("", [{"image_url":"%s"%hourly_forecast['icon_url'], "title":"%s at %s:"%(hourly_forecast['condition'],hourly_forecast['FCTTIME']['pretty'])}])
    send_message("\n")
    f.close()    
        
#get weather from Wunderground
def getWeather(command, user):  
    logme('Retrieving the current weather conditions from wunderground.com...') 
    
    #match = re.search('^\\\\(weather)\s(hourly)', command)
    f = urllib2.urlopen('http://api.wunderground.com/api/%s/geolookup/conditions/q/pws:%s.json'%(wunderground_token, wunderground_station))
    json_string = f.read()
    #print json_string
    parsed_json = json.loads(json_string)
    location = parsed_json['current_observation']['observation_location']['city']
    station = parsed_json['current_observation']['station_id']
    temp = parsed_json['current_observation']['temperature_string']
    weather = parsed_json['current_observation']['weather']
    rh = parsed_json['current_observation']['relative_humidity']
    wind = parsed_json['current_observation']['wind_string']
    wind_dir = parsed_json['current_observation']['wind_dir']
    wind_mph = parsed_json['current_observation']['wind_mph']
    dewpoint = parsed_json['current_observation']['dewpoint_string']
    icon_url = parsed_json['current_observation']['icon_url']
    #send_message("", [{"image_url":"%s"%icon_url, "title":"Weather at SEO (%s):"%station}])
    send_message("", [{"image_url":"%s"%icon_url, "title":"Current Weather:"}])
    send_message(">Conditions: %s" %(weather))    
    send_message(">Temperature: %s" %(temp))
    send_message(">Winds: %s" %(wind))
    send_message(">Humidity: %s" %(rh))
    send_message(">Local Station: %s (%s)" %(location, station))        
    send_message("\n") 
    f.close()

def getHelp(command, user=None):
    logme('Processing the "help" command...')
    
    #allow getHelp to be called by Itzamna
    user_name = 'Fear not, mortals'
    if user != None:
        user_name = user['profile']['first_name']
    send_message(   user_name + ', here are some helpful tips:\n' + \
                    '>`\\help` shows this message\n' + \
                    '>`\\where` shows where the telescope is pointing\n' + \
                    '>`\\weather` shows the current weather conditions\n' + \
                    '>`\\forecast` shows the hourly weather forecast\n' + \
                    '>`\\stats` shows the weekly telescope statistics\n' + \
                    '>`\\clearsky` shows the Clear Sky chart(s)\n' + \
                    '>`\\find <object>` finds <object> position in sky\n' + \
                    '>`\\observe <object #>` shows if/when <object> is observable (run `\\find` first!)\n' + \
                    '>`\\lock` locks the telescope\n' + \
                    '>`\\unlock` unlocks the telescope\n' \
                    )
    send_message('\n')

def abort(msg):
    logme(msg)
    os.sys.exit(1)

#print and log messages
def logme(msg):
    #open log file
    log=open(log_fname, 'a+')
    dt = datetime.datetime.now().strftime("%Y/%m/%d %H:%M:%S:\t") 
    log.write(dt + msg + "\n")
    log.close()
    print dt + msg
    return

#send a message into the slack_channel
def send_message(msg, attachments=None):
    if slack_connected:
        sc.api_call(
          "chat.postMessage",
          channel=slack_channel,
          text=msg+'\n',
          username=bot_name,
          attachments=attachments
        )
        if len(msg.strip()) > 0:
            logme('Slack message: "%s"'%(msg.strip()))
    else:
        logme('Error! Could not send message. Client is not connected.')

def send_file(path, title):
    if slack_connected:
        #curl -F file=@$file -F channels=$slackpreview_channel -F title="$title" -F token=$slackpreview_token $slackpreview_url > /dev/null 2>&1
        files = {'file': open(path, 'rb')}
        data = {'channels': slack_channel_name, 'title': title, 'token' : slack_token}  
        url = slack_file_upload_url
        r = requests.post(url, files=files, data=data)
    else:
        logme('Error! Could not send file (%s). Client is not connected.'%path)
        
#get a list of slack users        
def get_users():
    if slack_connected:
        result = sc.api_call("users.list")
        if 'members' in result:
            return result['members']        
    logme('Error! Could not get user list. Client is not connected.')
    return []        

#get a list of private and public channels
def get_channels():
    if slack_connected:
        channels = []
        #combined public and private channels
        result = sc.api_call("channels.list")
        if 'channels' in result:
            channels += result['channels']
        result = sc.api_call("groups.list")
        if 'groups' in result:
            channels += result['groups'] 
        if len(channels):
            return channels             
    logme('Error! Could not get channel list. Client is not connected.')
    return []
    
def buildSatDatabase():
    global norad_sat_db
    for url in norad_sats_urls:
        #grab NORAD geosat data
        sats = urllib2.urlopen(url).readlines()
        #clean it up
        sats = [item.strip() for item in sats]
        #create an array of name, tle1, and tle2
        sats = [(str.upper(sats[i]),sats[i+1],sats[i+2]) for i in xrange(0,len(sats)-2,3)]
        #add sats to norad database
        norad_sat_db = numpy.concatenate((norad_sat_db, sats))
    logme('Read NORAD Two-Line Elements for %d satellite(s).'%len(norad_sat_db))

def ping():
    if slack_connected:
        try:
            data = json.dumps({"type": "ping"})
            sc.server.websocket.send(data)
            return True
        except:
            return False

def process_messages(msgs):
    global dt_last_message
    global dt_last_activity
    for msg in msgs:
        dt_last_activity = datetime.datetime.now()
        #look for a msg from a user in the slack_channel, itzamna
        if 'type' in msg and 'channel' in msg and 'user' in msg:
            if msg['type'] == 'message' and msg['channel'] == slack_channel:
                dt = datetime.datetime.fromtimestamp(float(msg['ts'])) 
                #which user sent this message?
                #user_name='Unknown'
                user = None
                for slack_user in slack_users:
                    if(slack_user['id'] == msg['user']):
                        #print user
                        user = slack_user
                        #user_name = user['profile']['first_name']
                        break
                if(dt > dt_last_message):
                    dt_last_message = dt
                    text = msg['text'].strip()
                    match = re.search('^\\\\', text)    
                    if match:
                        parse_command(text, user, dt)
                    else:                          
                        logme('User %s sent text (%s) on %s.'%(user['profile']['first_name'], msg['text'],dt_last_message.strftime("%Y/%m/%d @ %H:%M:%S")))
                    #is this a command, starts with \      
                else:
                    logme('Warning! Ignoring old/duplicate message from #%s ("%s" from %s).'%(slack_channel_name,msg['text'], user['profile']['first_name']))         

def parse_command(text, user, dt):
    match = False
    for command in commands:
        #match the command first, then look for
        match = re.search(command[0], text)
        if match: #compare with list of known commands, spelling and capitalization count!
            logme('%s sent command (%s).'%(user['profile']['first_name'], text))
            #call associated function
            command[1](text, user)
            break;
        #index += 1
    if not match: #did not recognize this command
        logme('%s sent unrecognized command (%s) on %s.'%(user['profile']['first_name'], text, dt.strftime("%Y/%m/%d @ %H:%M:%S")))
        send_message('%s, the almighty Itzamna does not recognize your command (%s).'%(user['profile']['first_name'],text))  
 
###############################
#CHANGE THESE VALUES AS NEEDED#
############################### 
#log file
log_fname = 'itzanma.log'
#name of channel assigned to telescope interface
slack_channel_name='itzamna'
#how long to wait before successive slack reads
read_delay_s=1 
#how long to wait before trying to reconnect after connection fails or drops
reconnect_delay_s=10
#name of this book
bot_name='Itzamna'
#specify a station close to SEO, e.g. LOLO Sonoma Farms
wunderground_station = 'KCASONOM27'
#how many hours of forecast should we show? 
wunderground_max_forecast_hours = 12 
#giphy shown when itzamna app is first started
welcome_giphy_url = 'http://www.nowinski.com/downloads/itzamna.gif'
#norad sat tle database urls
norad_sats_urls = [
'http://www.celestrak.com/NORAD/elements/geo.txt',
'http://www.celestrak.com/NORAD/elements/iridium.txt',
'http://www.celestrak.com/NORAD/elements/iridium-NEXT.txt'
]
###############################
#CHANGE THESE VALUES AS NEEDED#
############################### 

#valid commands
#element 1 is a regular expression describing the command (and its parameters)
#element 2 is the function called when this command is received
commands = [

['^\\\\(help)', getHelp],
#['^\\\\(weather)(\s)?(hourly)?', getWeather],
['^\\\\(weather)', getWeather],
['^\\\\(forecast)', getForecast],
['^\\\\(clearsky)',getClearDarkSky],
['^\\\\(where)',getWhere],
['^\\\\(sun)',getSun],
['^\\\\(moon)',getMoon],
['^\\\\(slit)',getSlit],
['^\\\\(test)', doTest],
['^\\\\(find) ([a-zA-Z0-9\\s\\-\\+\\*]+)', getObject],
['^\\\\(observe)(\\s[0-9]+)?', getObservability],
['^\\\\(stats)', getStats],
['^\\\\(lock)', doLock],
['^\\\\(unlock)', doUnLock],
 
]

#ensure slack token has been provided
if(len(sys.argv) < 3):
    abort('Error! Invalid command line arguments. Use "itzanma <SLACK_API_TOKEN> <WUNDERGROUND_API_TOKEN>".')

#the Slack api token
slack_token = sys.argv[1]
#the Slack url for file uploads
slack_file_upload_url="https://slack.com/api/files.upload"
#the Wunderground api token
wunderground_token = sys.argv[2]
#track if slack client is connected
slack_connected = False
#list of slack users
slack_users = []
#list of slack channels
slack_channels = []
#id of slack channel assigned to telescope interface
slack_channel = None
#track time of last message received
#use to ignore old/duplcate messages
dt_last_message = dt_last_activity = datetime.datetime.now()
#observatory info
#seo
observatory_code = 'G52'
observatory_lat = 38.259 #deg
observatory_lon = -122.440 #deg
observatory_elev = 63.8 #m
observatory_tz = 'US/Pacific' #for pytz
#satellite database; starts empty
norad_sat_db = numpy.array([]).reshape(0,3)
#set pyephem observer
sat_observer = ephem.Observer()
sat_observer.lat, sat_observer.lon = observatory_lat, observatory_lon
#calc SkyCoord Earth location of observatory    
object_observer = EarthLocation(lat=observatory_lat*u.deg, lon=observatory_lon*u.deg, height=observatory_elev*u.m) 
#current object list
objects = [] #all objects found

logme('Starting #itzamna Slack bot service...')

#build up a database of satellites from NORAD TLEs
buildSatDatabase()

#init the slack client
sc = SlackClient(slack_token)

#main loop
while True:
    #connect to slack
    logme('Trying to connect to Slack...')
    if sc.rtm_connect():
        logme('Connected to Slack!')
        slack_connected = True
        #get list of users
        slack_users = get_users()
        #get a list of channels
        slack_channels = get_channels()
        #get the id of the slack channel assigned to telescope interface 
        slack_channel = None
        for channel in slack_channels:
            if channel['name'] == slack_channel_name:
                slack_channel = channel['id']
        if slack_channel == None:
            abort('Error! Could not find #%s.'%slack_channel_name)
        #send welcome message
        doWelcome()
        logme('Listening for commands on #%s...'%slack_channel_name)
        #data loop  
        while True:
            try:
                msgs = sc.rtm_read() #returns array of json objects, e.g. return of json.loads()
            except:
                logme('Error! Connection with Slack was lost. Retrying...')
                #time.sleep(read_delay_s)
                sc.rtm_connect()
            #process incoming messages
            process_messages(msgs)
            #print msgs
            #ping to ensure connection is intact
            if (datetime.datetime.now() - dt_last_activity).total_seconds() > 60:
                logme('Pinging Slack server...')
                if not ping():
                    logme('Error! Connection with Slack was lost. Retrying...')
                    #time.sleep(read_delay_s)
                    sc.rtm_connect()
                    #logme('Error! Connection with Slack was lost. Reconnecting...')
                    #slack_connected = False
                    #break
                logme('Received pong...still connected to Slack!')
                dt_last_activity = datetime.datetime.now()
            #wait
            time.sleep(read_delay_s)
    else:
        logme("Error! Slack connection failed. Retrying in %d seconds..."%(slack_token,reconnect_delay_s))
    time.sleep(reconnect_delay_s)

#clean up    
#log.close()              