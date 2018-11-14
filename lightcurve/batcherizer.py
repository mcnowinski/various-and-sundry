from subprocess import Popen, PIPE
import glob
import time
import math
import os

# path to python script that performs the plate-solving one file at a time
asterize_script_path = '/cygdrive/d/owncloud/code/giterdone/lightcurve/asterize.py'
# input path *with* ending forward slash
input_path = './'
# how many threads to run at once?
max_threads = 2

fits_list = glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
print 'Found %d fits files.' % len(fits_list)
cmds_list = [['python', asterize_script_path, file_name]
             for file_name in fits_list]
# do quick calculate of average processing time per file
start = time.time()
last_cmd = 0
num_batches = int(math.ceil(len(fits_list)*1.0/max_threads))
print 'Processing files in %d batch(es) of %d thread(s)...' % (
    num_batches, max_threads)
for batch in range(0, num_batches):
    print 'Starting batch #%d...' % batch
    end_cmd = last_cmd + max_threads
    if end_cmd > len(cmds_list):
        end_cmd = len(cmds_list)
    procs_list = [Popen(cmds_list[cmd], stdout=PIPE, stderr=PIPE)
                  for cmd in range(last_cmd, end_cmd)]
    for proc in procs_list:
        out, err = proc.communicate()
        print out
        print err
    # next batch
    last_cmd += max_threads
# do quick calculate of average processing time per file
end = time.time()
# remove astrometry.net temporary files
os.system("find . -name '*.xyls' -delete;")
os.system("find . -name '*.axy' -delete;")
os.system("find . -name '*.corr' -delete;")
os.system("find . -name '*.match' -delete;")
os.system("find . -name '*.rdls' -delete;")
os.system("find . -name '*.solved' -delete;")
os.system("find . -name '*.wcs' -delete;")
os.system("find . -name '*.png' -delete;")
print 'Average processing time per file is %f s.' % (
    (end - start)/len(fits_list))
