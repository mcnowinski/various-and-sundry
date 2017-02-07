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

def doTest(command, user_name):
    output = subprocess.check_output('date -u', shell=True, stderr=subprocess.STDOUT)
    send_message(output)
    output = subprocess.check_output('tx taux', shell=True, stderr=subprocess.STDOUT)
    send_message(output)
    output = subprocess.check_output('sun', shell=True, stderr=subprocess.STDOUT)
    send_message(output)	
    output = subprocess.check_output('moon', shell=True, stderr=subprocess.STDOUT)
    send_message(output)
    output = subprocess.check_output('tx slit', shell=True, stderr=subprocess.STDOUT)
    send_message(output)
    output = subprocess.check_output('tx lock', shell=True, stderr=subprocess.STDOUT)
    send_message(output)
    output = subprocess.check_output('tx lamps', shell=True, stderr=subprocess.STDOUT)
    send_message(output)
    output = subprocess.check_output('who', shell=True, stderr=subprocess.STDOUT)
    send_message(output)   
    output = subprocess.check_output('tx dome', shell=True, stderr=subprocess.STDOUT)
    send_message(output)  
    output = subprocess.check_output('tx track', shell=True, stderr=subprocess.STDOUT)
    send_message(output)
    output = subprocess.check_output('tx where', shell=True, stderr=subprocess.STDOUT)
    send_message(output)      
    output = subprocess.check_output('tx mets', shell=True, stderr=subprocess.STDOUT)
    send_message(output) 
    #send_message("", [{"fields": [{"title": "Priority","value": "<http://i.imgur.com/nwo13SM.png|test>","short": True},{"title": "Priority","value": "Low","short": True}]}])    

#get weather from Wunderground
def getForecast(command, user_name):
    logme('Retrieving the hourly forecast from wunderground.com...')  
                
    f = urllib2.urlopen('http://api.wunderground.com/api/%s/geolookup/hourly/q/pws:%s.json'%(wunderground_token, wunderground_station))
    json_string = f.read()
    parsed_json = json.loads(json_string)
    #print json_string
    hourly_forecasts = parsed_json['hourly_forecast']
    count = 0
    send_message("Weather forecast at SEO:")
    for hourly_forecast in hourly_forecasts:
        count += 1
        if count > wunderground_max_forecast_hours:
            break
        send_message("", [{"image_url":"%s"%hourly_forecast['icon_url'], "title":"%s at %s:"%(hourly_forecast['condition'],hourly_forecast['FCTTIME']['pretty'])}])
        #send_message('Conditions at %s will be %s.'%(hourly_forecast['FCTTIME']['pretty'],hourly_forecast['condition']))
    send_message("\n")
    f.close()    
        
#get weather from Wunderground
def getWeather(command, user_name):  
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
    send_message("", [{"image_url":"%s"%icon_url, "title":"Weather at SEO:"}])
    send_message(">Conditions: %s" %(weather))    
    send_message(">Temperature: %s" %(temp))
    send_message(">Winds: %s" %(wind))
    send_message(">Humidity: %s" %(rh))
    send_message(">Local Station: %s (%s)" %(location, station))        
    send_message("\n") 
    f.close()

def getHelp(command, user_name):
    logme('Processing the "help" command...')
    send_message(user_name + ', here are some helpful tips:\n>`\\help` shows this message\n>`\\weather` shows the current weather conditions at SEO\n>`\\forecast` shows the hourly weather forecast at SEO\n')

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
                user_name='Unknown'
                for user in slack_users:
                    if(user['id'] == msg['user']):
                        #print user
                        user_name = user['profile']['first_name']
                        break
                if(dt > dt_last_message):
                    dt_last_message = dt
                    text = msg['text'].strip()
                    match = re.search('^\\\\', text)    
                    if match:
                        parse_command(text, user_name, dt)
                    else:                          
                        logme('User %s sent text (%s) on %s.'%(user_name, msg['text'],dt_last_message.strftime("%Y/%m/%d @ %H:%M:%S")))
                    #is this a command, starts with \      
                else:
                    logme('Warning!. Ignoring old/duplicate message from #%s ("%s" from %s).'%(slack_channel_name,msg['text'],user_name))    
                #send_message('You are the greatest, %s.'%user_name)       

def parse_command(text, user_name, dt):
    match = False
    for command in commands:
        #match the command first, then look for
        match = re.search(command[0], text)
        if match: #compare with list of known commands, spelling and capitalization count!
            logme('%s sent command (%s) on %s.'%(user_name, text, dt.strftime("%Y/%m/%d @ %H:%M:%S")))
            #call associated function
            command[1](text, user_name)
            #send_message('%s, the almighty Itzamna has received your command (%s).'%(user_name,text))
            break;
        #index += 1
    if not match: #did not recognize this command
        logme('%s sent unrecognized command (%s) on %s.'%(user_name, text, dt.strftime("%Y/%m/%d @ %H:%M:%S")))
        send_message('%s, the almighty Itzamna does not recognize your command (%s).'%(user_name,text))
  
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
['^\\\\(test)', doTest],
    
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
        send_message("", [{"image_url":"%s"%welcome_giphy_url, "title":"Itzamna is here! Let your petitions be known..."}])
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