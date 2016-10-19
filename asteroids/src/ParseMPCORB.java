

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseMPCORB{

    public static void main(String[] args) {        
        try {   	
        	FileWriter outputFile = new FileWriter("C:\\Users\\Me\\Desktop\\MPCORB.csv");
            File inputFile = new File("C:\\Users\\Me\\Desktop\\MPCORB.DAT");
            
            //outputFile.write("Des'n,H,G,Epoch,M,Peri.,Node,Incl.,e,n,a,Reference,#Obs,#Opp,Arc,rms,Perts,Computer\n");
            
            Scanner sc = new Scanner(inputFile);
            int count = 0;
            boolean dataStart = false;
            Pattern r;
            Matcher m;
            String [] field = new String[24];
            String header = "";
            String line_old = "";
            while(sc.hasNextLine()){
                String line = sc.nextLine();

                //skip header, last line indicated by all dashes
                if(!dataStart) {
                	r  = Pattern.compile("^\\-+$");
                	m = r.matcher(line);
                	if(m.find()) {
                		dataStart = true;
                	}
                	//System.out.println(line);
                	continue;
                }
                
               if(line.length() == 0) {
            	   System.out.println("Empty line found after: " + line_old);
            	   continue;
               }
                
               line_old = line;
               
               //clear fields
                for(int i=0; i<field.length; i++)
                	field[i] = "";
                
                int j=0;
//                Columns   F77    Use
//
//                1 -   7  a7     Number or provisional designation
//                                  (in packed form)
                field[j++] = line.substring(0, 7).trim();
                header = "Des'n,";
                
                //translate packed format into human readable form
                //asteroid number > 100000 and packed; replace leading character
                field[j] = field[j-1];
                r = Pattern.compile("^[A-Za-z][0-9]{4,4}$");
                m = r.matcher(field[j-1]);                  	
                if(m.find()) {
                	int asciiFirstLetter = (int) field[j-1].charAt(0);
                	//A=10, B=11...
                	if(asciiFirstLetter >= 65 && asciiFirstLetter <= 90)
                		asciiFirstLetter -= 55; //A is 10
                	else if(asciiFirstLetter >= 97 && asciiFirstLetter <= 122)
                		asciiFirstLetter -= 61; //A is 10
                	field[j] = asciiFirstLetter + field[j-1].substring(1);
                	//j++;
                } else {
                    //doc packed provisional designation
                    r = Pattern.compile("^([JK][0-9]{2,2})([A-Z])([A-Za-z0-9][0-9])([A-Z])$");
                    m = r.matcher(field[j-1]);
                    if(m.find()) {
                    	String year = m.group(1);
                    	year = year.replaceAll("^J", "19");
                    	year = year.replaceAll("^K", "20");
                    	//System.out.println(year);
                    	
                    	String halfMonth = m.group(2);
                    	String halfMonthCount = m.group(4);
                    	
                    	String cycleCount = m.group(3);
                    	int asciiFirstLetter = (int) cycleCount.charAt(0);
                    	if(asciiFirstLetter >= 48 && asciiFirstLetter <= 57) { 
                    		asciiFirstLetter -= 48; //0 is 0...                 		
                    	} else if(asciiFirstLetter >= 65 && asciiFirstLetter <= 90) {
                    		asciiFirstLetter -= 55; //A is 10... 
                    	} else if(asciiFirstLetter >= 97 && asciiFirstLetter <= 122) {
                    		asciiFirstLetter -= 61 ; //a is 36...
                    	} else {
                    		System.out.println("Invalid characters in cycle count: " + cycleCount);
                    		continue;
                    	}
                    	cycleCount = asciiFirstLetter + "" + cycleCount.charAt(1);
                    	//doc replace leading zeros
                    	cycleCount = cycleCount.replaceAll("^0+", "");
                    	
                    	field[j] = year + " " + halfMonth + halfMonthCount + cycleCount;
                    	//j++;
                    } else {
                        r = Pattern.compile("^([A-Z0-9]{2,2})S([0-9]{4,4})$");
                        m = r.matcher(field[j-1]);
                        if(m.find()) {
                        	String surveyId = m.group(1);
                        	String surveyNumber = m.group(2);
                        	
                        	field[j] = surveyNumber + " " + surveyId.charAt(0) + "-" + surveyId.charAt(1);
                          	//j++;
                        }
                    }
                }
                j++;
                header += "Desig,";                
                
//                9 -  13  f5.2   Absolute magnitude, H
                field[j++] = line.substring(8, 13).trim(); 
                header += "H,";
//               15 -  19  f5.2   Slope parameter, G
                field[j++] = line.substring(14, 19).trim();  
                header += "G,";
//
//               21 -  25  a5     Epoch (in packed form, .0 TT)
                field[j++] = line.substring(20, 25).trim();    
                header += "Epoch,";
//               27 -  35  f9.5   Mean anomaly at the epoch, in degrees
                field[j++] = line.substring(26, 35).trim(); 
                header += "M,";
//
//               38 -  46  f9.5   Argument of perihelion, J2000.0 (degrees)
                field[j++] = line.substring(37, 46).trim(); 
                header += "Peri.,";
//               49 -  57  f9.5   Longitude of the ascending node, J2000.0
//                                  (degrees)
                field[j++] = line.substring(48, 57).trim();
                header += "Node,";
//               60 -  68  f9.5   Inclination to the ecliptic, J2000.0 (degrees)
//
                field[j++] = line.substring(59, 68).trim(); 
                header += "Incl.,";
//               71 -  79  f9.7   Orbital eccentricity
                field[j++] = line.substring(70, 79).trim(); 
                header += "e,";
//               81 -  91  f11.8  Mean daily motion (degrees per day)
                field[j++] = line.substring(80, 91).trim(); 
                header += "n,";
//               93 - 103  f11.7  Semimajor axis (AU)
                field[j++] = line.substring(92, 103).trim();
                header += "a,";
//
//              106        i1     Uncertainty parameter, U
//                      or a1     If this column contains `E' it indicates
//                                that the orbital eccentricity was assumed.
//                                For one-opposition orbits this column can
//                                also contain `D' if a double (or multiple)
//                                designation is involved or `F' if an e-assumed
//                                double (or multiple) designation is involved.
                field[j++] = line.substring(105,106).trim();
                header += "U,";
//
//              108 - 116  a9     Reference
                field[j++] = line.substring(107,116).trim();
                header += "Reference,";
//              118 - 122  i5     Number of observations
                field[j++] = line.substring(117,122).trim();
                header += "#Obs,";
//              124 - 126  i3     Number of oppositions
                field[j++] = line.substring(123,126).trim();
                header += "#Opp,";
//
//                 For multiple-opposition orbits:
//                 128 - 131  i4     Year of first observation
//                 132        a1     '-'
//                 133 - 136  i4     Year of last observation
//
//                 For single-opposition orbits:
//                 128 - 131  i4     Arc length (days)
//                 133 - 136  a4     'days'
                field[j++] = line.substring(127,136).trim();
                header += "Arc,";
//                
//              138 - 141  f4.2   r.m.s residual (")
                field[j++] = line.substring(137,141).trim();
                header += "rms,";  
//              143 - 145  a3     Coarse indicator of perturbers
//              (blank if unperturbed one-opposition object)
                field[j++] = line.substring(142,145).trim();
                header += "PertsC,"; 
//              147 - 149  a3     Precise indicator of perturbers
//                                (blank if unperturbed one-opposition object)
                field[j++] = line.substring(146,149).trim();
                header += "PertsP,";                 
//              151 - 160  a10    Computer name
                field[j++] = line.substring(150,160).trim();
                header += "Computer,"; 
//
//            There may sometimes be additional information beyond column 160
//            as follows:
//
//              162 - 165  z4.4   4-hexdigit flags
//
//                                This information has been updated 2014 July 16, for files
//                                created after 18:40 UTC on that day.  Classification of
//                                distant-orbit types will resume after we ingest data from
//                                an outside collaborator.
//
//                                The bottom 6 bits (bits 0 to 5) are used to encode
//                                a value representing the orbit type (other
//                                values are undefined):
//
//                                 Value
//                                    1  Atira
//                                    2  Aten
//                                    3  Apollo
//                                    4  Amor
//                                    5  Object with q < 1.665 AU
//                                    6  Hungaria
//                                    7  Phocaea
//                                    8  Hilda
//                                    9  Jupiter Trojan
//                                   10  Distant object
//
//                                Additional information is conveyed by
//                                adding in the following bit values:
//
//                           Bit  Value
//                             6     64  Unused or internal MPC use only
//                             7    128  Unused or internal MPC use only
//                             8    256  Unused or internal MPC use only
//                             9    512  Unused or internal MPC use only
//                            10   1024  Unused or internal MPC use only
//                            11   2048  Object is NEO
//                            12   4096  Object is 1-km (or larger) NEO
//                            13   8192  1-opposition object seen at
//                                       earlier opposition
//                            14  16384  Critical list numbered object
//                            15  32768  Object is PHA
//
//                                Note that the orbit classification is
//                                  based on cuts in osculating element
//                                  space and is not 100% reliable.
//
//                                Note also that certain of the flags
//                                  are for internal MPC use and are
//                                  not documented.
//
                if(line.length() >= 165) {
                    field[j++] = line.substring(161,165).trim();
                }
                header += "Code,";
//              167 - 194  a      Readable designation
                if(line.length() >= 194) {
                    field[j++] = line.substring(166,194).trim();
                }
                header += "Description,";
//
//              195 - 202  i8     Date of last observation included in
//                                  orbit solution (YYYYMMDD format)
//                
                if(line.length() >= 202) {
                    field[j++] = line.substring(194,202).trim();
                }
                header += "LastObs";
                
                if(count == 0)
                    outputFile.write(header + "\n");                	
                
                int k;
                String csv = "";
                for(k=0; k<field.length-1; k++) {
                	//System.out.print(field[k] + ",");
                	csv += field[k] + ",";
                }	
                //System.out.println(field[k]);
                csv += field[k];
                
                outputFile.write(csv + "\n");
                
                count++;
            }
            System.out.println("Processed " + count + " lines.");
            //System.out.println("Count2="+count2);
            sc.close();
            outputFile.close();     
            
        } catch (Exception e) {         
            e.printStackTrace();
        }
    }	
	
}