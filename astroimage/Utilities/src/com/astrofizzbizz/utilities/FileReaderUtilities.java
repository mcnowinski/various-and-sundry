package com.astrofizzbizz.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class FileReaderUtilities 
{
	public static final DecimalFormat twoPlaces = new DecimalFormat("###.##");
	public static final DecimalFormat fourPlaces = new DecimalFormat("###.####");
	public static final DecimalFormat zeroPlaces = new DecimalFormat("###");
	public static final String delim = System.getProperty("file.separator");
	public static final String newline = System.getProperty("line.separator");
	public static String[] readFile(String aFileName) throws IOException
	{
		File aFile = new File(aFileName);
		BufferedReader input =  new BufferedReader(new FileReader(aFile));
    	ArrayList<String> outputBuffer = new ArrayList<String>();
		try 
		{
			String line = null; //not declared within while loop
			while (( line = input.readLine()) != null)
			{
				outputBuffer.add(line);
			}
		}
		finally 
		{
			input.close();
		}
		int nlines = outputBuffer.size();
		String[] status = null;
		if (nlines < 1) 
		{
			status = new String[1];
			status[0] = "";
		}
		else
		{
			status = new String[nlines];
			for (int il = 0; il < nlines; ++il)
			{
				status[il] = outputBuffer.get(il);
			}
		}
 	    return status;
	}
	public static ArrayList<String[]> readCsvFile(String csvFileName) throws IOException
	{
		String[] fileData = readFile(csvFileName);
    	ArrayList<String[]> outputBuffer = new ArrayList<String[]>();
		for (int il = 0; il < fileData.length; ++il)
		{
			outputBuffer.add(fileData[il].split(","));
		}
		return outputBuffer;
	}
	public static String stripWhiteSpaces(String whitey)
	{
		int numChar = 0;
		for (int ii = 0; ii < whitey.length(); ++ii)
		{
			if (whitey.charAt(ii) != ' ') numChar = numChar + 1;
		}
		if (numChar == 0) return "";
		char[] slimJimArray = new char[numChar];
		int iChar = 0;
		for (int ii = 0; ii < whitey.length(); ++ii)
		{
			if (whitey.charAt(ii) != ' ') 
			{
				slimJimArray[iChar] = whitey.charAt(ii);
				iChar = iChar + 1;
			}
		}
		return new String(slimJimArray);
	}
	public static void main(String[] args) throws IOException 
	{
		String directory = "C:\\Dropbox\\McGinnisFiles\\ESS\\TraceWin\\ScOnly";
		String fileName = "paramVary.csv";
		ArrayList<String[]> csvData = readCsvFile(directory + delim + fileName);
		for (int il = 0; il < csvData.size(); ++il)
		{
			for (int ic = 0; ic < csvData.get(il).length; ++ic)
			{
				System.out.print(csvData.get(il)[ic] + "\t");
			}
			System.out.print("\n");
		}
	}
}
