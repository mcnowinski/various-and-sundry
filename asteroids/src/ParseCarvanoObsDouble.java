import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class ParseCarvanoObsDouble {	
		
    public static void main(String[] args) {        
        try { 
            //
			//"ID","AST_NUMBER","AST_NAME","AST_NAME_PROV_DESIG","AST_NUM_PROV_DESIG","TAX_CLASS","SCORE","MOID","BAD","LOG_REFLECTANCE_U","LOG_REFLECTANCE_ERROR_U","LOG_REFLECTANCE_G","LOG_REFLECTANCE_ERROR_G","LOG_REFLECTANCE_R","LOG_REFLECTANCE_ERROR_R","LOG_REFLECTANCE_I","LOG_REFLECTANCE_ERROR_I","LOG_REFLECTANCE_Z","LOG_REFLECTANCE_ERROR_Z"
			//1,166,"Rhodope","-","166","C",78,"s394c9",0,0.86,8.0e-03,1,8.0e-03,1.03,4.0e-03,1.05,8.0e-03,1.04,0.01             	
            //
            File inputFile = new File("C:\\Users\\me\\Desktop\\carvano_obs.csv");
        	
            FileWriter outputFileW = new FileWriter("C:\\Users\\me\\Desktop\\carvano_obs_compiled_weighted.csv");
            outputFileW.write("AST_NAME_PROV_DESIG,OBS_COUNT,OBS_CLASSES,TOTAL_CLASSES_ALL,CLASS_SINGLE,CLASS_SINGLE_NUM,TOTAL_CLASSES_SINGLE,TOTAL_CLASSES_SINGLE_UNIQUE,TOTAL_SCORE,AVE_SCORE_SINGLE,STDEV_SINGLE,AVE_SCORE_CLASS,STDEV_CLASS,C,X,D,L,A,S,Q,O,V,Cw,Xw,Dw,Lw,Aw,Sw,Qw,Ow,Vw\n");

            int count = 0, countUnique = 0, countObs = 0;
            //classification counts, unweighted
            int bad = 0;
            int badO = 0, badV = 0, badQ = 0, badS = 0, badA = 0, badC = 0, badX = 0, badL = 0, badD = 0;
            int countO = 0, countV = 0, countQ = 0, countS = 0, countA = 0, countC = 0, countX = 0, countL = 0, countD = 0;            
            double countOw = 0.0, countVw = 0.0, countQw = 0.0, countSw = 0.0, countAw = 0.0, countCw = 0.0, countXw = 0.0, countLw = 0.0, countDw = 0.0;          
            int countMultiple = 0;
            String line, id = "", lastId = "", taxClass = "", taxScore = "";
            Double score = 0.0;
            Double totalScore = 0.0;
            int totalCount = 0, totalClass = 0;
            String [] fields;
            Scanner sc = new Scanner(inputFile);
            sc.nextLine(); //skip header
            while(sc.hasNextLine()){   
            	count++;
            	
                line = sc.nextLine();
                fields = line.split(",");
                
                id = fields[3]; //AST_NAME_PROV_DESIG
                
                if(!id.equals(lastId)) {
                	if(lastId.length() > 0) { //not first hit
                		//print out combined values
                		System.out.println("Asteroid: " + lastId);
                		System.out.println("	Obs = " + countObs);
                		System.out.println("	Classes (all) = " + taxClass);             		
                		System.out.println("	Total Classes (all) = " + totalClass);                 		
                		String finalTaxClass = "";
                		String finalTaxClassNumber = "";
                		int finalTaxClassCountUnique = 0;
                		if(countC > 0) {
                			finalTaxClass += "C";
                			finalTaxClassNumber += Integer.toString(1);
                			finalTaxClassCountUnique++;
                		}
                		if(countX > 0) {
                			finalTaxClass += "X";
                			finalTaxClassNumber += Integer.toString(2);        			
                			finalTaxClassCountUnique++;
                		}
                		if(countD > 0) {
                			finalTaxClass += "D";
                			finalTaxClassNumber += Integer.toString(3);        			
                			finalTaxClassCountUnique++;
                		}
                		if(countL > 0) {
                			finalTaxClass += "L";
                			finalTaxClassNumber += Integer.toString(4);        			
                			finalTaxClassCountUnique++;
                		}
                		if(countA > 0) {
                			finalTaxClass += "A";
                			finalTaxClassNumber += Integer.toString(5);        			
                			finalTaxClassCountUnique++;
                		}
                		if(countS > 0) {
                			finalTaxClass += "S";
                			finalTaxClassNumber += Integer.toString(6);        			
                			finalTaxClassCountUnique++;
                		}
                		if(countQ > 0) {
                			finalTaxClass += "Q";
                			finalTaxClassNumber += Integer.toString(7);        			
                			finalTaxClassCountUnique++;
                		}
                		if(countO > 0) {
                			finalTaxClass += "O";
                			finalTaxClassNumber += Integer.toString(8);        			
                			finalTaxClassCountUnique++;
                		}
                		if(countV > 0) {
                			finalTaxClass += "V";
                			finalTaxClassNumber += Integer.toString(9);        			
                			finalTaxClassCountUnique++;
                		}
                		System.out.println("	Class (single)	= " + finalTaxClass);              		
                		System.out.println("	Total Classes (single) = " + totalCount); 
                		System.out.println("	Scores (single)	= " + taxScore); 
                		System.out.println("	Total Score (single) = " + String.format("%.2f", totalScore));
                		System.out.println("	Ave Score (single) = " + String.format("%.2f", totalCount > 0 ? totalScore/totalCount : 0.0));
                		double stddev_single = 0.0;
                		double mean = totalCount > 0 ? totalScore/totalCount : 0.0;
                    	String [] scores = taxScore.split(";");
                    	for(int i=0; i<scores.length; i++) {
                    		if(scores[i].length() > 0) {
                        		score = Double.parseDouble(scores[i]);
                        		stddev_single += totalCount > 0 ? Math.pow((mean-score),2)/totalCount : 0.0;
                    		}
                    	}  
                    	stddev_single = Math.sqrt(stddev_single); 
                		System.out.println("	Std Dev Score (single) = " + String.format("%.3f", stddev_single));                    	
                		countCw = countC > 0 ? countCw/countC : 0.0;
                		countXw = countX > 0 ? countXw/countX : 0.0;
                		countDw = countD > 0 ? countDw/countD : 0.0;                		
                		countLw = countL > 0 ? countLw/countL : 0.0;
                		countAw = countA > 0 ? countAw/countA : 0.0;
                		countSw = countS > 0 ? countSw/countS : 0.0;
                		countQw = countQ > 0 ? countQw/countQ : 0.0;
                		countOw = countO > 0 ? countOw/countO : 0.0;
                		countVw = countV > 0 ? countVw/countV : 0.0;              		
                		System.out.println("	C/X/D/L/A/S/Q/O/V = " +
                				countC + "/" + countX + "/" + countD + "/" +
                				countL + "/" + countA + "/" + countS + "/" +
                				countQ + "/" + countO + "/" + countV);
                		System.out.println("	Wgt C/X/D/L/A/S/Q/O/V = " +
                				String.format("%.2f", countCw) + "/" + String.format("%.2f", countXw) + "/" + String.format("%.2f", countDw)  + "/" +
                				String.format("%.2f", countLw)  + "/" + String.format("%.2f", countAw)  + "/" + String.format("%.2f", countSw)  + "/" +
                				String.format("%.2f", countQw)  + "/" + String.format("%.2f", countOw) + "/" + String.format("%.2f", countVw));
                		System.out.println("	Bad C/X/D/L/A/S/Q/O/V = " +
                				badC + "/" + badX + "/" + badD + "/" +
                				badL + "/" + badA + "/" + badS + "/" +
                				badQ + "/" + badO + "/" + badV);
                		double mean_class = (countCw + countXw + countDw + countLw + countAw + countSw + countQw + countOw + countVw)/finalTaxClassCountUnique; 
                		double stddev_class = (countC > 0 ? Math.pow((countCw-mean_class), 2)/finalTaxClassCountUnique : 0.0) +
                				 (countX > 0 ? Math.pow((countXw-mean_class), 2)/finalTaxClassCountUnique : 0.0) +
                				 (countD > 0 ? Math.pow((countDw-mean_class), 2)/finalTaxClassCountUnique : 0.0) +
                        		 (countL > 0 ? Math.pow((countLw-mean_class), 2)/finalTaxClassCountUnique : 0.0) + 
                        	     (countA > 0 ? Math.pow((countAw-mean_class), 2)/finalTaxClassCountUnique : 0.0) +
                                 (countS > 0 ? Math.pow((countSw-mean_class), 2)/finalTaxClassCountUnique : 0.0) +  
								 (countQ > 0 ? Math.pow((countQw-mean_class), 2)/finalTaxClassCountUnique : 0.0) + 
								 (countO > 0 ? Math.pow((countOw-mean_class), 2)/finalTaxClassCountUnique : 0.0) +
								 (countV > 0 ? Math.pow((countVw-mean_class), 2)/finalTaxClassCountUnique : 0.0);
                		stddev_class = Math.sqrt(stddev_class);
						System.out.println("	Ave Score (classes) = " + String.format("%.3f", mean_class));								 
			            System.out.println("	Std Dev Score (classes) = " + String.format("%.3f", stddev_class));
			            //outputFileW.write("AST_NAME_PROV_DESIG,OBS_COUNT,OBS_CLASSES,TOTAL_CLASSES_ALL,CLASS_SINGLE,CLASS_SINGLE_NUM,TOTAL_CLASSES_SINGLE,TOTAL_CLASSES_SINGLE_UNIQUE,TOTAL_SCORE,AVE_SCORE_SINGLE,STDEV_SINGLE,AVE_SCORE_CLASS,STDEV_CLASS,C,X,D,L,A,S,Q,O,V,Cw,Xw,Dw,Lw,Aw,Sw,Qw,Ow,Vw\n");
		        		outputFileW.write(lastId + "," + countObs + "," + taxClass + "," + totalClass + "," + finalTaxClass + "," + finalTaxClassNumber + "," + totalCount + "," + finalTaxClassCountUnique  + "," +
		        				totalScore + "," + String.format("%.5f", totalCount > 0 ? totalScore/totalCount : 0.0) + "," + (Double.isNaN(stddev_single) ? "" : stddev_single) + "," + String.format("%.5f", (Double.isNaN(mean_class) ? 0.0 : mean_class)) + "," + (Double.isNaN(stddev_class) ? "" : stddev_class) + "," +
		        				countC + "," + countX + "," + countD + "," + countL + "," + countA + "," + countS + "," + countQ + "," + countO + "," + countV + "," +
		        				String.format("%.5f", countCw) + "," + String.format("%.5f", countXw) + "," + String.format("%.5f", countDw)  + "," +
		        				String.format("%.5f", countLw)  + "," + String.format("%.5f", countAw)  + "," + String.format("%.5f", countSw)  + "," +
		        				String.format("%.5f", countQw)  + "," + String.format("%.5f", countOw) + "," + String.format("%.5f", countVw) + "\n");
                		if(countObs > 1) {
	                		countMultiple++;
                		}	
                	}
                	lastId = id;
                	//taxClass = fields[5];
                	score = Double.parseDouble(fields[6])/100.0;
                	bad = Integer.parseInt(fields[8]);
                	countUnique++;
                	countObs = 1;
                	countO = countV = countQ = countS = countA = countC = countX = countL = countD = 0; 
                	badO = badV = badQ = badS = badA = badC = badX = badL = badD = 0;                 	
                	countOw = countVw = countQw = countSw = countAw = countCw = countXw = countLw = countDw = 0.0;
                	totalScore = 0.0;
                	totalCount = totalClass = 0;
                	taxScore = "";
                	taxClass = "";
                	if(fields[5].length() > 2 || fields[5].length() == 0) {
                		System.out.println("Error. Invalid classification (" + fields[5] + ").");
                	} else {
                		totalClass++;
                    	taxClass += (taxClass.length() > 0 ? ";" : "")  + fields[5];
                		for(int i=0; i<fields[5].length(); i++) {
	                    	totalScore += score;
	                    	totalCount++;
	                    	taxScore += (taxScore.length() > 0 ? ";" : "")  + String.format("%.2f", Double.parseDouble(fields[6])/100.0);
                			String charTaxClass = fields[5].substring(i,i+1);
	                		if(charTaxClass.equals("O")) {
	                			countO++;
	                			countOw += score;
	                			badO += bad;
	                		} else if(charTaxClass.equals("V")) {
	                			countV++;  
	                			countVw += score;
	                			badV += bad;
	                		} else if(charTaxClass.equals("Q")) {
	                			countQ++;
	                			countQw += score;
	                			badQ += bad;
	                		} else if(charTaxClass.equals("S")) {
	                			countS++;
	                			countSw += score;
	                			badS += bad;
		            		} else if(charTaxClass.equals("A")) {
		            			countA++;
		            			countAw += score;
	                			badA += bad;	            			
		            		} else if(charTaxClass.equals("C")) {
		            			countC++;
		            			countCw += score;
	                			badC += bad;	            			
		            		} else if(charTaxClass.equals("X")) {
		            			countX++;
		            			countXw += score;
	                			badX += bad;	            			
		            		} else if(charTaxClass.equals("L")) {
		            			countL++;
		            			countLw += score;
	                			badL += bad;
		            		} else if(charTaxClass.equals("D")) {
		            			countD++;
		            			countDw += score;
	                			badD += bad;	            			
		            		} else {
		            			System.out.println("Error. Unknown classification (" + charTaxClass + ").");
		            			break;
		            		}
                		}
                	} 
                } else {
                	countObs++;
                	String [] classes = taxClass.split(";");
                	boolean foundMatch = false;
                	for(int i=0; i<classes.length; i++) {
                		if(classes[i].equals(fields[5])) {
                			foundMatch = true;
                			break;
                		}
                	}
                	if(!foundMatch)
                		totalClass++;
                	//taxClass += ";" + fields[5];
                	score = Double.parseDouble(fields[6])/100.0;
                	if(fields[5].length() > 2 || fields[5].length() == 0) {
                		System.out.println("Error. Invalid classification (" + fields[5] + ").");
                		return;
                	} else {
                		//totalClass++;
                    	taxClass += (taxClass.length() > 0 ? ";" : "")  + fields[5];
                		for(int i=0; i<fields[5].length(); i++) {
	                    	totalScore += score;
	                    	totalCount++;
	                    	taxScore += (taxScore.length() > 0 ? ";" : "")  + String.format("%.2f", Double.parseDouble(fields[6])/100.0);
                			String charTaxClass = fields[5].substring(i,i+1);
                			//System.out.println("charTaxClass="+charTaxClass);
	                		if(charTaxClass.equals("O")) {
	                			countO++;
	                			countOw += score;
	                			badO += bad;
	                		} else if(charTaxClass.equals("V")) {
	                			countV++;  
	                			countVw += score;
	                			badV += bad;
	                		} else if(charTaxClass.equals("Q")) {
	                			countQ++;
	                			countQw += score;
	                			badQ += bad;
	                		} else if(charTaxClass.equals("S")) {
	                			countS++;
	                			countSw += score;
	                			badS += bad;
		            		} else if(charTaxClass.equals("A")) {
		            			countA++;
		            			countAw += score;
	                			badA += bad;	            			
		            		} else if(charTaxClass.equals("C")) {
		            			countC++;
		            			countCw += score;
	                			badC += bad;	            			
		            		} else if(charTaxClass.equals("X")) {
		            			countX++;
		            			countXw += score;
	                			badX += bad;	            			
		            		} else if(charTaxClass.equals("L")) {
		            			countL++;
		            			countLw += score;
	                			badL += bad;
		            		} else if(charTaxClass.equals("D")) {
		            			countD++;
		            			countDw += score;
	                			badD += bad;	            			
		            		} else {
		            			System.out.println("Error. Unknown classification (" + charTaxClass + ").");
		            			break;
		            		}
                		}
                	}                 				
                }
            }
        	if(lastId.length() > 0) { //not first hit
        		//print out combined values
        		System.out.println("Asteroid: " + lastId);
        		System.out.println("	Obs = " + countObs);
        		System.out.println("	Classes (all) = " + taxClass);             		
        		System.out.println("	Total Classes (all) = " + totalClass);         		
        		String finalTaxClass = "";
        		String finalTaxClassNumber = "";
        		int finalTaxClassCountUnique = 0;
        		if(countC > 0) {
        			finalTaxClass += "C";
        			finalTaxClassNumber += Integer.toString(1);
        			finalTaxClassCountUnique++;
        		}
        		if(countX > 0) {
        			finalTaxClass += "X";
        			finalTaxClassNumber += Integer.toString(2);        			
        			finalTaxClassCountUnique++;
        		}
        		if(countD > 0) {
        			finalTaxClass += "D";
        			finalTaxClassNumber += Integer.toString(3);        			
        			finalTaxClassCountUnique++;
        		}
        		if(countL > 0) {
        			finalTaxClass += "L";
        			finalTaxClassNumber += Integer.toString(4);        			
        			finalTaxClassCountUnique++;
        		}
        		if(countA > 0) {
        			finalTaxClass += "A";
        			finalTaxClassNumber += Integer.toString(5);        			
        			finalTaxClassCountUnique++;
        		}
        		if(countS > 0) {
        			finalTaxClass += "S";
        			finalTaxClassNumber += Integer.toString(6);        			
        			finalTaxClassCountUnique++;
        		}
        		if(countQ > 0) {
        			finalTaxClass += "Q";
        			finalTaxClassNumber += Integer.toString(7);        			
        			finalTaxClassCountUnique++;
        		}
        		if(countO > 0) {
        			finalTaxClass += "O";
        			finalTaxClassNumber += Integer.toString(8);        			
        			finalTaxClassCountUnique++;
        		}
        		if(countV > 0) {
        			finalTaxClass += "V";
        			finalTaxClassNumber += Integer.toString(9);        			
        			finalTaxClassCountUnique++;
        		}
        		System.out.println("	Class (single)	= " + finalTaxClass);              		
        		System.out.println("	Total Classes (single) = " + totalCount); 
        		System.out.println("	Scores (single)	= " + taxScore); 
        		System.out.println("	Total Score (single) = " + String.format("%.2f", totalScore));
        		System.out.println("	Ave Score (single) = " + String.format("%.2f", totalCount > 0 ? totalScore/totalCount : 0.0));
        		double stddev_single = 0.0;
        		double mean = totalCount > 0 ? totalScore/totalCount : 0.0;
            	String [] scores = taxScore.split(";");
            	for(int i=0; i<scores.length; i++) {
            		if(scores[i].length() > 0) {
                		score = Double.parseDouble(scores[i]);
                		stddev_single += totalCount > 0 ? Math.pow((mean-score),2)/totalCount : 0.0;
            		}
            	}  
            	stddev_single = Math.sqrt(stddev_single); 
        		System.out.println("	Std Dev Score (single) = " + String.format("%.3f", stddev_single));                    	
        		countCw = countC > 0 ? countCw/countC : 0.0;
        		countXw = countX > 0 ? countXw/countX : 0.0;
        		countDw = countD > 0 ? countDw/countD : 0.0;                		
        		countLw = countL > 0 ? countLw/countL : 0.0;
        		countAw = countA > 0 ? countAw/countA : 0.0;
        		countSw = countS > 0 ? countSw/countS : 0.0;
        		countQw = countQ > 0 ? countQw/countQ : 0.0;
        		countOw = countO > 0 ? countOw/countO : 0.0;
        		countVw = countV > 0 ? countVw/countV : 0.0;              		
        		System.out.println("	C/X/D/L/A/S/Q/O/V = " +
        				countC + "/" + countX + "/" + countD + "/" +
        				countL + "/" + countA + "/" + countS + "/" +
        				countQ + "/" + countO + "/" + countV);
        		System.out.println("	Wgt C/X/D/L/A/S/Q/O/V = " +
        				String.format("%.2f", countCw) + "/" + String.format("%.2f", countXw) + "/" + String.format("%.2f", countDw)  + "/" +
        				String.format("%.2f", countLw)  + "/" + String.format("%.2f", countAw)  + "/" + String.format("%.2f", countSw)  + "/" +
        				String.format("%.2f", countQw)  + "/" + String.format("%.2f", countOw) + "/" + String.format("%.2f", countVw));
        		System.out.println("	Bad C/X/D/L/A/S/Q/O/V = " +
        				badC + "/" + badX + "/" + badD + "/" +
        				badL + "/" + badA + "/" + badS + "/" +
        				badQ + "/" + badO + "/" + badV);
        		double mean_class = (countCw + countXw + countDw + countLw + countAw + countSw + countQw + countOw + countVw)/finalTaxClassCountUnique; 
        		double stddev_class = (countC > 0 ? Math.pow((countCw-mean_class), 2)/finalTaxClassCountUnique : 0.0) +
        				 (countX > 0 ? Math.pow((countXw-mean_class), 2)/finalTaxClassCountUnique : 0.0) +
        				 (countD > 0 ? Math.pow((countDw-mean_class), 2)/finalTaxClassCountUnique : 0.0) +
                		 (countL > 0 ? Math.pow((countLw-mean_class), 2)/finalTaxClassCountUnique : 0.0) + 
                	     (countA > 0 ? Math.pow((countAw-mean_class), 2)/finalTaxClassCountUnique : 0.0) +
                         (countS > 0 ? Math.pow((countSw-mean_class), 2)/finalTaxClassCountUnique : 0.0) +  
						 (countQ > 0 ? Math.pow((countQw-mean_class), 2)/finalTaxClassCountUnique : 0.0) + 
						 (countO > 0 ? Math.pow((countOw-mean_class), 2)/finalTaxClassCountUnique : 0.0) +
						 (countV > 0 ? Math.pow((countVw-mean_class), 2)/finalTaxClassCountUnique : 0.0);
        		stddev_class = Math.sqrt(stddev_class);
				System.out.println("	Ave Score (classes) = " + String.format("%.3f", mean_class));								 
	            System.out.println("	Std Dev Score (classes) = " + String.format("%.3f", stddev_class));
	            //outputFileW.write("AST_NAME_PROV_DESIG,OBS_COUNT,OBS_CLASSES,TOTAL_CLASSES_ALL,CLASS_SINGLE,CLASS_SINGLE_NUM,TOTAL_CLASSES_SINGLE,TOTAL_CLASSES_SINGLE_UNIQUE,TOTAL_SCORE,AVE_SCORE_SINGLE,STDEV_SINGLE,AVE_SCORE_CLASS,STDEV_CLASS,C,X,D,L,A,S,Q,O,V,Cw,Xw,Dw,Lw,Aw,Sw,Qw,Ow,Vw\n");
        		outputFileW.write(lastId + "," + countObs + "," + taxClass + "," + totalClass + "," + finalTaxClass + "," + finalTaxClassNumber + "," + totalCount + "," + finalTaxClassCountUnique  + "," +
        				totalScore + "," + String.format("%.5f", totalCount > 0 ? totalScore/totalCount : 0.0) + "," + (Double.isNaN(stddev_single) ? "" : stddev_single) + "," + String.format("%.5f", (Double.isNaN(mean_class) ? 0.0 : mean_class)) + "," + (Double.isNaN(stddev_class) ? "" : stddev_class) + "," +
        				countC + "," + countX + "," + countD + "," + countL + "," + countA + "," + countS + "," + countQ + "," + countO + "," + countV + "," +
        				String.format("%.5f", countCw) + "," + String.format("%.5f", countXw) + "," + String.format("%.5f", countDw)  + "," +
        				String.format("%.5f", countLw)  + "," + String.format("%.5f", countAw)  + "," + String.format("%.5f", countSw)  + "," +
        				String.format("%.5f", countQw)  + "," + String.format("%.5f", countOw) + "," + String.format("%.5f", countVw) + "\n");
        		if(countObs > 1) {
            		countMultiple++;
        		}	
        	}
            System.out.println("Processed " + count + " asteroid observations."); 
            System.out.println("Processed " + countMultiple + " asteroid observations with multiple observations.");             
            System.out.println("Found " + countUnique + " unique asteroids.");
            
            sc.close();
            outputFileW.close();
            
        } catch (Exception e) {         
            e.printStackTrace();
        }
    }	
	
}