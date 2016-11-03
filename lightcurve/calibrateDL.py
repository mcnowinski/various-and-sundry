##calibrateDL.py##calibrate fits images in MaximDL using darks, flats, and bias frames
import win32com.clientimport osfrom astropy.io import fits
import time#calibration frame relative pathsinput_path='./'dark_path='./dark/'bias_path='./bias/'flat_path='./flat/'#MaximDL win32 COM interfacedoc=win32com.client.Dispatch("MaxIM.Document")
app=win32com.client.Dispatch("Maxim.Application")cur_path = os.getcwd()
app.CreateCalibrationGroups('%s;%s;%s'%(bias_path,dark_path,flat_path),2,0,False) #call maxim's command to create calibration group#get a list of all FITS files in the input directoryfits_files=glob.glob(input_path+'*.fits')+glob.glob(input_path+'*.fit')
for image in fits_files:
    app.CloseAll() #close all files open in maxim
    doc.OpenFile("%s/%s"%(x,image)) #open single image
    doc.Calibrate() #calibrate image    doc.SaveFile("C:/reduce/reduced_prehold30/%s.REDUCED.fit"%(image[:-3]),3,True,1,0)    doc.close
		