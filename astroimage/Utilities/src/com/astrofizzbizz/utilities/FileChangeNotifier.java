package com.astrofizzbizz.utilities;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Date;
import java.util.List;

public class FileChangeNotifier
{
  public static void main(String args[]) throws IOException, InterruptedException
  {
     watchDir("C:\\EclipseWorkSpace2014\\LinacLego\\EssLinacXmlFiles");		// Monitor changes to the files in E:\MyFolder
  }
	public static void watchDir(String dir) throws IOException, InterruptedException
	{
		WatchService service = FileSystems.getDefault().newWatchService();	// Create a WatchService
		Path path = Paths.get(dir);	// Get the directory to be monitored
		path.register(service,
			StandardWatchEventKinds.ENTRY_CREATE,
			StandardWatchEventKinds.ENTRY_MODIFY,
			StandardWatchEventKinds.ENTRY_DELETE);	// Register the directory
		Date oldDate = new Date();
		while(true)
		{
			WatchKey key = service.take();	// retrieve the watchkey
			List<WatchEvent<?>> event = key.pollEvents();
			Date newDate = new Date();
			if ((newDate.getTime() - oldDate.getTime()) > 1000)
			{
				for (int ii = 0; ii < event.size(); ++ii)
				{
					System.out.println(event.get(ii).kind() + ": "+ event.get(ii).context());	// Display event and file name
				}
				oldDate.setTime(newDate.getTime());
			}
			boolean valid = key.reset();
			if (!valid) {break;}
		}
  }
}