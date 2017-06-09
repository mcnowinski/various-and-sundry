import logging
from logging.handlers import RotatingFileHandler
import argparse

#configure logging
log_file='/home/mcnowinski/var/log/seo.log'
logger = logging.getLogger('snapit')
logger.setFormatter(logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s'))
logger.setLevel(logging.INFO)
handler = RotatingFileHandler(log_file, maxBytes=5*1024*1024, backupCount=10)
logger.addHandler(handler)
logger.info('Started snapit...')

#parse command line parameters
parser = argparse.ArgumentParser(prog='snapit', description='Take image with the SEO telescope.')
parser.add_argument('--time', type=int, help='exposure time in seconds')
parser.add_argument('--file', type=str, help='output filename')
parser.add_argument('--bin', type=int, default=1, help='binning (e.g., 1, 2, etc.)')
parser.add_argument('--observer', type=str, help='name of observer')
parser.add_argument('--label', type=str, help='image label')
parser.add_argument('--notel', action='store_true', help='do *not* store telescope info in FITS header')
parser.add_argument('--dark', action='store_true', help='take "dark" image, i.e. shutter closed')
parser.add_argument('--cloud', type=int, help='max. cloud coverage (0-100)')
parser.add_argument('--slit', action='store_true', help='ensure slit is open')
parser.add_argument('--log', type=str, help='log level (debug, info, warning, error, critical)')
#store command lin eparameters in args
args = vars(parser.parse_args())

#set up logging
logging.basicConfig(filename='example.log',level=logging.DEBUG)
logging.debug('This message should go to the log file')
logging.info('So should this')
logging.warning('And this, too')

