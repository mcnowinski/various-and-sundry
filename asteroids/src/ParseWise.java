

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseWise{

    public static void main(String[] args) {        
        try {   	
        	FileWriter outputFile = new FileWriter("C:\\Users\\Me\\Desktop\\wise_out.txt");
            File inputFile = new File("C:\\Users\\Me\\Desktop\\wise.txt");
            
            Scanner sc = new Scanner(inputFile);
            int count = 0;
            int count2 = 0;
            while(sc.hasNextLine()){
                String line = sc.nextLine();

                Pattern r = Pattern.compile("^[0-9A-Z][0-9]+$");
                Matcher m = r.matcher(line);
                if(m.find()) {
                	//asteroid number < 100000
                    r = Pattern.compile("^[0-9]+$");
                    m = r.matcher(line);                  	
                    if(m.find()) {
                    	//doc strip leading zeros
                    	//System.out.println(Integer.parseInt(line));                     	
                    	outputFile.write(Integer.parseInt(line)+"\n");                 	
                      	count++;
                    }
                    //asteroid number > 100000 and packed; replace leading character
                    r = Pattern.compile("^[A-Z][0-9]{4,4}$");
                    m = r.matcher(line);                  	
                    if(m.find()) {
                    	int asciiFirstLetter = (int) line.charAt(0);
                    	//A=10, B=11...
                    	if(asciiFirstLetter >= 65 && asciiFirstLetter <= 90)
                    		asciiFirstLetter -= 55; //A is 10
                    	outputFile.write(asciiFirstLetter + line.substring(1) + "\n");
                      	count++;
                    }                    
                }
                
                //doc packed provisional designation
                r = Pattern.compile("^([JK][0-9]{2,2})([A-Z])([A-Za-z0-9][0-9])([A-Z])$");
                m = r.matcher(line);
                if(m.find()) {
                	//System.out.println(m.group(1));
                	count2++;
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
                		return;
                	}
                	cycleCount = asciiFirstLetter + "" + cycleCount.charAt(1);
                	//doc replace leading zeros
                	cycleCount = cycleCount.replaceAll("^0+", "");
                	
                	//System.out.println(year+" "+halfMonth+halfMonthCount+cycleCount); 
                	outputFile.write(year+" "+halfMonth+halfMonthCount+cycleCount+"\n");
                  	count++;
                }                

                //doc packed provisional designation
                r = Pattern.compile("^([A-Z0-9]{2,2})S([0-9]{4,4})$");
                m = r.matcher(line);
                if(m.find()) {
                	String surveyId = m.group(1);
                	String surveyNumber = m.group(2);
                	
                	outputFile.write(surveyNumber + " " + surveyId.charAt(0) + "-" + surveyId.charAt(1) + "\n");
                  	count++;
                }            
            }
            System.out.println("Count="+count);
            //System.out.println("Count2="+count2);
            sc.close();
            outputFile.close();     
            
        } catch (Exception e) {         
            e.printStackTrace();
        }
    }	
	
}
