import pandas as pd
from collections import defaultdict
import math
from scipy import stats
import numpy as np
import matplotlib.pyplot as plt
import os

#SDSS MOC4 data file
path = 'ADR4.dat'

#solar colors (reverse calculated from Carvano)
#reference to g
solar_color_ug = 3.81
solar_color_rg = 2.04
solar_color_ig = 1.94
solar_color_zg = 1.90
solar_color_gg = 2.5 #to make LRgg = 1

#4.27, 2.96, 2.5, 2.4, 2.36
#2.32, 0.46, 0, -0.1, -0.14
#reference to r
solar_color_ur = solar_color_ug - solar_color_rg
solar_color_gr = solar_color_gg - solar_color_rg
solar_color_rr = 0.0
solar_color_ir = solar_color_ig - solar_color_rg
solar_color_zr = solar_color_zg - solar_color_rg
#print solar_color_ur, solar_color_gr, solar_color_rr, solar_color_ir, solar_color_zr
#os.sys.exit(1)

#sdss wavelengths (microns)
#0.354, 0.477, 0.6230, 0.7630 and 0.913 um
u_wavelength=0.3543
g_wavelength=0.4770
r_wavelength=0.6231
i_wavelength=0.7625
z_wavelength=0.9134

#carvano taxonomy limits
#TAX  LRug    LRgg    LRrg    LRig    LRzg    CGguL  CGguU   CGrgL  CGrgU   CGirL  CGirU  CGziL  CGziU 
# O   0.884   1.000   1.057   1.053   0.861   0.784  1.666   0.175  0.505  -0.143  0.106  -0.833 -0.467 
# V   0.810   1.000   1.099   1.140   0.854   1.087  2.095   0.511  2.374  -0.077  0.445  -2.018 -0.683 
# Q   0.842   1.000   1.082   1.094   0.989   0.757  2.122   0.421  0.967  -0.032  0.229  -0.719 -0.200 
# S   0.839   1.000   1.099   1.148   1.096   0.868  1.960   0.379  0.910   0.148  0.601  -0.530 -0.047 
# A   0.736   1.000   1.156   1.209   1.137   1.264  4.210   0.937  1.342   0.151  0.505  -0.521 -0.089 
# C   0.907   1.000   1.008   1.011   1.021   0.385  1.990  -0.140  0.403  -0.203  0.202  -0.221  0.259 
# X   0.942   1.000   1.029   1.063   1.073   0.178  1.081  -0.089  0.481   0.136  0.478  -0.182  0.187 
# L   0.858   1.000   1.071   1.109   1.116   0.913  2.089   0.253  0.871   0.136  0.622  -0.125  0.160 
# D   0.942   1.000   1.075   1.135   1.213   0.085  1.717  -0.080  0.589   0.142  0.625   0.121  0.502
LR_means = {}
LR_means['O'] = {'LRug': 0.884, 'LRgg': 1.000, 'LRrg': 1.057, 'LRig': 1.053, 'LRzg': 0.861}
LR_means['V'] = {'LRug': 0.810, 'LRgg': 1.000, 'LRrg': 1.099, 'LRig': 1.140, 'LRzg': 0.854}
LR_means['Q'] = {'LRug': 0.842, 'LRgg': 1.000, 'LRrg': 1.082, 'LRig': 1.094, 'LRzg': 0.989}
LR_means['S'] = {'LRug': 0.839, 'LRgg': 1.000, 'LRrg': 1.099, 'LRig': 1.148, 'LRzg': 1.096}
LR_means['A'] = {'LRug': 0.736, 'LRgg': 1.000, 'LRrg': 1.156, 'LRig': 1.209, 'LRzg': 1.137}
LR_means['C'] = {'LRug': 0.907, 'LRgg': 1.000, 'LRrg': 1.008, 'LRig': 1.011, 'LRzg': 1.021}
LR_means['X'] = {'LRug': 0.942, 'LRgg': 1.000, 'LRrg': 1.029, 'LRig': 1.063, 'LRzg': 1.073}
LR_means['L'] = {'LRug': 0.858, 'LRgg': 1.000, 'LRrg': 1.071, 'LRig': 1.109, 'LRzg': 1.116}
LR_means['D'] = {'LRug': 0.942, 'LRgg': 1.000, 'LRrg': 1.075, 'LRig': 1.135, 'LRzg': 1.213}

#K type calc from Wabash 2453
LR_means['K'] = {'LRug': 0.871, 'LRgg': 1.000, 'LRrg': 1.053, 'LRig': 1.088, 'LRzg': 1.077}

#calc slope and bd (Carvano 2015) for the mean taxonomic shapes (Carvano 2011)
#LR_means['O'] = {'LRug': 0.884, 'LRgg': 1.000, 'LRrg': 1.057, 'LRig': 1.053, 'LRzg': 0.861}
log_mean=open('moc4.mean.txt', 'w')
log_mean.write('%s,%f,%f,%f,%f,%f\n'%('space', u_wavelength, g_wavelength, r_wavelength, i_wavelength, z_wavelength))
log_mean.write('%s,%s,%s,%s,%s,%s,%s,%s\n'%('class', 'Rur', 'Rgr', 'Rrr', 'Rir', 'Rzr', 'slope', 'bd'))
for key in LR_means:
    LRug = LR_means[key]['LRug']
    LRgg = LR_means[key]['LRgg']
    LRrg = LR_means[key]['LRrg']
    LRig = LR_means[key]['LRig']
    LRzg = LR_means[key]['LRzg']
    #
    Cug = -2.5*LRug
    Cgg = -2.5*LRgg
    Crg = -2.5*LRrg
    Cig = -2.5*LRig
    Czg = -2.5*LRzg
    #
    Cur = Cug - Crg
    Cgr = Cgg - Crg
    Crr = 0.0
    Cir = Cig - Crg
    Czr = Czg - Crg
    #
    LRur = -Cur/2.5
    LRgr = -Cgr/2.5
    LRrr = -Crr/2.5
    LRir = -Cir/2.5
    LRzr = -Czr/2.5
    #
    Rur = pow(10,LRur)    
    Rgr = pow(10,LRgr) 
    Rrr = pow(10,LRrr) 
    Rir = pow(10,LRir) 
    Rzr = pow(10,LRzr)
    #Carvano 2015 parameters
    slope = (Rir-Rgr)/(i_wavelength-g_wavelength)
    bd = Rzr - Rir
    log_mean.write('%s,%f,%f,%f,%f,%f,%f,%f\n'%(key, Rur, Rgr, Rrr, Rir, Rzr, slope, bd))
log_mean.close()

#os.sys.exit(1)

CG_limits = {}
CG_limits['O'] = {'CGguL': 0.784, 'CGguU': 1.666, 'CGrgL': 0.175, 'CGrgU': 0.505, 'CGirL':-0.143, 'CGirU': 0.106, 'CGziL': -0.833, 'CGziU': -0.467}
CG_limits['V'] = {'CGguL': 1.087, 'CGguU': 2.095, 'CGrgL': 0.511, 'CGrgU': 2.374, 'CGirL':-0.077, 'CGirU': 0.445, 'CGziL': -2.018, 'CGziU': -0.683}
CG_limits['Q'] = {'CGguL': 0.757, 'CGguU': 2.122, 'CGrgL': 0.421, 'CGrgU': 0.967, 'CGirL':-0.032, 'CGirU': 0.229, 'CGziL': -0.719, 'CGziU': -0.200}
CG_limits['S'] = {'CGguL': 0.868, 'CGguU': 1.960, 'CGrgL': 0.379, 'CGrgU': 0.910, 'CGirL': 0.148, 'CGirU': 0.601, 'CGziL': -0.530, 'CGziU': -0.047}
CG_limits['A'] = {'CGguL': 1.264, 'CGguU': 4.210, 'CGrgL': 0.937, 'CGrgU': 1.342, 'CGirL': 0.151, 'CGirU': 0.505, 'CGziL': -0.521, 'CGziU': -0.089}
CG_limits['C'] = {'CGguL': 0.385, 'CGguU': 1.990, 'CGrgL':-0.140, 'CGrgU': 0.403, 'CGirL':-0.203, 'CGirU': 0.202, 'CGziL': -0.221, 'CGziU':  0.259}
CG_limits['X'] = {'CGguL': 0.178, 'CGguU': 1.081, 'CGrgL':-0.089, 'CGrgU': 0.481, 'CGirL': 0.136, 'CGirU': 0.478, 'CGziL': -0.182, 'CGziU':  0.187}
CG_limits['L'] = {'CGguL': 0.913, 'CGguU': 2.089, 'CGrgL': 0.253, 'CGrgU': 0.871, 'CGirL': 0.136, 'CGirU': 0.622, 'CGziL': -0.125, 'CGziU':  0.160}
CG_limits['D'] = {'CGguL': 0.085, 'CGguU': 1.717, 'CGrgL':-0.080, 'CGrgU': 0.589, 'CGirL': 0.142, 'CGirU': 0.625, 'CGziL':  0.121, 'CGziU':  0.502}
#1 x sigma
#1.243181211	0.516802843	0.357449432	0.074183133
#0.870581826	0.209380322	0.137706511	-0.216456472
#CG_limits['K'] = {'CGguL': 0.870581826, 'CGguU': 1.243181211, 'CGrgL':0.209380322, 'CGrgU': 0.516802843, 'CGirL': 0.137706511, 'CGirU': 0.357449432, 'CGziL':  -0.216456472, 'CGziU':  0.074183133}
#2x sigma
#1.429480904	0.670514103	0.467320892	0.219502936
#0.684282133	0.055669061	0.027835051	-0.361776275
CG_limits['K'] = {'CGguL': 0.684282133, 'CGguU': 1.429480904, 'CGrgL':0.055669061, 'CGrgU': 0.670514103, 'CGirL': 0.027835051, 'CGirU': 0.467320892, 'CGziL':  -0.361776275, 'CGziU':  0.219502936}

#asteroid dictionary
asteroids = defaultdict(dict)

#===============================================================================
# 1     1 - 7     moID     Unique SDSS moving-object ID
# 2     8 - 13     Run     SDSS object IDs, for details see SDSS EDR paper
# 3     14 - 15     Col
# 4     16 - 20     Field
# 5     21 - 26     Object
# 6     27 - 35     rowc     Pixel row
# 7     36 - 44     colc     Pixel col
#     -- Astrometry --
# 8     47 - 59     Time (MJD)     Modified Julian Day for the mean observation time
# 9     60 - 70     R.A.     J2000 right ascension of the object at the time of the (r band) SDSS observation
# 10     71 - 81     Dec     J2000 declination of the object at the time of the (r band) SDSS observation
# 11     82 - 92     Lambda     Ecliptic longitude at the time of observation
# 12     93 - 103     Beta     Ecliptic latitude at the time of observation
# 13     104 - 115     Phi     Distance from the opposition at the time of observation
# 14     117 - 124     vMu     The velocity component parallel to the SDSS scanning direction, and its error (deg/day)
# 15     125 - 131     vMu Error
# 16     132 - 139     vNu     The velocity component perpendicular to the SDSS scanning direction, and its error (deg/day)
# 17     140 - 146     vNu Error
# 18     147 - 154
#     vLambda
#     The velocity component parallel to the Ecliptic (deg/day)
# 19     155 - 162
#     vBeta
#     The velocity component perpendicular to the Ecliptic (deg/day)
#     -- Photometry --
# 20     164 - 169     u     SDSS u'g'r'i'z' psf magnitudes and corresponding errors
# 21     170 - 174     uErr
# 22     175 - 180     g
# 23     181 - 185     gErr
# 24     186 - 191     r
# 25     192 - 196     rErr
# 26     197 - 202     i
# 27     203 - 207     iErr
# 28     208 - 213     z
# 29     214 - 218     zErr
# 30     219 - 224     a     a* color = 0.89 (g - r) + 0.45 (r - i) - 0.57 (see Paper I)
# 31     225 - 229     aErr
# 32     231 - 236     V     Johnson-V band magnitude, synthetized from SDSS magnitudes
# 33     237 - 242     B     Johnson-B band magnitude, synthetized from SDSS magnitudes
#     -- Identification --
# 34     243 - 244     Identification flag     Has this moving object been linked to a known asteroid (0/1)? See Paper II.
# 35     245 - 252     Numeration     Numeration of the asteroid. If the asteroid is not numbered, or this moving object has not yet been linked to a known asteroid, it's 0.
# 36     253 - 273     Designation     Asteroid designation or name. If this moving object has not yet been linked to a known asteroid, it's '-'
# 37     274 - 276
#     Detection Counter
#     Detection counter of this object in SDSS data
# 38     277 - 279     Total Detection Count     Total number of SDSS observations of this asteroid
# 39     280 - 288     Flags     Flags that encode SDSSMOC processing information (internal)
#     -- Matching information --
# 40     290 - 300     Computed R.A.     Predicted position and magnitude at the time of SDSS observation for an associated known object computed using ASTORB data See a note about an error in the first three releases
# 41     301 - 311     Computed Dec
# 42     312 - 317     Computed App. Mag.
# 43     319 - 326     R     Heliocentric distance at the time of observation
# 44     327 - 334     Geocentric     Geocentric distance at the time of observation
# 45     335 - 340     Phase     Phase angle at the time of observation
#     -- Osculating elements --
# 46     342 - 352     Catalog ID     Identification of the catalog from which the osculating elements and (H, G) values were extracted
# 47     363 - 368     H     Absolute magnitude and slope parameter
# 48     369 - 373     G
# 49     374 - 379     Arc     Arc of observations used to derive the elements
# 50     380 - 393     Epoch     Osculating elements
# 51     394 - 406     a
# 52     407 - 417     e
# 53     418 - 428     i
# 54     429 - 439     Lon. of asc. node
# 55     440 - 450     Arg. of perihelion
# 56     451 - 461     M
#     -- Proper elements --
# 57     463 - 483     Proper elements catalog ID     Identification of the catalog from which the proper elements were extracted
# 58     484 - 496     a'     Proper elements
# 59     497 - 507     e'
# 60     508 - 518     sin(i')
# 61-124 519 - 646     binary processing flags     Only since the 3rd release!! 
#===============================================================================

#using pandas with a column specification defined above   
col_specification =[ (0, 6), (7, 12), (13, 14), (15, 19), (20, 25), (26, 34), (35, 43), (46, 58), (59, 69), (70, 80), (81, 91), (92, 102), (103, 114), (116, 123), (124, 130), (131, 138), (139, 145), (146, 153), (154, 161), (163, 168), (169, 173), (174, 179), (180, 184), (185, 190), (191, 195), (196, 201), (202, 206), (207, 212), (213, 217), (218, 223), (224, 228), (230, 235), (236, 241), (242, 243), (244, 251), (252, 272), (273, 275), (276, 278), (279, 287), (289, 299), (300, 310), (311, 316), (318, 325), (326, 333), (334, 339), (341, 351), (362, 367), (368, 372), (373, 378), (379, 392), (393, 405), (406, 416), (417, 427), (428, 438), (439, 449), (450, 460), (462, 482), (483, 495), (496, 506), (507, 517), (518, 645)]

print 'Reading SDSS MOC data from %s...'%path

#read all lines from MOC 4 data file
#variables to process big ole MOC4 data file
skipRows = 0
nRowsMax = 100000
nRows=nRowsMax
#is this a known moving object?
id_flag = 0
#track observation and unique asteroid count
asteroid_count = 0
observation_count = 0
#log files
log=open('moc4.log.txt', 'w')
log_tax=open('moc4.tax.txt', 'w')
log_tax_final=open('moc4.tax.final.txt', 'w')
#organize the observations by asteroid
observation={}
while nRows >= nRowsMax:
    try:
        data = pd.read_fwf(path, colspecs=col_specification, skiprows=skipRows, nrows=nRowsMax, header=None)
    except:
        break
    nRows = data.shape[0]
    for irow in range(0,nRows):
        id_flag = data.iat[irow, 33]
        #is this a known asteroid?
        if id_flag == 1:
            designation = data.iat[irow, 35]
            if not asteroids.has_key(designation):
                asteroids[designation]={}
                asteroids[designation]['numeration'] = data.iat[irow, 34]
                asteroids[designation]['observations'] = []
                asteroid_count += 1
            #add a new observation to this asteroid
            observation={}
            observation['moID'] = data.iat[irow, 0]
            observation['mjd'] = float(data.iat[irow, 7])           
            observation['u'] = float(data.iat[irow, 19])
            observation['uErr'] = float(data.iat[irow, 20])                       
            observation['g'] = float(data.iat[irow, 21])
            observation['gErr'] = float(data.iat[irow, 22])
            observation['r'] = float(data.iat[irow, 23])
            observation['rErr'] = float(data.iat[irow, 24])
            observation['i'] = float(data.iat[irow, 25])
            observation['iErr'] = float(data.iat[irow, 26])
            observation['z'] = float(data.iat[irow, 27])
            observation['zErr'] = float(data.iat[irow, 28])
            observation['a'] = float(data.iat[irow, 29])
            observation['aErr'] = float(data.iat[irow, 30])
            observation['V'] = float(data.iat[irow, 31])
            observation['B'] = float(data.iat[irow, 32])
            observation['Phase'] = float(data.iat[irow, 44])
            #print observation['moID'], observation['Phase'] 
            #calc asteroid colors, relative to g-band and with solar color subtracted
            #Cxg = mx - mg - (C(solar)x - C(solar)g)
            observation['Cug'] = observation['u'] - observation['g'] - solar_color_ug
            observation['Cgg'] = -solar_color_gg
            observation['Crg'] = observation['r'] - observation['g'] - solar_color_rg
            observation['Cig'] = observation['i'] - observation['g'] - solar_color_ig
            observation['Czg'] = observation['z'] - observation['g'] - solar_color_zg
            #calc asteroid color error
            ##propagate errors using quadrature, e.g. for Cug, error is sqrt(uErr*uErr+gErr*gErr)??
            ##observation['CugErr'] = math.sqrt(observation['gErr']*observation['gErr']+observation['uErr']*observation['uErr'])
            ##observation['CggErr'] = observation['gErr']
            ##observation['CrgErr'] = math.sqrt(observation['gErr']*observation['gErr']+observation['rErr']*observation['rErr'])
            ##observation['CigErr'] = math.sqrt(observation['gErr']*observation['gErr']+observation['iErr']*observation['iErr'])
            ##observation['CzgErr'] = math.sqrt(observation['gErr']*observation['gErr']+observation['zErr']*observation['zErr'])
            #from the Carvano data, this is what it seems they are doing
            observation['CugErr'] = observation['uErr']
            observation['CggErr'] = observation['gErr']
            observation['CrgErr'] = observation['rErr']
            observation['CigErr'] = observation['iErr']
            observation['CzgErr'] = observation['zErr']          
            #calc asteroid log reflectance, relative to g-band
            #Cxg = -2.5(logRx-logRg) = -2.5(log(Rx/Rg)) = -2.5*LRx
            #LRx = LRxg = -Cxg/2.5
            observation['LRug'] = -observation['Cug']/2.5
            observation['LRgg'] = 1.0
            observation['LRrg'] = -observation['Crg']/2.5
            observation['LRig'] = -observation['Cig']/2.5
            observation['LRzg'] = -observation['Czg']/2.5
            #calc asteroid log reflectance errors by propagating the Cxg errors
            observation['LRugErr'] = observation['CugErr']/2.5
            observation['LRggErr'] = observation['CggErr']/2.5
            observation['LRrgErr'] = observation['CrgErr']/2.5
            observation['LRigErr'] = observation['CigErr']/2.5
            observation['LRzgErr'] = observation['CzgErr']/2.5            
            #calc asteroid color gradients, basis of Carvano taxonomy
            #CGx = -0.4*(Cxg-C(x-1)g)/(lambdax-lambda(x-1))
            observation['CGgu'] = -0.4*(observation['Cgg']-observation['Cug'])/(g_wavelength-u_wavelength)
            observation['CGrg'] = -0.4*(observation['Crg']-observation['Cgg'])/(r_wavelength-g_wavelength)
            observation['CGir'] = -0.4*(observation['Cig']-observation['Crg'])/(i_wavelength-r_wavelength)
            observation['CGzi'] = -0.4*(observation['Czg']-observation['Cig'])/(z_wavelength-i_wavelength)
            #observation['CGguErr'] = math.sqrt(observation['gErr']*observation['gErr']+observation['uErr']*observation['uErr'])
            #observation['CGrgErr'] = math.sqrt(observation['rErr']*observation['rErr']+observation['gErr']*observation['gErr'])
            #observation['CGirErr'] = math.sqrt(observation['iErr']*observation['iErr']+observation['rErr']*observation['rErr'])
            #observation['CGziErr'] = math.sqrt(observation['zErr']*observation['zErr']+observation['iErr']*observation['iErr'])
            #observation['CGguErr'] = observation['gErr'] + observation['uErr']
            #observation['CGrgErr'] = observation['rErr'] + observation['gErr']
            #observation['CGirErr'] = observation['iErr'] + observation['rErr']
            #observation['CGziErr'] = observation['zErr'] + observation['iErr']
            #observation['CGguErr'] = math.sqrt(observation['gErr']*observation['gErr']+observation['uErr']*observation['uErr'])*0.4/(g_wavelength-u_wavelength)
            #observation['CGrgErr'] = math.sqrt(observation['rErr']*observation['rErr']+observation['gErr']*observation['gErr'])*0.4/(r_wavelength-g_wavelength)
            #observation['CGirErr'] = math.sqrt(observation['iErr']*observation['iErr']+observation['rErr']*observation['rErr'])*0.4/(i_wavelength-r_wavelength)
            #observation['CGziErr'] = math.sqrt(observation['zErr']*observation['zErr']+observation['iErr']*observation['iErr'])*0.4/(z_wavelength-i_wavelength)
            observation['CGguErr'] = math.sqrt(observation['LRggErr']*observation['LRggErr']+observation['LRugErr']*observation['LRugErr'])/(g_wavelength-u_wavelength)
            observation['CGrgErr'] = math.sqrt(observation['LRrgErr']*observation['LRrgErr']+observation['LRggErr']*observation['LRggErr'])/(r_wavelength-g_wavelength)
            observation['CGirErr'] = math.sqrt(observation['LRigErr']*observation['LRigErr']+observation['LRrgErr']*observation['LRrgErr'])/(i_wavelength-r_wavelength)
            observation['CGziErr'] = math.sqrt(observation['LRzgErr']*observation['LRzgErr']+observation['LRigErr']*observation['LRigErr'])/(z_wavelength-i_wavelength)
            #observation['CGguErr'] = (observation['gErr']+observation['uErr'])*0.4/(g_wavelength-u_wavelength)
            #observation['CGrgErr'] = (observation['rErr']+observation['gErr'])*0.4/(r_wavelength-g_wavelength)
            #observation['CGirErr'] = (observation['iErr']+observation['rErr'])*0.4/(i_wavelength-r_wavelength)
            #observation['CGziErr'] = (observation['zErr']+observation['iErr'])*0.4/(z_wavelength-i_wavelength)
            #
            #this is for phase angle analysis (Carvano et al. 2015)
            #color gradients based on r'
            observation['Cur'] = observation['u'] - observation['r'] - solar_color_ur
            observation['Cgr'] = observation['g'] - observation['r'] - solar_color_gr
            observation['Crr'] = 0.0 #-solar_color_rr
            observation['Cir'] = observation['i'] - observation['r'] - solar_color_ir
            observation['Czr'] = observation['z'] - observation['r'] - solar_color_zr
            #from the Carvano data, this is what it seems they are doing
            observation['CurErr'] = math.sqrt(observation['uErr']*observation['uErr']+observation['rErr']*observation['rErr'])
            observation['CgrErr'] = math.sqrt(observation['gErr']*observation['gErr']+observation['rErr']*observation['rErr'])
            observation['CrrErr'] = 0.0
            observation['CirErr'] = math.sqrt(observation['iErr']*observation['iErr']+observation['rErr']*observation['rErr'])
            observation['CzrErr'] = math.sqrt(observation['zErr']*observation['zErr']+observation['rErr']*observation['rErr'])        
            #calc asteroid reflectance, relative to r-band
            #Cxr = -2.5(logRx-logRr) = -2.5(log(Rx/Rr))
            #Rx/Rr = 10^(-Cxr/2.5)
            observation['Rur'] = pow(10,-observation['Cur']/2.5)
            observation['Rgr'] = pow(10, -observation['Cgr']/2.5)
            observation['Rrr'] = 1.0
            observation['Rir'] = pow(10, -observation['Cir']/2.5)
            observation['Rzr'] = pow(10, -observation['Czr']/2.5)
            #
            #http://slittlefair.staff.shef.ac.uk/teaching/phy217/lectures/stats/L18/index.html
            observation['RurErr'] = observation['CurErr']/1.09*observation['Rur']
            observation['RgrErr'] = observation['CgrErr']/1.09*observation['Rgr']
            observation['RrrErr'] = 0.0
            observation['RirErr'] = observation['CirErr']/1.09*observation['Rir']
            observation['RzrErr'] = observation['CzrErr']/1.09*observation['Rzr']            
            
            #observation['RurErr'] = (abs(observation['Rur']*(pow(10,-observation['CurErr']/2.5)-1))+abs(observation['Rur']*(pow(10,observation['CurErr']/2.5)-1)))/2.0
            #observation['RgrErr'] = (abs(observation['Rgr']*(pow(10,-observation['CgrErr']/2.5)-1))+abs(observation['Rgr']*(pow(10,observation['CgrErr']/2.5)-1)))/2.0
            #observation['RrrErr'] = (abs(observation['Rrr']*(pow(10,-observation['CrrErr']/2.5)-1))+abs(observation['Rrr']*(pow(10,observation['CrrErr']/2.5)-1)))/2.0
            #observation['RirErr'] = (abs(observation['Rir']*(pow(10,-observation['CirErr']/2.5)-1))+abs(observation['Rir']*(pow(10,observation['CirErr']/2.5)-1)))/2.0
            #observation['RzrErr'] = (abs(observation['Rzr']*(pow(10,-observation['CzrErr']/2.5)-1))+abs(observation['Rzr']*(pow(10,observation['CzrErr']/2.5)-1)))/2.0            
            #calc slope and bd parameters from Carvano et al. 2015
            #eq 1: Rir-Rgr/(lambdai-lambdag)
            #eq 2: Rzr-Rir
            observation['slope'] = (observation['Rir']-observation['Rgr'])/(i_wavelength-g_wavelength)
            observation['slopeErr'] = math.sqrt(observation['RirErr']*observation['RirErr']+observation['RgrErr']*observation['RgrErr'])/(i_wavelength-g_wavelength)
            
            observation['bd'] = observation['Rzr'] - observation['Rir']
            observation['bdErr'] = math.sqrt(observation['RzrErr']*observation['RzrErr']+observation['RirErr']*observation['RirErr'])            
            #calc asteroid log reflectance errors by propagating the Cxg errors
            #observation['RurErr'] = ?
            #observation['RgrErr'] = ?
            #observation['RrrErr'] = ?
            #observation['RirErr'] = ?
            #observation['RzrErr'] = ?            
            #
            asteroids[designation]['observations'].append(observation)
            #print asteroids[designation]
    skipRows += nRows

print 'Read %d row(s).'%(skipRows)
print 'Found %d asteroid(s).'%asteroid_count

print 'Calculating taxonomic classes for each observation...'
log_tax.write('%s,%s,%s,%s,%s,%s,%s\n'%('designation', 'moid', 'phase', 'slope', 'slopeErr', 'bd', 'bdErr'))
for designation in asteroids:
    log.write('%s\n'%designation)
    print 'Processing observations for %s...'%designation
    for observation in asteroids[designation]['observations']:
        log.write('\t%s\t%s\t\t%s\t\t%s\t\t%s\t\t%s\t\t%s\t\t%s\t\t%s\t\t%s\t\t%s\n'%('moID', 'LRug', 'LRugErr', 'LRgg', 'LRggErr', 'LRrg', 'LRrgErr', 'LRig', 'LRigErr', 'LRzg', 'LRzgErr'))
        log.write('\t%s\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\n'%(observation['moID'], observation['LRug'], observation['LRugErr'], observation['LRgg'], observation['LRggErr'], observation['LRrg'], observation['LRrgErr'], observation['LRig'], observation['LRigErr'], observation['LRzg'], observation['LRzgErr']))
        log.write('\t%s\t\t%s\t\t%s\t\t%s\n'%('CGgu', 'CGrg', 'CGir', 'CGzi'))
        log.write('\t%f\t%f\t%f\t%f\n'%(observation['CGgu'], observation['CGrg'], observation['CGir'], observation['CGzi']))
        log.write('\t%s\t\t%s\t\t%s\t\t%s\n'%('CGguErr', 'CGrgErr', 'CGirErr', 'CGziErr'))
        log.write('\t%f\t%f\t%f\t%f\n'%(observation['CGguErr'], observation['CGrgErr'], observation['CGirErr'], observation['CGziErr']))
        log_tax.write('%s,%s,%s,%f,%f,%f,%f\n'%(designation, observation['moID'], observation['Phase'], observation['slope'], observation['slopeErr'], observation['bd'], observation['bdErr']))          
        log.write('\t***************************************\n')
        
log.close()
log_tax.close()

print 'Processed %d asteroid(s).'%asteroid_count

# 1     1 - 7     moID     Unique SDSS moving-object ID
# 8     47 - 59     Time (MJD)     Modified Julian Day for the mean observation time  
# 34     243 - 244     Identification flag     Has this moving object been linked to a known asteroid (0/1)? See Paper II.
# 35     245 - 252     Numeration     Numeration of the asteroid. If the asteroid is not numbered, or this moving object has not yet been linked to a known asteroid, it's 0.
# 36     253 - 273     Designation     Asteroid designation or name. If this moving object has not yet been linked to a known asteroid, it's '-'
# 20     164 - 169     u     SDSS u'g'r'i'z' psf magnitudes and corresponding errors
# 21     170 - 174     uErr
# 22     175 - 180     g
# 23     181 - 185     gErr
# 24     186 - 191     r
# 25     192 - 196     rErr
# 26     197 - 202     i
# 27     203 - 207     iErr
# 28     208 - 213     z
# 29     214 - 218     zErr
# 30     219 - 224     a     a* color = 0.89 (g - r) + 0.45 (r - i) - 0.57 (see Paper I)
# 31     225 - 229     aErr
# 32     231 - 236     V     Johnson-V band magnitude, synthetized from SDSS magnitudes
# 33     237 - 242     B     Johnson-B band magnitude, synthetized from SDSS magnitudes
  