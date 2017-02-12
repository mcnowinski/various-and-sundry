#Web and Real-Time Messaging interface to Slack for use with the SEO telescope, a.k.a. itzamna

from slackclient import SlackClient
import time
import os
import sys
import datetime
import json
import re
import urllib2
import subprocess
import astropy.coordinates as coord
from astropy.coordinates import SkyCoord
import astropy.units as u

#http://server3.sky-map.org/imgcut?survey=SDSS&img_id=all&angle=0.9375&ra=23.50142&de=47.253&width=400&height=400&projection=tan&interpolation=bicubic&jpeg_quality=0.8&output_type=jpeg
#http://server3.wikisky.org/map?custom=1&language=EN&type=PART&w=500&h=500&angle=5.0&ra=9.9166666666666666666666666666667&de=69.066666666666666666666666666667&rotation=0.0&mag=10&max_stars=100000&zoom=10&borders=1&border_color=400000&show_grid=0&grid_color=404040&grid_color_zero=808080&grid_lines_width=1.0&grid_ra_step=1.0&grid_de_step=15.0&show_const_lines=0&constellation_lines_color=006000&constellation_lines_width=1.0&show_const_names=&constellation_names_color=006000&const_name_font_type=PLAIN&const_name_font_name=SanSerif&const_name_font_size=15&show_const_boundaries=&constellation_boundaries_color=000060&constellation_boundaries_width=1.0&background_color=000000&output=PNG

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

def getObject(command, user):
    match = re.search('^\\\\(find) ([a-zA-Z0-9\\s]+)', command)
    if(match):
        object_name = match.group(2)
    else:
        logme('Error. Unexpected command format (%s).'%command)
        return        
    logme('Search for object (%s)...'%object_name)
    try:
        object = SkyCoord.from_name(object_name)
    except:
        logme('Error. Could not find object (%s).'%object_name)
        send_message('Sorry. Itzamna knows all but *still* could not find "%s".'%object_name)
        return
    logme('Object (%s) found at RA=%s, DEC=%s.'%(object_name, object.ra, object.dec))
    send_message('Object (%s) found at RA=%s, DEC=%s.'%(object_name, object.ra, object.dec))
    
def isLocked():
    logme('Checking to see if the telescope is locked...')
    
    locked_by = ""
    (output, error, pid) = runSubprocess(['tx','lock'])
    #print output
    #done lock user=mcnowinski email=mcnowinski@gmail.com phone=7032869140 comment=slac timestamp=2017-02-10T20:32:03Z
    match = re.search('^done lock user=([a-zA-Z]+) email=([a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+)', output)
    if(match):
        locked_by = match.group(1) + ' (' + match.group(2) + ')'
        logme('Telescope is currently locked by %s.'%locked_by)
    return locked_by

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
        ra_decimal = coord.Angle(match.group(1) + '  hours')
        dec_decimal = coord.Angle(match.group(2) + '  degrees')
        #skymap_dot_org_image_url='http://server3.sky-map.org/imgcut?survey=SDSS&img_id=all&angle=0.9375&ra=%f&de=%f&width=400&height=400&projection=tan&interpolation=bicubic&jpeg_quality=0.8&output_type=jpeg'%(ra_decimal.hour,dec_decimal.degree)
        #url='http://server3.wikisky.org/map?custom=1&language=EN&type=PART&w=500&h=500&angle=5.0&ra=%f&de=%f&rotation=0.0&mag=8&max_stars=100000&zoom=10&borders=1&border_color=400000&show_grid=0&grid_color=404040&grid_color_zero=808080&grid_lines_width=1.0&grid_ra_step=1.0&grid_de_step=15.0&show_const_lines=0&constellation_lines_color=006000&constellation_lines_width=1.0&show_const_names=&constellation_names_color=006000&const_name_font_type=PLAIN&const_name_font_name=SanSerif&const_name_font_size=15&show_const_boundaries=&constellation_boundaries_color=000060&constellation_boundaries_width=1.0&background_color=000000&output=PNG'%(ra_decimal.hour,dec_decimal.degree)
        #send_message("", [{"image_url":"%s"%url, "title":"Sky-Map View"}])
        url='http://server3.sky-map.org/imgcut?survey=DSS2&img_id=all&angle=0.5&ra=%f&de=%f&width=400&height=400&projection=tan&interpolation=bicubic&jpeg_quality=0.8&output_type=jpeg'%(ra_decimal.hour,dec_decimal.degree)
        send_message("", [{"image_url":"%s"%url, "title":"Sky Position (DSS2):"}])
        send_message('\n')
        #logme('%f'%ra_decimal.hour)
        #logme('%f'%dec_decimal.degree)        
        #send_message(output)
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
        #send_message('Conditions at %s will be %s.'%(hourly_forecast['FCTTIME']['pretty'],hourly_forecast['condition']))
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
                    '>`\\clearsky` shows the Clear Sky chart(s)\n' \
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
    else:
        logme('Error! Could not sent message. Client is not connected.')
        
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
                #send_message('You are the greatest, %s.'%user_name)       

def parse_command(text, user, dt):
    match = False
    for command in commands:
        #match the command first, then look for
        match = re.search(command[0], text)
        if match: #compare with list of known commands, spelling and capitalization count!
            logme('%s sent command (%s) on %s.'%(user['profile']['first_name'], text, dt.strftime("%Y/%m/%d @ %H:%M:%S")))
            #call associated function
            command[1](text, user)
            #send_message('%s, the almighty Itzamna has received your command (%s).'%(user_name,text))
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
['^\\\\(find) ([a-zA-Z0-9\\s]+)', getObject],
    
]

#ensure slack token has been provided
if(len(sys.argv) < 3):
    abort('Error! Invalid command line arguments. Use "itzanma <SLACK_API_TOKEN> <WUNDERGROUND_API_TOKEN>".')

#the Slack api token
slack_token = sys.argv[1]
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

#the slack client
sc = SlackClient(slack_token)

#connect loop
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
            msgs = sc.rtm_read() #returns array of json objects, e.g. return of json.loads()
            #process incoming messages
            process_messages(msgs)
            #print msgs
            #ping to ensure connection is intact
            if (datetime.datetime.now() - dt_last_activity).total_seconds() > 60:
                logme('Pinging Slack server...')
                if not ping():
                    logme('Error! Connection with Slack was lost. Retrying in %d seconds...'%(reconnect_delay_s))
                    slack_connected = False
                    break
                logme('Received pong...still connected to Slack!')
                dt_last_activity = datetime.datetime.now()
            #wait
            time.sleep(read_delay_s)
    else:
        logme("Error! Slack connection failed. Retrying in %d seconds..."%(slack_token,reconnect_delay_s))
    time.sleep(reconnect_delay_s)

#clean up    
#log.close()              