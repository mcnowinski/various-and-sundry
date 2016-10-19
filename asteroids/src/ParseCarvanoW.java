import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class ParseCarvanoW {	
		
    public static void main(String[] args) {        
        try { 
            //
			//"ID","AST_NUMBER","AST_NAME","AST_NAME_PROV_DESIG","AST_NUM_PROV_DESIG","TAX_CLASS","SCORE","MOID","BAD","LOG_REFLECTANCE_U","LOG_REFLECTANCE_ERROR_U","LOG_REFLECTANCE_G","LOG_REFLECTANCE_ERROR_G","LOG_REFLECTANCE_R","LOG_REFLECTANCE_ERROR_R","LOG_REFLECTANCE_I","LOG_REFLECTANCE_ERROR_I","LOG_REFLECTANCE_Z","LOG_REFLECTANCE_ERROR_Z"
			//1,166,"Rhodope","-","166","C",78,"s394c9",0,0.86,8.0e-03,1,8.0e-03,1.03,4.0e-03,1.05,8.0e-03,1.04,0.01             	
            //
            File inputFile = new File("C:\\Users\\me\\Desktop\\carvano_obs.csv");
        	
            FileWriter outputFileW = new FileWriter("C:\\Users\\me\\Desktop\\carvano_obs_compiled_weighted.csv");
            outputFileW.write("AST_NAME_PROV_DESIG,OBS_COUNT,OBS_CLASSES,TOTAL_CLASSES,CLASS,CLASS_COUNT,TOTAL_COUNT,TOTAL_SCORE,AVE_SCORE,STDEV,KURTOSIS,C,X,D,L,A,S,Q,O,V,Cw,Xw,Dw,Lw,Aw,Sw,Qw,Ow,Vw\n");

            int count = 0, countUnique = 0, countObs = 0;
            //classification counts, unweighted
            boolean bad = false;
            int badO = 0, badV = 0, badQ = 0, badS = 0, badA = 0, badC = 0, badX = 0, badL = 0, badD = 0;
            int countO = 0, countV = 0, countQ = 0, countS = 0, countA = 0, countC = 0, countX = 0, countL = 0, countD = 0;            
            double countOw = 0.0, countVw = 0.0, countQw = 0.0, countSw = 0.0, countAw = 0.0, countCw = 0.0, countXw = 0.0, countLw = 0.0, countDw = 0.0;
            //use these to quantify the "ambiguity", use analogy of plot's "moment of intertia"
            //double dC = 0, dX = 1, dD = 2, dL = 3, dA = 3.5, dS = 4.0, dQ = 4.33, dO = 4.67, dV = 5; //"distance"
            double dC = 0, dX = 1, dD = 2, dL = 3, dA = 4, dS = 5, dQ = 6, dO = 7, dV = 8; //"distance"            
            int countMultiple = 0;
            String line, id = "", lastId = "", taxClass = "";
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
                id = id.replaceAll("\"", ""); //remove double quotes, if any
                
                if(!id.equals(lastId)) {
                	if(lastId.length() > 0) { //not first hit
                		//print out combined values
                		System.out.println("Asteroid: " + lastId);
                		System.out.println("	Obs = " + countObs);
                		System.out.println("	Classes	= " + taxClass);
                		System.out.println("	Total Classes = " + totalClass);                 		
                		String finalTaxClass = "";
                		int totalCountUnique = 0;
                		if(countC > 0) {
                			finalTaxClass += "C";
                			totalCountUnique++;
                		}
                		if(countX > 0) {
                			finalTaxClass += "X";
                			totalCountUnique++;
                		}
                		if(countD > 0) {
                			finalTaxClass += "D";
                			totalCountUnique++;
                		}
                		if(countL > 0) {
                			finalTaxClass += "L";
                			totalCountUnique++;
                		}
                		if(countA > 0) {
                			finalTaxClass += "A";
                			totalCountUnique++;
                		}
                		if(countS > 0) {
                			finalTaxClass += "S";
                			totalCountUnique++;
                		}
                		if(countQ > 0) {
                			finalTaxClass += "Q";
                			totalCountUnique++;
                		}
                		if(countO > 0) {
                			finalTaxClass += "O";
                			totalCountUnique++;
                		}
                		if(countV > 0) {
                			finalTaxClass += "V";
                			totalCountUnique++;
                		}
                		System.out.println("	Class	= " + finalTaxClass);              		
                		System.out.println("	Total Count = " + totalCount); 
                		System.out.println("	Total Score = " + String.format("%.2f", totalScore));
                		System.out.println("	Ave Score = " + String.format("%.2f", totalCount > 0 ? totalScore/totalCount : 0.0));

//                		System.out.println("	Cw/Xw/Dw/Lw/Aw/Sw/Qw/Ow/Vw = " +
//                				String.format("%.2f", countC > 0 ? countCw/countC : 0.0) + "/" + String.format("%.2f", countX > 0 ? countXw/countX : 0.0) + "/" + String.format("%.2f", countD > 0 ? countDw/countD : 0.0)  + "/" +
//                				String.format("%.2f", countL > 0 ? countLw/countL : 0.0)  + "/" + String.format("%.2f", countA > 0 ? countAw/countA : 0.0)  + "/" + String.format("%.2f", countS > 0 ? countSw/countS : 0.0)  + "/" +
//                				String.format("%.2f", countQ > 0 ? countQw/countQ : 0.0)  + "/" + String.format("%.2f", countO > 0 ? countOw/countO : 0.0) + "/" + String.format("%.2f", countV > 0 ? countVw/countV : 0.0));
//                		System.out.println("	Cw/Xw/Dw/Lw/Aw/Sw/Qw/Ow/Vw = " +
//                				String.format("%.2f", countCw) + "/" + String.format("%.2f", countXw) + "/" + String.format("%.2f", countDw)  + "/" +
//                				String.format("%.2f", countLw)  + "/" + String.format("%.2f", countAw)  + "/" + String.format("%.2f", countSw)  + "/" +
//                				String.format("%.2f", countQw)  + "/" + String.format("%.2f", countOw) + "/" + String.format("%.2f", countVw));
//                		System.out.println("	Cw/Xw/Dw/Lw/Aw/Sw/Qw/Ow/Vw = " +
//                				Math.round(countCw/totalScore*totalCount) + "/" + Math.round(countXw/totalScore*totalCount) + "/" + Math.round(countDw/totalScore*totalCount)  + "/" +
//                				Math.round(countLw/totalScore*totalCount)  + "/" + Math.round(countAw/totalScore*totalCount)  + "/" + Math.round(countSw/totalScore*totalCount)  + "/" +
//                				Math.round(countQw/totalScore*totalCount)  + "/" + Math.round(countOw/totalScore*totalCount) + "/" + Math.round(countVw/totalScore*totalCount));
                		//calculate mean, std dev, m4, and kurtosis = indication of heavy tails = ambiguity
                		//http://brownmath.com/stat/shape.htm
                		double mean = ((double) (countOw*dO + countVw*dV + countQw*dQ + countSw*dS + countAw*dA + countCw*dC + countXw*dX + countLw*dL + countDw*dD)) / ((double) (countOw + countVw + countQw + countSw + countAw + countCw + countXw + countLw + countDw));
                		double stdev = (countOw*Math.pow((mean-dO),2) + countVw*Math.pow((mean-dV),2) + countQw*Math.pow((mean-dQ),2) + countSw*Math.pow((mean-dS),2) + countAw*Math.pow((mean-dA),2) + countCw*Math.pow((mean-dC),2) + countXw*Math.pow((mean-dX),2) + countLw*Math.pow((mean-dL),2) + countDw*Math.pow((mean-dD),2))/((double) (countOw + countVw + countQw + countSw + countAw + countCw + countXw + countLw + countDw));
                		double m4 = (countOw*Math.pow((mean-dO),4) + countVw*Math.pow((mean-dV),4) + countQw*Math.pow((mean-dQ),4) + countSw*Math.pow((mean-dS),4) + countAw*Math.pow((mean-dA),4) + countCw*Math.pow((mean-dC),4) + countXw*Math.pow((mean-dX),4) + countLw*Math.pow((mean-dL),4) + countDw*Math.pow((mean-dD),4))/((double) (countOw + countVw + countQw + countSw + countAw + countCw + countXw + countLw + countDw));
                		double kurtosis = m4/Math.pow(stdev, 2);
                		System.out.println("	Std Dev (weighted) = " + stdev);                		
                		System.out.println("	Kurtosis (weighted) = " + kurtosis);
                		System.out.println("	C/X/D/L/A/S/Q/O/V = " +
                				countC + "/" + countX + "/" + countD + "/" +
                				countL + "/" + countA + "/" + countS + "/" +
                				countQ + "/" + countO + "/" + countV);
                		System.out.println("	Wgt C/X/D/L/A/S/Q/O/V = " +
                				String.format("%.2f", totalScore > 0 ? countCw/totalScore*totalCount : 0.0) + "/" + String.format("%.2f", totalScore > 0 ? countXw/totalScore*totalCount : 0.0) + "/" + String.format("%.2f", totalScore > 0 ? countDw/totalScore*totalCount : 0.0)  + "/" +
                				String.format("%.2f", totalScore > 0 ? countLw/totalScore*totalCount : 0.0)  + "/" + String.format("%.2f", totalScore > 0 ? countAw/totalScore*totalCount : 0.0)  + "/" + String.format("%.2f", totalScore > 0 ? countSw/totalScore*totalCount : 0.0)  + "/" +
                				String.format("%.2f", totalScore > 0 ? countQw/totalScore*totalCount : 0.0)  + "/" + String.format("%.2f", totalScore > 0 ? countOw/totalScore*totalCount : 0.0) + "/" + String.format("%.2f", totalScore > 0 ? countVw/totalScore*totalCount : 0.0));
                		System.out.println("	Bad C/X/D/L/A/S/Q/O/V = " +
                				badC + "/" + badX + "/" + badD + "/" +
                				badL + "/" + badA + "/" + badS + "/" +
                				badQ + "/" + badO + "/" + badV);
                		outputFileW.write(lastId + "," + countObs + "," + taxClass + "," + totalClass + "," + finalTaxClass + "," + totalCountUnique + "," +
                				totalCount + "," + totalScore + "," + String.format("%.2f", totalCount > 0 ? totalScore/totalCount : 0.0) + "," + (Double.isNaN(stdev) ? "" : stdev) + "," + (Double.isNaN(kurtosis) ? "" : kurtosis) + "," +
                				countC + "," + countX + "," + countD + "," + countL + "," + countA + "," + countS + "," + countQ + "," + countO + "," + countV + "," +
                				String.format("%.2f", totalScore > 0 ? countCw/totalScore*totalCount : 0.0) + "," + String.format("%.2f", totalScore > 0 ? countXw/totalScore*totalCount : 0.0) + "," + String.format("%.2f", totalScore > 0 ? countDw/totalScore*totalCount : 0.0)  + "," +
                				String.format("%.2f", totalScore > 0 ? countLw/totalScore*totalCount : 0.0)  + "," + String.format("%.2f", totalScore > 0 ? countAw/totalScore*totalCount : 0.0)  + "," + String.format("%.2f", totalScore > 0 ? countSw/totalScore*totalCount : 0.0)  + "," +
                				String.format("%.2f", totalScore > 0 ? countQw/totalScore*totalCount : 0.0)  + "," + String.format("%.2f", totalScore > 0 ? countOw/totalScore*totalCount : 0.0) + "," + String.format("%.2f", totalScore > 0 ? countVw/totalScore*totalCount : 0.0) + "\n");
                		if(countObs > 1) {
	                		countMultiple++;
                		}	
                	}
                	lastId = id;
                	taxClass = fields[5];
                	score = Double.parseDouble(fields[6])/100.0;
                	bad = Integer.parseInt(fields[8]) == 1 ? true : false;
                	countUnique++;
                	countObs = 1;
                	countO = countV = countQ = countS = countA = countC = countX = countL = countD = 0; 
                	badO = badV = badQ = badS = badA = badC = badX = badL = badD = 0;                 	
                	countOw = countVw = countQw = countSw = countAw = countCw = countXw = countLw = countDw = 0.0;
                	if(taxClass.length() > 2 || taxClass.length() == 0) {
                		System.out.println("Error. Invalid classification (" + fields[5] + ").");
                	} else if(taxClass.length() == 1) {
                    	totalScore = score;
                    	totalCount = totalClass = 1;
                		if(taxClass.equals("O")) {
                			countO++;
                			countOw += score;
                			badO += bad ? 1 : 0;
                		} else if(taxClass.equals("V")) {
                			countV++;  
                			countVw += score;
                			badV += bad ? 1 : 0;
                		} else if(taxClass.equals("Q")) {
                			countQ++;
                			countQw += score;
                			badQ += bad ? 1 : 0;
                		} else if(taxClass.equals("S")) {
                			countS++;
                			countSw += score;
                			badS += bad ? 1 : 0;
	            		} else if(taxClass.equals("A")) {
	            			countA++;
	            			countAw += score;
                			badA += bad ? 1 : 0;	            			
	            		} else if(taxClass.equals("C")) {
	            			countC++;
	            			countCw += score;
                			badC += bad ? 1 : 0;	            			
	            		} else if(taxClass.equals("X")) {
	            			countX++;
	            			countXw += score;
                			badX += bad ? 1 : 0;	            			
	            		} else if(taxClass.equals("L")) {
	            			countL++;
	            			countLw += score;
                			badL += bad ? 1 : 0;
	            		} else if(taxClass.equals("D")) {
	            			countD++;
	            			countDw += score;
                			badD += bad ? 1 : 0;	            			
	            		} else {
	            			System.out.println("Error. Unknown classification (" + taxClass + ").");
	            			break;
	            		}	
                	} else { //length == 2
                    	totalScore = 0.0;
                    	totalCount = 0;
                    	totalClass = 1;
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
                	taxClass += ";" + fields[5];
                	score = Double.parseDouble(fields[6])/100.0;
                	if(fields[5].length() > 2) {
                		System.out.println("Invalid classification (" + fields[5] + ").");
                		break;
                	} else if(fields[5].length() == 1) {
                    	totalScore += score;
                    	totalCount += 1;
                		if(fields[5].equals("O")) {
                			countO++;
                			countOw += score;
                			badO += bad ? 1 : 0;
                		} else if(fields[5].equals("V")) {
                			countV++; 
                			countVw += score;
                			badV += bad ? 1 : 0;
                		} else if(fields[5].equals("Q")) {
                			countQ++;
                			countQw += score;
                			badQ += bad ? 1 : 0;
                		} else if(fields[5].equals("S")) {
                			countS++; 
                			countSw += score;
                			badS += bad ? 1 : 0;
	            		} else if(fields[5].equals("A")) {
	            			countA++;
	            			countAw += score;
                			badA += bad ? 1 : 0;
	            		} else if(fields[5].equals("C")) {
	            			countC++;
	            			countCw += score;
                			badC += bad ? 1 : 0;
	            		} else if(fields[5].equals("X")) {
	            			countX++;
	            			countXw += score;
                			badX += bad ? 1 : 0;
	            		} else if(fields[5].equals("L")) {
	            			countL++; 
	            			countLw += score;
                			badL += bad ? 1 : 0;
	            		} else if(fields[5].equals("D")) {
	            			countD++;
	            			countDw += score;
                			badD += bad ? 1 : 0;
	            		} else {
	            			System.out.println("Error. Unknown classification (" + fields[5] + ").");
	            			break;
	            		}	
                	} else { //length == 2
                	}               	
                }
            }
        	if(lastId.length() > 0) { //not first hit
        		//print out combined values
        		System.out.println("Asteroid: " + lastId);
        		System.out.println("	Obs = " + countObs);
        		System.out.println("	Classes	= " + taxClass);
        		System.out.println("	Total Classes = " + totalClass);        		
        		String finalTaxClass = "";
        		int totalCountUnique = 0;
        		if(countC > 0) {
        			finalTaxClass += "C";
        			totalCountUnique++;
        		}
        		if(countX > 0) {
        			finalTaxClass += "X";
        			totalCountUnique++;
        		}
        		if(countD > 0) {
        			finalTaxClass += "D";
        			totalCountUnique++;
        		}
        		if(countL > 0) {
        			finalTaxClass += "L";
        			totalCountUnique++;
        		}
        		if(countA > 0) {
        			finalTaxClass += "A";
        			totalCountUnique++;
        		}
        		if(countS > 0) {
        			finalTaxClass += "S";
        			totalCountUnique++;
        		}
        		if(countQ > 0) {
        			finalTaxClass += "Q";
        			totalCountUnique++;
        		}
        		if(countO > 0) {
        			finalTaxClass += "O";
        			totalCountUnique++;
        		}
        		if(countV > 0) {
        			finalTaxClass += "V";
        			totalCountUnique++;
        		}
        		System.out.println("	Class	= " + finalTaxClass);
        		System.out.println("	Total Count = " + totalCount); 
        		System.out.println("	Total Score = " + String.format("%.2f", totalScore));
        		System.out.println("	Ave Score = " + String.format("%.2f", totalCount > 0 ? totalScore/totalCount : 0.0));
        		//calculate mean, std dev, m4, and kurtosis = indication of heavy tails = ambiguity
        		//http://brownmath.com/stat/shape.htm
        		double mean = ((double) (countOw*dO + countVw*dV + countQw*dQ + countSw*dS + countAw*dA + countCw*dC + countXw*dX + countLw*dL + countDw*dD)) / ((double) (countOw + countVw + countQw + countSw + countAw + countCw + countXw + countLw + countDw));
        		double stdev = (countOw*Math.pow((mean-dO),2) + countVw*Math.pow((mean-dV),2) + countQw*Math.pow((mean-dQ),2) + countSw*Math.pow((mean-dS),2) + countAw*Math.pow((mean-dA),2) + countCw*Math.pow((mean-dC),2) + countXw*Math.pow((mean-dX),2) + countLw*Math.pow((mean-dL),2) + countDw*Math.pow((mean-dD),2))/((double) (countOw + countVw + countQw + countSw + countAw + countCw + countXw + countLw + countDw));
        		double m4 = (countOw*Math.pow((mean-dO),4) + countVw*Math.pow((mean-dV),4) + countQw*Math.pow((mean-dQ),4) + countSw*Math.pow((mean-dS),4) + countAw*Math.pow((mean-dA),4) + countCw*Math.pow((mean-dC),4) + countXw*Math.pow((mean-dX),4) + countLw*Math.pow((mean-dL),4) + countDw*Math.pow((mean-dD),4))/((double) (countOw + countVw + countQw + countSw + countAw + countCw + countXw + countLw + countDw));
        		double kurtosis = m4/Math.pow(stdev, 2);
        		System.out.println("	Std Dev (weighted) = " + stdev);                		
        		System.out.println("	Kurtosis (weighted) = " + kurtosis);
        		System.out.println("	C/X/D/L/A/S/Q/O/V = " +
        				countC + "/" + countX + "/" + countD + "/" +
        				countL + "/" + countA + "/" + countS + "/" +
        				countQ + "/" + countO + "/" + countV);
        		System.out.println("	Wgt C/X/D/L/A/S/Q/O/V = " +
        				String.format("%.2f", totalScore > 0 ? countCw/totalScore*totalCount : 0.0) + "/" + String.format("%.2f", totalScore > 0 ? countXw/totalScore*totalCount : 0.0) + "/" + String.format("%.2f", totalScore > 0 ? countDw/totalScore*totalCount : 0.0)  + "/" +
        				String.format("%.2f", totalScore > 0 ? countLw/totalScore*totalCount : 0.0)  + "/" + String.format("%.2f", totalScore > 0 ? countAw/totalScore*totalCount : 0.0)  + "/" + String.format("%.2f", totalScore > 0 ? countSw/totalScore*totalCount : 0.0)  + "/" +
        				String.format("%.2f", totalScore > 0 ? countQw/totalScore*totalCount : 0.0)  + "/" + String.format("%.2f", totalScore > 0 ? countOw/totalScore*totalCount : 0.0) + "/" + String.format("%.2f", totalScore > 0 ? countVw/totalScore*totalCount : 0.0));
        		System.out.println("	Bad C/X/D/L/A/S/Q/O/V = " +
        				badC + "/" + badX + "/" + badD + "/" +
        				badL + "/" + badA + "/" + badS + "/" +
        				badQ + "/" + badO + "/" + badV);
        		//outputFileW.write("AST_NAME_PROV_DESIG,OBS_COUNT,OBS_CLASSES,CLASS,TOTAL_COUNT,TOTAL_SCORE,AVE_SCORE,STDEV,KURTOSIS,C,X,D,L,A,S,Q,O,V,Cw,Xw,Dw,Lw,Aw,Sw,Qw,Ow,Vw\n");
        		outputFileW.write(lastId + "," + countObs + "," + taxClass + "," + totalClass + "," + finalTaxClass + "," + totalCountUnique + "," +
        				totalCount + "," + totalScore + "," + String.format("%.2f", totalCount > 0 ? totalScore/totalCount : 0.0) + "," + (Double.isNaN(stdev) ? "" : stdev) + "," + (Double.isNaN(kurtosis) ? "" : kurtosis) + "," +
        				countC + "," + countX + "," + countD + "," + countL + "," + countA + "," + countS + "," + countQ + "," + countO + "," + countV + "," +
        				String.format("%.2f", totalScore > 0 ? countCw/totalScore*totalCount : 0.0) + "," + String.format("%.2f", totalScore > 0 ? countXw/totalScore*totalCount : 0.0) + "," + String.format("%.2f", totalScore > 0 ? countDw/totalScore*totalCount : 0.0)  + "," +
        				String.format("%.2f", totalScore > 0 ? countLw/totalScore*totalCount : 0.0)  + "," + String.format("%.2f", totalScore > 0 ? countAw/totalScore*totalCount : 0.0)  + "," + String.format("%.2f", totalScore > 0 ? countSw/totalScore*totalCount : 0.0)  + "," +
        				String.format("%.2f", totalScore > 0 ? countQw/totalScore*totalCount : 0.0)  + "," + String.format("%.2f", totalScore > 0 ? countOw/totalScore*totalCount : 0.0) + "," + String.format("%.2f", totalScore > 0 ? countVw/totalScore*totalCount : 0.0) + "\n");
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