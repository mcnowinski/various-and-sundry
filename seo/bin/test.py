#!/usr/bin/python

import sys

#
## the mock-0.3.1 dir contains testcase.py, testutils.py & mock.py
#sys.path.append('/home/mcnowinski/.local/lib/python2.7/')

import astropy
from astropy.io import fits
from astropy import wcs
from astropy.io.fits import getheader
import astropy.coordinates as coord
import astropy.units as u

astropy.test()
