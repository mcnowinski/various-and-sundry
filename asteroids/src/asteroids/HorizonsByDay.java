package asteroids;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.time.LocalTime;
import java.time.LocalDate;

import javax.swing.JFileChooser;

public class HorizonsByDay {
	
    static final String apMagName = "APmag";
    static final String phaseAngName = "S-T-O";
    static final String rightAscensionName = "R.A._(ICRF/J2000.0)";
    static final String declinationName = "DEC_(ICRF/J2000.0)";    
    static final String elevationName = "Elev_(a-app)";  
    static final String moonPhaseAngName = "T-O-M"; //where is the moon?
    static final String moonIllumName = "MN_Illu%"; //where is the moon?
    static final String galacticLatName = "GlxLat";
    static final String raName = "R.A._(ICRF/J2000.0)";
    static final String decName = "DEC_(ICRF/J2000.0)";    
    static final double apMagMin = 17.0;	//only process days where apparent magnitude is brighter than this number	
    static final int stepSizeMin = 15;	//time step size in minutes of ephemeris
	//static final LocalDateTime dtStart = LocalDateTime.of(2016, 12, 30, 0, 0); //reference start date (e.g. beginning of IRTF session)
	static final LocalDateTime dtStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).plusDays(1);
	
	public int id = 1;
	
	//constructor
	public HorizonsByDay() {
	}

	public class HorizonsByDayData {
		
		private String csv;
		private String csvHeader;		
		private String gantt;
		private String target;
		
		//constructor
		public HorizonsByDayData(String target, String csv, String gantt, String csvHeader/*, int apMagIndex*/) {
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
	}
	
	//parse file
	public HorizonsByDayData getData(File inputFile) {

        Matcher m;
        String csv = "";
        String gantt = "";
        String csvHeader = "";
        boolean isCsv = false;
        String target = "";
        int apMagIndex = -1;
        int phaseAngIndex = -1;
        int moonPhaseAngIndex = -1;
        int moonIllumIndex = -1;
        int rightAscensionIndex = -1;
        int declinationIndex = -1;
        int elevationIndex = -1;
        int galacticLatIndex = -1;
        int raIndex = -1;
        int decIndex = -1;
        int ibpIndex = -1; //INTERFERING BODYLUNAR PRESENCE
        double apMag = 0.0;
        double brightestApMag = 100.0;
        String dateBrightestApMag = "";
		double moonPhaseAngBrightestApMag = 0.0;
		double moonIllumBrightestApMag = 0.0;
        double phaseAng = 0.0;
        double moonPhaseAng = 0.0;
        double moonIllum = 0.0;
        double elevMax = 0.0, elevMin = 0.0;  
        double galacticLat = 0.0;
        String ra = "";
        String dec = "";
        String ibp = "";
               
        LocalDateTime dt_start = LocalDateTime.now();
        LocalDateTime dt;
        int steps = 0;
        boolean foundMatch = false;
        
		try {
			//does file exist?
			if(inputFile.isDirectory() || !inputFile.exists()) { 
				System.out.println("Input file (" + inputFile.getAbsolutePath() + ") does not exist!");
			    return new HorizonsByDayData(target, csv, gantt, csvHeader/*, apMagIndex*/);
			}
			
	        Scanner sc = new Scanner(inputFile);
	        //precompile these pattern (better performance)
	        Pattern rTarget = Pattern.compile("^Target body name: ([0-9A-Za-z\\-\\(\\)\\s]+)");
	        Pattern rDate = Pattern.compile("^\\sDate__\\(UT\\)__HR\\:MN");
	        Pattern rCsvEnd = Pattern.compile("^\\$\\$EOE");
	        Pattern rCsv = Pattern.compile("^([12][0-9][0-9][0-9]\\-[A-Za-z][A-Za-z][A-Za-z]\\-[0-9][0-9]\\s[0-9][0-9]\\:[0-9][0-9])");
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
	                	if(apMagIndex == -1) {
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
	                	if(phaseAngIndex == -1) {
	                		System.out.println("Error. S-T-O phase angle column not found!");
	                	}
	                	//find moonPhaseAngIndexcolumn
	                	for(int col=0; col<fields.length; col++) {
	                		if(fields[col].trim().equals(moonPhaseAngName)) {
	                			moonPhaseAngIndex = col;
	                			//System.out.println(phaseAngIndex);
	                			break;
	                		}
	                	}
	                	if(moonPhaseAngIndex == -1) {
	                		System.out.println("Error. Moon-T-O phase angle column not found!");
	                	}
	                	//find moonIllum column
	                	for(int col=0; col<fields.length; col++) {
	                		if(fields[col].trim().equals(moonIllumName)) {
	                			moonIllumIndex = col;
	                			//System.out.println(phaseAngIndex);
	                			break;
	                		}
	                	}
	                	if(moonIllumIndex == -1) {
	                		System.out.println("Error. Moon Illum% column not found!");
	                	}
	                	//find right ascension column
	                	for(int col=0; col<fields.length; col++) {
	                		if(fields[col].trim().equals(rightAscensionName)) {
	                			rightAscensionIndex = col;
	                			//System.out.println(rightAscensionIndex);
	                			break;
	                		}
	                	}
	                	if(rightAscensionIndex == -1) {
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
	                	if(declinationIndex == -1) {
	                		System.out.println("Error. Declination column not found!");
	                	}
	                	//find elevation column
	                	for(int col=0; col<fields.length; col++) {
	                		if(fields[col].trim().equals(elevationName)) {
	                			elevationIndex = col;
	                			//System.out.println(declinationIndex);
	                			break;
	                		}
	                	}
	                	if(elevationIndex == -1) {
	                		System.out.println("Error. Elevation column not found!");
	                	}
	                	//find galactic lat
	                	for(int col=0; col<fields.length; col++) {
	                		if(fields[col].trim().equals(galacticLatName)) {
	                			galacticLatIndex = col;
	                			//System.out.println(declinationIndex);
	                			break;
	                		}
	                	}
	                	if(galacticLatIndex == -1) {
	                		System.out.println("Error. Galactic latitude column not found!");
	                	}
	                	//find RA
	                	for(int col=0; col<fields.length; col++) {
	                		if(fields[col].trim().equals(raName)) {
	                			raIndex = col;
	                			ibpIndex = raIndex-1;
	                			break;
	                		}
	                	}
	                	if(raIndex == -1) {
	                		System.out.println("Error. RA column not found!");
	                	}
	                	//find DEC
	                	for(int col=0; col<fields.length; col++) {
	                		if(fields[col].trim().equals(decName)) {
	                			decIndex = col;
	                			break;
	                		}
	                	}
	                	if(decIndex == -1) {
	                		System.out.println("Error. DEC column not found!");
	                	}	                	
	                	csvHeader = "Target Name," + line.trim() + "\n";
	                	//System.out.println(csvHeader);
	                } 
	            }
                
                //csv end?
                //r = Pattern.compile("^\\$\\$EOE");
	            if(isCsv) {
	            	//System.out.println(line);
	                m = rCsvEnd.matcher(line);
	                if(m.find()) {
	                	isCsv = false;
	                	//System.out.println("CSV data end!");
	                }
	        	}
                
                if(isCsv) {
                	m = rCsv.matcher(line.trim());
                    if(m.find()) {
                    	//System.out.println("Found csv entry!");
                    	String [] fields = line.trim().split(",");
                    	//filter by ap mag
                    	if(apMagIndex >= 0 && apMagIndex < fields.length && Double.parseDouble(fields[apMagIndex]) <= apMagMin) {
                    		//count++; //count entries matching magnitude constraint
	                		dt = LocalDateTime.parse(m.group(1), DateTimeFormatter.ofPattern("uuuu-MMM-dd HH:mm"));
	                		//within a day in the future of dtStart
	                		if(dt.atZone(ZoneId.systemDefault()).toEpochSecond() >= dtStart.atZone(ZoneId.systemDefault()).toEpochSecond()&& 
	                				Duration.between(dtStart, dt).toDays() == 0) {
	                			if(!foundMatch) {
	                				foundMatch = true;
	                				dt_start = dt; //start date
	                				elevMax = elevMin = Double.parseDouble(fields[elevationIndex]);
	                				apMag = Double.parseDouble(fields[apMagIndex]);
	                				phaseAng = Double.parseDouble(fields[phaseAngIndex]);
	                				moonPhaseAng = Double.parseDouble(fields[moonPhaseAngIndex]);
	                				moonIllum = Double.parseDouble(fields[moonIllumIndex]);
	                				galacticLat = Double.parseDouble(fields[galacticLatIndex]);
	                			}
	                			double elev = Double.parseDouble(fields[elevationIndex]);
	                			if(elev > elevMax)
	                				elevMax = elev;
	                			if(elev < elevMin)
	                				elevMin = elev;
	                			//get RA/DEC at transit
                				ibp = fields[ibpIndex];
                				//System.out.println(ibp);
                				if(ibp.trim().equals("t")) {
	                				ra = fields[raIndex];
	                				ra = ra.replace(' ', ':');
	                				dec = fields[decIndex];	 
	                				dec = dec.replace(' ', ':');
	                				//System.out.println(ra+","+dec);
                				}
		                		steps++;
	                		}
	                		//find the occurrence of the brightest appearance
	                		if(brightestApMag > Double.parseDouble(fields[apMagIndex])) {
	                			brightestApMag = Double.parseDouble(fields[apMagIndex]);
	                			dateBrightestApMag = dt.format(DateTimeFormatter.ofPattern("MM/dd/uuuu"));
                				moonPhaseAngBrightestApMag = Double.parseDouble(fields[moonPhaseAngIndex]);
                				moonIllumBrightestApMag = Double.parseDouble(fields[moonIllumIndex]);
	                		}
                    	}	              		
                    }	
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
       
	        sc.close();
	        
	    } catch (Exception e) {         
            e.printStackTrace();
        }
		
		if(foundMatch) {
    		csv += 	target + ":\n\t" + dt_start.format(DateTimeFormatter.ofPattern("MM/dd/uuuu @ HH:mm")) + " for " + String.format("%.1f", ((double)steps*stepSizeMin/60.0)) + " hours." +
    				"\n\tRA/DEC (transit) = " + ra + " " + dec + "." +
    				"\n\tMagnitude = " + apMag + "." + " Min/Max Elev. = " + elevMin + "/" + elevMax + " deg." + " Phase Angle = " + phaseAng + " deg."  +
    				"\n\tMax. Magnitude = " + brightestApMag + " on " + dateBrightestApMag + " (Moon Phase Angle = " + moonPhaseAngBrightestApMag + " deg, " + " Moon Illumination = " + moonIllumBrightestApMag + "%)." +
    				"\n\tMoon Phase Angle = " + moonPhaseAng + " deg." + " Moon Illumination = " + moonIllum + "%." +
    				"\n\tGalactic Latitude = " + galacticLat + " deg.";
		}
		
		return new HorizonsByDayData(target, csv, gantt, csvHeader);
	}
	
	//main
    public static void main(String[] args) {
    	
    	String cookieFile = "_HorizonsByDay.txt";
    	
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
	    chooser.setDialogTitle("Select a folder containing your FITS images.");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);
	    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	      path = chooser.getSelectedFile().toString();
	    } else {
	      System.out.println("Error. No folder selected.");
	      return;
	    } 
	    //save this path for next time
		try {
	        //open output file
	        FileWriter fw = new FileWriter(cookieFile);
	        fw.write(path);
			fw.close();
	    } catch (Exception e) {         
	        e.printStackTrace();
	    }
		
	    if(!path.substring(path.length()-1, path.length()).equals("\\"))
	    	path += "\\";
    	
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
    		
    		System.out.println("Testing " + files.length + " target(s) in " + path + ".\n");		
    		        
	        //open output file
	        FileWriter outputFile = new FileWriter(path + dtStart.format(DateTimeFormatter.ofPattern("MMdduuuu")) + ".targets.txt");
	        
	        HorizonsByDay horizons = new HorizonsByDay();
	    	
        	//get data
	    	int count = 0;
	    	for (File inputFile : files) {  		
	    		
	    		count++;
	    		//System.out.println(inputFile.getAbsolutePath());
	    		HorizonsByDayData horizonsData = horizons.getData(inputFile);
	    		//System.out.println("Processed " + horizonsData.getTarget()+ " (" + inputFile.getName() + ").");
	    		
	    		//header
	        	String csvHeader = horizonsData.getCsvHeader();
		    	//if(csvHeader.length() == 0) {
		    	//	System.out.println("Error. No csv header found for " + horizonsData.getTarget() + "(" + inputFile.getName() + ").");
		    	//}
		    	
		    	//data
	        	String csvData = horizonsData.getCsv();
	        	if(csvHeader.length() > 0 && csvData.length() > 0) {
		    		//open data output file
	        		System.out.println(csvData);
	        		outputFile.write(csvData + "\n");
	        	}
	        	//System.out.println(csv);
	    	} 

	    	outputFile.close();
	    	System.out.println("\nProcessed " + count + " target(s).");
	    	
        } catch (Exception e) {         
            e.printStackTrace();
        }
    }	
	
}