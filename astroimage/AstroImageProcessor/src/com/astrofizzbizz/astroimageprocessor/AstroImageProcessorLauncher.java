package com.astrofizzbizz.astroimageprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

//import javax.swing.JOptionPane;

//import com.astrofizzbizz.utilities.CodeUpdateInformer;
import com.astrofizzbizz.utilities.WaitFrame;

public class AstroImageProcessorLauncher 
{
	public static int launch(int maxHeap, int minHeap, String pathToJar, String mainClass, String args) throws IOException 
	{
		System.gc();
		System.out.println(System.getProperties().toString());
		String delim = System.getProperty("file.separator");
		String javaPath = System.getProperty("java.home") + delim + "bin"+ delim + "java";
		javaPath.replaceAll(" ", "\\ ");
		
		ProcessBuilder pb = new ProcessBuilder(javaPath,"-Xmx" + maxHeap +"m", "-Xms" + minHeap +"m","-classpath", pathToJar, mainClass, args);
		Process process = pb.start();
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String read = br.readLine();
		if (read.equals(args)) return 1;
		if (read.equals("Error occurred during initialization of VM"))
		{
			read = br.readLine();
			if (read.equals("Could not reserve enough space for object heap")) return -1;
		}
		System.out.println(read);
		return 0;
	}
	public static void launchLoop(int maxHeap, int minHeap, String pathToJar, String mainClass) throws IOException 
	{
		String args = "test";
		int test = launch(maxHeap, minHeap, pathToJar, mainClass, args);
		while (test < 0)
		{
			maxHeap = maxHeap - 100;
			if (maxHeap > minHeap)
			{
				test = launch(maxHeap, minHeap, pathToJar, mainClass, args);
			}
			else
			{
				test = 0;
			}
		}
		if (test < 1)
		{
			new WaitFrame("Error", mainClass, null);
		}
	}
	public static void bruteForceLauncher()
	{
		String path = System.getProperty("user.dir") + System.getProperty("file.separator") + "AstroImageProcessor.jar";
		String[] errStatus = null;
		try {
			Runtime.getRuntime().exec("java -Xmx1024m -Xms1024m -jar " + path );
		} catch (IOException e) {
			errStatus = new String[1];
			errStatus[0] = e.getMessage();
		}
		System.exit(0);
		return ;
	}

	public static void main(String[] args)
	{
		int maxHeap = 1024;
		int minHeap = 512;
		//String pathToJar = "AstroImageProcessorV3_4.jar";
		String mainClass = "com.astrofizzbizz.astroimageprocessor.AstroImageProcessorGui";
        //String codeURL = "https://drive.google.com/file/d/0B3Hieedgs_7FZENJelh6WE56OGs/edit?usp=sharing";  
        //String downloadURL = "https://drive.google.com/file/d/0B3Hieedgs_7FZENJelh6WE56OGs/edit?usp=sharing"; 
        	
		//get name of current jar file, just in case it got renamed
		String path = AstroImageProcessorLauncher.class.getResource(AstroImageProcessorLauncher.class.getSimpleName() + ".class").getFile();
		if(path.startsWith("/")) {
        	new WaitFrame("AstroImageProcessor", "Error. This class must be launched from a .jar file.", null);			
			return;
		}
		File jarFile = new File(path.substring(0, path.lastIndexOf('!')));	
		String pathToJar = jarFile.getName();
		
		//disable the updater
		//CodeUpdateInformer codeUpdateInformer = new CodeUpdateInformer(codeURL, downloadURL, null);
        //if (!codeUpdateInformer.isNewCodeDownloaded())
        //{
			try 
			{
				launchLoop(maxHeap, minHeap, pathToJar, mainClass);
			}  catch (Exception e) 
			{
				new WaitFrame("AstroImageProcessor", e.getMessage(), null);
			}
        //}
	}

}
