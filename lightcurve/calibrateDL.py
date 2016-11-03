#
import win32com.client
import time
app=win32com.client.Dispatch("Maxim.Application")
app.CreateCalibrationGroups('%s;%s;%s'%(bias_path,dark_path,flat_path),2,0,False) #call maxim's command to create calibration group
for image in fits_files:
    app.CloseAll() #close all files open in maxim
    doc.OpenFile("%s/%s"%(x,image)) #open single image
    doc.Calibrate() #calibrate image
		