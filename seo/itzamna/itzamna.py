#Web and Real-Time Messaging interface to Slack for use with the SEO telescope, a.k.a. itzamna

from slackclient import SlackClient
import time
import os
import sys
import datetime
import json

def abort(msg):
    logme(msg)
    log.close()
    os.sys.exit(1)

#print and log messages
def logme(msg):
    dt = datetime.datetime.now().strftime("%Y/%m/%d %H:%M:%S:\t") 
    log.write(dt + msg + "\n")
    print dt + msg
    return

#send a message into the slack_channel
def send_message(msg):
    if slack_connected:
        sc.api_call(
          "chat.postMessage",
          channel=slack_channel,
          text=msg
        )
    else:
        logme('Error. Could not sent message. Client is not connected.')
        
#get a list of slack users        
def get_users():
    if slack_connected:
        result = sc.api_call("users.list")
        if 'members' in result:
            return result['members']        
    logme('Error. Could not get user list. Client is not connected.')
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
    logme('Error. Could not get channel list. Client is not connected.')
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
    for msg in msgs:
        #look for a msg from a user in the slack_channel, itzamna
        if 'type' in msg and 'channel' in msg and 'user' in msg:
            if msg['type'] == 'message' and msg['channel'] == slack_channel:
                #which user sent this message?
                user_name='Unknown'
                for user in slack_users:
                    if(user['id'] == msg['user']):
                        user_name = user['name']
                logme('User %s said, "%s" at %s.'%(user_name, msg['text'],msg['ts']))
                #send_message('You are the greatest, %s.'%user_name)
                
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
###############################
#CHANGE THESE VALUES AS NEEDED#
############################### 

#open log file
log=open(log_fname, 'a+')

#ensure slack token has been provided
if(len(sys.argv) < 2):
    abort('Error. Invalid command line arguments. Use "itzanma <SLACK_API_TOKEN>".')

#the api token
slack_token = sys.argv[1]
#track if slack client is connected
slack_connected = False
#list of slack users
slack_users = []
#list of slack channels
slack_channels = []
#id of slack channel assigned to telescope interface
slack_channel = None

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
            abort('Error. Could not find #%s.'%slack_channel_name)
        logme('Listening for commands on #%s...'%slack_channel_name)
        #data loop  
        while True:
            msgs = sc.rtm_read() #returns array of json objects, e.g. return of json.loads()
            #process incoming messages
            process_messages(msgs)
            #wait
            time.sleep(read_delay_s)
            #ping to ensure connection is intact
            if not ping():
                logme('Error. Connection with Slack was lost. Retrying in %d seconds...'%(reconnect_delay_s))
                slack_connected = False
                break
    else:
        logme("Error. Slack connection failed. Retrying in %d seconds..."%(slack_token,reconnect_delay_s))
    time.sleep(reconnect_delay_s)          