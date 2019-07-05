package asteroids;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
//import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

import javax.swing.JFileChooser;

public class Horizons {
	
    static final String apMagName = "APmag";
    static final String phaseAngName = "S-T-O";
    static final String rightAscensionName = "R.A._(ICRF/J2000.0)";
    static final String declinationName = "DEC_(ICRF/J2000.0)";    
    static final double apMagMin = 17.0;	//only process days where apparent magnitude is brighter than this number	
    static final int stepSizeMin = 15;	//time step size in minutes of ephemeris 2018/08/25
//	static final LocalDateTime dtStart = LocalDateTime.of(2018, 8, 25, 9, 55); //reference start date (e.g. beginning of IRTF session)
//	static final LocalDateTime dtStart = LocalDateTime.of(2018, 9, 20, 6, 45); //reference start date (e.g. beginning of IRTF session)
	static final LocalDateTime dtStart = LocalDateTime.of(2020, 01, 01, 0, 0); //reference start date (e.g. beginning of IRTF session)

	static String cookieFile = "_Horizons.txt";
	
	public int id = 1;
	
	//constructor
	public Horizons() {
	}

//	//parse file
//	public String getHeader(File inputFile) {
//
//        Pattern r;
//        Matcher m;
//        String csvHeader = "";	
//		
//		try {
//
//			//does file exist?
//			if(inputFile.isDirectory() || !inputFile.exists()) { 
//				System.out.println("Input file (" + inputFile.getAbsolutePath() + ") does not exist!");
//			    return csvHeader;
//			}
//			
//	        Scanner sc = new Scanner(inputFile);
//	        while(sc.hasNextLine()) {
//	            String line = sc.nextLine();                      
//                
//                //csv headers?
//                // Date__(UT)__HR:MN
//                r = Pattern.compile("^\\sDate__\\(UT\\)__HR\\:MN");
//                m = r.matcher(line);
//                if(m.find()) {
//                	csvHeader = line.trim();
//                	//System.out.println(csvHeader);
//                	//add target name
//                	return "Target Name," + csvHeader;
//                }           
//	        }
//	        sc.close();
//	        
//        } catch (Exception e) {         
//            e.printStackTrace();
//        }
//		
//		//System.out.println("Got nothing!");
//        return csvHeader;
//	}	
	
	public class HorizonsData {
		
		private String csv;
		private String csvHeader;		
		private String gantt;
		private String target;
		//private int apMagIndex; //column index for apparent magnitude
		//private int phaseAngIndex; //column index for phase angle
		
		//constructor
		public HorizonsData(String target, String csv, String gantt, String csvHeader/*, int apMagIndex*/) {
			this.target = target;
			this.csv = csv;
			this.csvHeader = csvHeader;
			this.gantt = gantt;
			//this.apMagIndex = apMagIndex;
		}
		
		public String getCsv() {
			return csv;
		}

		public String getCsvHeader() {
			return csvHeader;
		}	
		
		public String getGantt() {
			return gantt;
		}
		
		public String getTarget() {
			return target;
		}
		
//		public double getApMagIndex() {
//			return apMagIndex;
//		}		
	}
	
	//parse file
	public HorizonsData getData(File inputFile) {

        //Pattern r;
        Matcher m;
        String csv = "";
        String gantt = "";
        String csvHeader = "";
        boolean isCsv = false;
        String target = "";
        int apMagIndex = -1;
        int phaseAngIndex = -1;
        int rightAscensionIndex = -1;
        int declinationIndex = -1;
        
        //apparent magnitude
        double apMagAve = 0.0, apMagMinimum = 100.0;
        LocalDateTime dt_apMagMin = LocalDateTime.now();
        //phase angle
        double phaseAngAve = 0.0, phaseAngMinimum = 360.0, phaseAngMaximum = -360.0;
        LocalDateTime dt_phaseAngMin = LocalDateTime.now();
        LocalDateTime dt_phaseAngMax = LocalDateTime.now();
        
        String ra_phaseAngMin = "";
        String dec_phaseAngMin = "";
        String ra_phaseAngMax = "";
        String dec_phaseAngMax = "";
        
        int count = 0;
               
        LocalDateTime dt_start = LocalDateTime.now();
        LocalDateTime dt_last = LocalDateTime.now();
        LocalDateTime dt = LocalDateTime.now();
        int days = 0, steps = 0;
        boolean firstEntry = true;
		
		try {
			//does file exist?
			if(inputFile.isDirectory() || !inputFile.exists()) { 
				System.out.println("Input file (" + inputFile.getAbsolutePath() + ") does not exist!");
			    return new HorizonsData(target, csv, gantt, csvHeader/*, apMagIndex*/);
			}
			
	        Scanner sc = new Scanner(inputFile);
	        //precompile these pattern (better performance)
	        Pattern rTarget = Pattern.compile("^Target body name: ([0-9A-Za-z\\-\\(\\)\\s]+)");
	        Pattern rDate = Pattern.compile("^\\sDate__\\(UT\\)__HR\\:MN");
	        Pattern rCsvEnd = Pattern.compile("^\\$\\$EOE");
	        Pattern rCsv = Pattern.compile("^([12][0-9][0-9][0-9]\\-[A-Za-z][A-Za-z][A-Za-z]\\-[0-9][0-9])");
	        Pattern rCsvStart =Pattern.compile("^\\$\\$SOE");
	        while(sc.hasNextLine()) {
	            String line = sc.nextLine();       

	            //target name?
                //r = Pattern.compile("^Target body name: ([0-9A-Za-z\\-\\(\\)\\s]+)");
	            if(target.length() == 0) {
	                m = rTarget.matcher(line);
	                if(m.find()) {
	                	target = m.group(1).trim();
	                	//System.out.println(target);
	                } 
	            }
                
                //csv headers?
                // Date__(UT)__HR:MN
                //r = Pattern.compile("^\\sDate__\\(UT\\)__HR\\:MN");
	            if(apMagIndex == -1) {
	                m = rDate.matcher(line);
	                if(m.find()) {
	                	csvHeader = line.trim();
	                	//find ApMag column
	                	String [] fields = csvHeader.split(",");
	                	for(int col=0; col<fields.length; col++) {
	                		if(fields[col].trim().equals(apMagName)) {
	                			apMagIndex = col;
	                			//System.out.println(apMagIndex);
	                			break;
	                		}
	                	}
	                	if(apMagIndex == 0) {
	                		System.out.println("Error. Apparent magnitude column not found!");
	                	}
	                	//find phaseAng column
	                	for(int col=0; col<fields.length; col++) {
	                		if(fields[col].trim().equals(phaseAngName)) {
	                			phaseAngIndex = col;
	                			//System.out.println(phaseAngIndex);
	                			break;
	                		}
	                	}
	                	if(phaseAngIndex == 0) {
	                		System.out.println("Error. S-T-O phase angle column not found!");
	                	}
	                	//find right ascension column
	                	for(int col=0; col<fields.length; col++) {
	                		if(fields[col].trim().equals(rightAscensionName)) {
	                			rightAscensionIndex = col;
	                			//System.out.println(rightAscensionIndex);
	                			break;
	                		}
	                	}
	                	if(rightAscensionIndex == 0) {
	                		System.out.println("Error. Right ascension column not found!");
	                	}
	                	//find declination column
	                	for(int col=0; col<fields.length; col++) {
	                		if(fields[col].trim().equals(declinationName)) {
	                			declinationIndex = col;
	                			//System.out.println(declinationIndex);
	                			break;
	                		}
	                	}
	                	if(declinationIndex == 0) {
	                		System.out.println("Error. Declination column not found!");
	                	}
	                	csvHeader = "Target Name," + line.trim() + "\n";
	                	//System.out.println(csvHeader);
	                } 
	            }
                
                //csv end?
                //r = Pattern.compile("^\\$\\$EOE");
	            if(isCsv) {
	                m = rCsvEnd.matcher(line);
	                if(m.find()) {
	                	isCsv = false;
	                	//System.out.println("CSV data end!");
	                }
	        	}
                
                if(isCsv) {
                    //r = Pattern.compile("^([12][0-9][0-9][0-9]\\-[A-Za-z][A-Za-z][A-Za-z]\\-[0-9][0-9]\\s[0-2][0-9]\\:[0-5][0-9])");
                    //r = Pattern.compile("^([12][0-9][0-9][0-9]\\-[A-Za-z][A-Za-z][A-Za-z]\\-[0-9][0-9])");
                	m = rCsv.matcher(line.trim());
                    if(m.find()) {
                    	String [] fields = line.trim().split(",");
                    	//filter by ap mag
                    	if(apMagIndex >= 0 && apMagIndex < fields.length && Double.parseDouble(fields[apMagIndex]) <= apMagMin) {
                    		count++; //count entries matching magnitude constraint
	                		csv += target + "," + line.trim() + "\n";
	                		dt = LocalDateTime.parse(m.group(1) + " 00:00", DateTimeFormatter.ofPattern("uuuu-MMM-dd HH:mm"));
	                		//track ave and min (brightest) apMag
                    		apMagAve += Double.parseDouble(fields[apMagIndex]);
                    		if(Double.parseDouble(fields[apMagIndex]) < apMagMinimum) {
                    			apMagMinimum = Double.parseDouble(fields[apMagIndex]);
                    			dt_apMagMin = dt; 
                    		}
                    		//track ave and min phaseAng
                    		phaseAngAve += Double.parseDouble(fields[phaseAngIndex]);
                    		if(Double.parseDouble(fields[phaseAngIndex]) < phaseAngMinimum) {
                    			phaseAngMinimum = Double.parseDouble(fields[phaseAngIndex]);
                    			dt_phaseAngMin = dt;
                    			ra_phaseAngMin = fields[rightAscensionIndex].trim();
                    			dec_phaseAngMin = fields[declinationIndex].trim();                    			
                    		}
                    		if(Double.parseDouble(fields[phaseAngIndex]) > phaseAngMaximum) {
                    			phaseAngMaximum = Double.parseDouble(fields[phaseAngIndex]);
                    			dt_phaseAngMax = dt;
                    			ra_phaseAngMax = fields[rightAscensionIndex].trim();
                    			dec_phaseAngMax = fields[declinationIndex].trim();                    			
                    		}
	                		if(firstEntry) {
	                			dt_start = dt_last = dt;
	                			days = steps = 1;
	                			firstEntry = false;
	                    		//System.out.println("Start datetime is " + dt_start);                 			
	                		} else {
		                		if(Duration.between(dt_last, dt).toDays() > 1) {
		                			apMagAve = apMagAve/count;
		                			phaseAngAve = phaseAngAve/count;
		                			//System.out.println(dt_start + " to " + dt_last + " = " + days + " days = " + steps + " steps.");
		                			gantt += 	id++ + "," +
		                						id + "," +
		                						target + "," + 
		        								dt_start.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")) + "," +
		        								dt_last.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")) + "," +
		        								Duration.between(dtStart, dt_start).toDays()  + "," +
		        								days + "," + ((double)steps*stepSizeMin/60.0)  + "," + String.format("%.1f", ((double)steps*stepSizeMin/60.0)/(double)days) + "," +
												inputFile.getName() + "," +
		        								//apMag
												String.format("%.2f", apMagAve) + "," +
												String.format("%.2f", apMagMinimum) + "," +
												dt_apMagMin.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")) + "," +
    											Duration.between(dtStart, dt_apMagMin).toDays()  +  "," +
												//phaseAng
												String.format("%.2f", phaseAngAve) + "," +
												//phaseAngMin
												String.format("%.2f", phaseAngMinimum) + "," +
												dt_phaseAngMin.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")) + "," +
												Duration.between(dtStart, dt_phaseAngMin).toDays()  +  "," +
												ra_phaseAngMin + "," +
												dec_phaseAngMin + "," +
												//phaseAngMax
												String.format("%.2f", phaseAngMaximum) + "," +
												dt_phaseAngMax.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")) + "," +
												Duration.between(dtStart, dt_phaseAngMax).toDays()  +  "," +
												ra_phaseAngMax + "," +
												dec_phaseAngMax + "\n";	          
		                			apMagAve = 0.0;
		                			phaseAngAve = 0.0;
		                			count = 0;
		                			apMagMinimum = 100.0;
		                			phaseAngMinimum = 360.0;
		                			phaseAngMaximum = -360.0;
		                			dt_start = dt_last = dt;
		                			days = steps = 1;
		                		} else if(Duration.between(dt_last, dt).toDays() == 1) {
		                			dt_last = dt;
		                			days++;
		                			steps++;
		                		} else if(Duration.between(dt_last, dt).toDays() == 0) {
		                			steps++;
		                		}
	                		}
                    	}	
	                	//System.out.println(line);                		
                    } else {
                    	//System.out.println("Skipped line: " + line);
                    }	
                	//System.out.println(target + "," + line.trim());
                }                 
                
                //csv start?
                //r = Pattern.compile("^\\$\\$SOE");
                if(!isCsv) {
	                m = rCsvStart.matcher(line);
	                if(m.find()) {
	                	isCsv = true;
	                	//System.out.println("CSV data start!");
	                }
                }
	        }

    		if(Duration.between(dt_last, dt).toDays() < 1) {
    			//System.out.println(dt_start + " to " + dt_last + " = " + days + " days = " + steps + " steps.");
    			if(days > 0) {
        			apMagAve = apMagAve/count;
        			phaseAngAve = phaseAngAve/count;     			
        			//System.out.println(dt_start + " to " + dt_last + " = " + days + " days = " + steps + " steps.");
        			gantt += 	id++ + "," +
    							id + "," +        			
        						target + "," + 
								dt_start.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")) + "," +
								dt_last.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")) + "," +
								Duration.between(dtStart, dt_start).toDays()  + "," +
								days + "," + ((double)steps*stepSizeMin/60.0)  + "," + String.format("%.1f", ((double)steps*stepSizeMin/60.0)/(double)days) + "," +
								inputFile.getName() + "," +
								//apMag
								String.format("%.2f", apMagAve) + "," +
								String.format("%.2f", apMagMinimum) + "," +
								dt_apMagMin.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")) + "," +
								Duration.between(dtStart, dt_apMagMin).toDays()  +  "," +
								//phaseAng
								String.format("%.2f", phaseAngAve) + "," +
								//phaseAngMin
								String.format("%.2f", phaseAngMinimum) + "," +
								dt_phaseAngMin.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")) + "," +
								Duration.between(dtStart, dt_phaseAngMin).toDays()  +  "," +
								ra_phaseAngMin + "," +
								dec_phaseAngMin + "," +
								//phaseAngMax
								String.format("%.2f", phaseAngMaximum) + "," +
								dt_phaseAngMax.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")) + "," +
								Duration.between(dtStart, dt_phaseAngMax).toDays()  +  "," +
								ra_phaseAngMax + "," +
								dec_phaseAngMax + "\n";	      		
        		}	
    		}	        
	        sc.close();
	        
	    } catch (Exception e) {         
            e.printStackTrace();
        }
		
		return new HorizonsData(target, csv, gantt, csvHeader/*, apMagIndex*/);
	}
	
	//main
    public static void main(String[] args) {
    	
    	//String path = "C:\\Users\\Me\\ownCloud\\horizons.lightcurve\\";
    	//allow user to choose folder containing asteroid images
		String path;
		JFileChooser chooser = new JFileChooser();
		//check for record of last folder opened
		String curDir = ".";
		try {
			File cookie = new File(cookieFile);
			if(cookie.exists() && cookie.isFile()) { 
		        Scanner sc = new Scanner(cookie);
		        curDir = sc.nextLine();
		        sc.close();
			}
	    } catch (Exception e) {         
	        e.printStackTrace();
	    }
	    chooser.setCurrentDirectory(new java.io.File(curDir));
	    chooser.setDialogTitle("Select a folder containing your Horizon's data.");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);
	    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	      path = chooser.getSelectedFile().toString();
	    } else {
	      System.out.println("Error. No folder selected.");
	      return;
	    }
	    if(!path.substring(path.length()-1, path.length()).equals("\\"))
	    	path += "\\";
	    //save this path for next time
		try {
	        //open output file
	        FileWriter fw = new FileWriter(cookieFile);
	        fw.write(path);
			fw.close();
	    } catch (Exception e) {         
	        e.printStackTrace();
	    }	    
    	//int id = 1;
    	
    	File dir = new File(path);
    	File [] files = dir.listFiles(new FilenameFilter() {
    	    @Override
    	    public boolean accept(File dir, String name) {
    	        return name.endsWith(".txt");
    	    }
    	});

    	try {
    		if(files.length == 0) {
    			System.out.println("No input files found in " + path + ".");
    			return;
    		}
    		
    		System.out.println("Processing " + files.length + " file(s) in " + path + ".");
    		        
	        //open gantt output file
	        FileWriter outputFileGantt = new FileWriter(path + "horizons_gantt." + apMagMin + ".csv");
	        //header
	        outputFileGantt.write("INDEX,ID,TARGET,START,END,START_DAYS,DURATION_DAYS,DURATION_HOURS,AVE_HOURS_PER_DAY,FILENAME,APMAG_AVE,APMAG_MIN,DATE_APMAG_MIN,DATE_APMAG_MIN_DAYS,PHASEANG_AVE,PHASEANG_MIN,DATE_PHASEANG_MIN,DATE_PHASEANG_MIN_DAYS,RA_HMS,DEC_HMS,PHASEANG_MAX,DATE_PHASEANG_MAX,DATE_PHASEANG_MAX_DAYS,RA_HMS,DEC_HMS\n");
	        
	        Horizons horizons = new Horizons();
//	    	//get header
//	    	String csvHeader = "";
//	    	for (File inputFile : files) {
//		    	csvHeader = horizons.getHeader(inputFile);
//		    	if(csvHeader.length() > 0) {
//		        	//System.out.println(csvHeader);
//		    		outputFileData.write(csvHeader + "\n");
//		        	break;
//		    	}	
//	    	}
	    	
        	//get data
	    	int count = 0;
	    	for (File inputFile : files) {  		
	    		
	    		count++;
	    		//System.out.println(inputFile.getAbsolutePath());
	    		HorizonsData horizonsData = horizons.getData(inputFile);
	    		System.out.println("Processed " + horizonsData.getTarget()+ " (" + inputFile.getName() + ").");
	    		
	    		//header
	        	String csvHeader = horizonsData.getCsvHeader();
		    	if(csvHeader.length() == 0) {
		    		System.out.println("Error. No csv header found for " + horizonsData.getTarget() + "(" + inputFile.getName() + ").");
		    	}
		    	
		    	//data
	        	String csvData = horizonsData.getCsv();
	        	if(csvHeader.length() > 0 && csvData.length() > 0) {
		    		//open data output file
			        FileWriter outputFileData = new FileWriter(path + horizonsData.getTarget() + "." + inputFile.getName() + "." + apMagMin + ".csv");	        		
	        		outputFileData.write(csvHeader);
	        		outputFileData.write(csvData);
			    	outputFileData.close();	        		
	        	}
	        	//System.out.println(csv);
	        	
	        	//gannt
	        	String gantt = horizonsData.getGantt();
	        	if(gantt.length() > 0) {
	        		//outputFileGantt.write(id++ + "," + gantt + "," + inputFile.getName() + "\n");
	        		outputFileGantt.write(/*id++ + "," + */gantt);	        		
		        	//System.out.println(gantt);
	        	} else {
	        		System.out.println("No available days found for " + horizonsData.getTarget() + "(" + inputFile.getName() + ").");
	        	}

	    	} 

	    	outputFileGantt.close();
	    	System.out.println("Processed " + count + " input files.");
	    	
        } catch (Exception e) {         
            e.printStackTrace();
        }
    }	
	
}