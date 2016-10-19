
import java.io.File;
import java.io.FileWriter;
//import java.io.FileWriter;
import java.util.Scanner;

public class ParseCarvano {	
		
    public static void main(String[] args) {        
        try { 
            FileWriter outputFile = new FileWriter("C:\\Users\\nowinskim\\Desktop\\carvano_obs_compiled.csv");
            outputFile.write("id,O,V,Q,S,A,C,X,L,D,Ow,Vw,Qw,Sw,Aw,Cw,Xw,Lw,Dw\n");
        	//
			//"ID","AST_NUMBER","AST_NAME","PROV_DESIG","AST_NUM_PROV_DESIG","TAX_CLASS","SCORE","MOID","BAD","LOG_REFLECTANCE_U","LOG_REFLECTANCE_ERROR_U","LOG_REFLECTANCE_G","LOG_REFLECTANCE_ERROR_G","LOG_REFLECTANCE_R","LOG_REFLECTANCE_ERROR_R","LOG_REFLECTANCE_I","LOG_REFLECTANCE_ERROR_I","LOG_REFLECTANCE_Z","LOG_REFLECTANCE_ERROR_Z"
			//1,166,"Rhodope","-","166","C",78,"s394c9",0,0.86,8.0e-03,1,8.0e-03,1.03,4.0e-03,1.05,8.0e-03,1.04,0.01             	
            //
            File inputFile = new File("C:\\Users\\nowinskim\\Desktop\\carvano_obs.csv");
            int count = 0, countUnique = 0, countObs = 0;
            //classification counts, unweighted
            double countO = 0, countV = 0, countQ = 0, countS = 0, countA = 0, countC = 0, countX = 0, countL = 0, countD = 0;            
            double countOw = 0, countVw = 0, countQw = 0, countSw = 0, countAw = 0, countCw = 0, countXw = 0, countLw = 0, countDw = 0;
            double countObsWeighted = 0;
            int countMultiple = 0;
            String line, id = "", lastId = "", taxClass = "";
            String [] fields;
            Scanner sc = new Scanner(inputFile);
            sc.nextLine(); //skip header
            while(sc.hasNextLine()){   
            	count++;
            	
                line = sc.nextLine();
                fields = line.split(",");
                
                id = fields[4]; //AST_NUM_PROV_DESIG
                id = id.replaceAll("\"", ""); //remove double quotes, if any
                
                //if(id.compareTo(lastId) < 0) {
                //    System.out.println("Error. List not sorted (" + id + " < " + lastId + ")");
                //    break;
                //}
                
                if(!id.equals(lastId)) {
                	if(lastId.length() > 0) { //not first hit
                		//print out combined values
                		System.out.println("Asteroid: " + lastId);
                		System.out.println("	Obs = " + countObs + ", Obs (weighted) = " + countObsWeighted);
                		System.out.println("	Classes	= " + taxClass);
                		//System.out.println("	O/V/Q/S/A/C/X/L/D = " + countO + "/" + countV + "/" + countQ + "/" + countS + "/" + countA + "/" + countC + "/" + countX + "/" + countL + "/" + countD);
                		System.out.println("	%  O/V/Q/S/A/C/X/L/D = " + Math.round(countO/countObs*100.0) + "/" + Math.round(countV/countObs*100.0) + "/" + Math.round(countQ/countObs*100.0) + "/" + Math.round(countS/countObs*100.0) + "/" + Math.round(countA/countObs*100.0) + "/" + Math.round(countC/countObs*100.0) + "/" + Math.round(countX/countObs*100.0) + "/" + Math.round(countL/countObs*100.0) + "/" + Math.round(countD/countObs*100.0));                		
                		System.out.println("	w% O/V/Q/S/A/C/X/L/D = " + Math.round(countOw/countObsWeighted*100.0) + "/" + Math.round(countVw/countObsWeighted*100.0) + "/" + Math.round(countQw/countObsWeighted*100.0) + "/" + Math.round(countSw/countObsWeighted*100.0) + "/" + Math.round(countAw/countObsWeighted*100.0) + "/" + Math.round(countCw/countObsWeighted*100.0) + "/" + Math.round(countXw/countObsWeighted*100.0) + "/" + Math.round(countLw/countObsWeighted*100.0) + "/" + Math.round(countDw/countObsWeighted*100.0));                		

                		if(countObs > 1) {
	                		outputFile.write(	lastId + "," + Math.round(countO/countObs*100.0) + "," + Math.round(countV/countObs*100.0) + "," + Math.round(countQ/countObs*100.0) +
	                							"," + Math.round(countS/countObs*100.0) + "," + Math.round(countA/countObs*100.0) + "," +
	                							Math.round(countC/countObs*100.0) + "," + Math.round(countX/countObs*100.0) + "," +
	                							Math.round(countL/countObs*100.0) + "," + Math.round(countD/countObs*100.0) + "," +
	                							Math.round(countOw/countObsWeighted*100.0) + "," + Math.round(countVw/countObsWeighted*100.0) + "," +
	                							Math.round(countQw/countObsWeighted*100.0) + "," + Math.round(countSw/countObsWeighted*100.0) + "," +
	                							Math.round(countAw/countObsWeighted*100.0) + "," + Math.round(countCw/countObsWeighted*100.0) + "," +
	                							Math.round(countXw/countObsWeighted*100.0) + "," + Math.round(countLw/countObsWeighted*100.0) + "," +
	                							Math.round(countDw/countObsWeighted*100.0) + "\n");
	                		countMultiple++;
                		}	
                	}
                	lastId = id;
                	taxClass = fields[5];
                	countUnique++;
                	countObs = 1;
                	countObsWeighted = Integer.parseInt(fields[6]) / 100.0; //count up score percentages
                	countO = countV = countQ = countS = countA = countC = countX = countL = countD = 0; 
                	countOw = countVw = countQw = countSw = countAw = countCw = countXw = countLw = countDw = 0; 
                	if(taxClass.length() > 2 || taxClass.length() == 0) {
                		System.out.println("Error. Invalid classification (" + fields[5] + ").");
                	} else if(taxClass.length() == 1) {
                		if(taxClass.equals("O")) {
                			countO = 1.0;
                			countOw = Integer.parseInt(fields[6]) / 100.0;
                		} else if(taxClass.equals("V")) {
                			countV = 1.0;
                			countVw = Integer.parseInt(fields[6]) / 100.0;                			
                		} else if(taxClass.equals("Q")) {
                			countQ = 1.0;
                			countQw = Integer.parseInt(fields[6]) / 100.0;
                		} else if(taxClass.equals("S")) {
                			countS = 1.0;
                			countSw = Integer.parseInt(fields[6]) / 100.0;
	            		} else if(taxClass.equals("A")) {
	            			countA = 1.0;
                			countAw = Integer.parseInt(fields[6]) / 100.0;
	            		} else if(taxClass.equals("C")) {
	            			countC = 1.0;
                			countCw = Integer.parseInt(fields[6]) / 100.0;
	            		} else if(taxClass.equals("X")) {
	            			countX = 1.0;
                			countXw = Integer.parseInt(fields[6]) / 100.0;
	            		} else if(taxClass.equals("L")) {
	            			countL = 1.0;
                			countLw = Integer.parseInt(fields[6]) / 100.0;
	            		} else if(taxClass.equals("D")) {
	            			countD = 1.0;
                			countDw = Integer.parseInt(fields[6]) / 100.0;
	            		} else {
	            			System.out.println("Error. Unknown classification (" + taxClass + ").");
	            			break;
	            		}	
                	} else { //length == 2
                		for(int i=0; i<2; i++) {
                			char halfClass = taxClass.charAt(i);
                			//System.out.println(taxClass + ": " + halfClass);
                    		if(halfClass == 'O') {
                    			countO = 0.5;
                    			countOw = (Integer.parseInt(fields[6]) / 100.0) * 0.5;
                    		} else if(halfClass == 'V') {
                    			countV = 0.5;
                    			countVw = (Integer.parseInt(fields[6]) / 100.0) * 0.5;               			
                    		} else if(halfClass == 'Q') {
                    			countQ = 0.5;
                    			countQw = (Integer.parseInt(fields[6]) / 100.0) * 0.5;
                    		} else if(halfClass == 'S') {
                    			countS = 0.5;
                    			countSw = (Integer.parseInt(fields[6]) / 100.0) * 0.5;
    	            		} else if(halfClass == 'A') {
    	            			countA = 0.5;
                    			countAw = (Integer.parseInt(fields[6]) / 100.0) * 0.5;
    	            		} else if(halfClass == 'C') {
    	            			countC = 0.5;
                    			countCw = (Integer.parseInt(fields[6]) / 100.0) * 0.5;
    	            		} else if(halfClass == 'X') {
    	            			countX = 0.5;
                    			countXw = (Integer.parseInt(fields[6]) / 100.0) * 0.5;
    	            		} else if(halfClass == 'L') {
    	            			countL = 0.5;
                    			countLw = (Integer.parseInt(fields[6]) / 100.0) * 0.5;
    	            		} else if(halfClass == 'D') {
    	            			countD = 0.5;
                    			countDw = (Integer.parseInt(fields[6]) / 100.0) * 0.5;
    	            		} else {
    	            			System.out.println("Error. Unknown classification (" + halfClass + ").");
    	            			break;
    	            		}                			
                		}
                	}
                } else {
                	countObs++;
                	countObsWeighted += Integer.parseInt(fields[6]) / 100.0; //count up score percentages
                	taxClass += "," + fields[5];
                	if(fields[5].length() > 2) {
                		System.out.println("Invalid classification (" + fields[5] + ").");
                		break;
                	} else if(fields[5].length() == 1) {
                		if(fields[5].equals("O")) {
                			countO += 1.0;
                			countOw += Integer.parseInt(fields[6]) / 100.0;
                		} else if(fields[5].equals("V")) {
                			countV += 1.0;
                			countVw += Integer.parseInt(fields[6]) / 100.0;                			
                		} else if(fields[5].equals("Q")) {
                			countQ += 1.0;
                			countQw += Integer.parseInt(fields[6]) / 100.0;                 			
                		} else if(fields[5].equals("S")) {
                			countS += 1.0;
                			countSw += Integer.parseInt(fields[6]) / 100.0; 
	            		} else if(fields[5].equals("A")) {
	            			countA += 1.0;
                			countAw += Integer.parseInt(fields[6]) / 100.0; 
	            		} else if(fields[5].equals("C")) {
	            			countC += 1.0;
                			countCw += Integer.parseInt(fields[6]) / 100.0; 
	            		} else if(fields[5].equals("X")) {
	            			countX += 1.0;
                			countXw += Integer.parseInt(fields[6]) / 100.0; 
	            		} else if(fields[5].equals("L")) {
	            			countL += 1.0;
                			countLw += Integer.parseInt(fields[6]) / 100.0; 
	            		} else if(fields[5].equals("D")) {
	            			countD += 1.0;
                			countDw += Integer.parseInt(fields[6]) / 100.0; 
	            		} else {
	            			System.out.println("Error. Unkown classification (" + fields[5] + ").");
	            			break;
	            		}	
                	} else { //length == 2
                		for(int i=0; i<2; i++) {
                			char halfClass = fields[5].charAt(i);
                    		if(halfClass == 'O') {
                    			countO += 0.5;
                    			countOw += (Integer.parseInt(fields[6]) / 100.0) * 0.5;
                    		} else if(halfClass == 'V') {
                    			countV += 0.5;
                    			countVw += (Integer.parseInt(fields[6]) / 100.0) * 0.5;               			
                    		} else if(halfClass == 'Q') {
                    			countQ += 0.5;
                    			countQw += (Integer.parseInt(fields[6]) / 100.0) * 0.5;
                    		} else if(halfClass == 'S') {
                    			countS += 0.5;
                    			countSw += (Integer.parseInt(fields[6]) / 100.0) * 0.5;
    	            		} else if(halfClass == 'A') {
    	            			countA += 0.5;
                    			countAw += (Integer.parseInt(fields[6]) / 100.0) * 0.5;
    	            		} else if(halfClass == 'C') {
    	            			countC += 0.5;
                    			countCw += (Integer.parseInt(fields[6]) / 100.0) * 0.5;
    	            		} else if(halfClass == 'X') {
    	            			countX += 0.5;
                    			countXw += (Integer.parseInt(fields[6]) / 100.0) * 0.5;
    	            		} else if(halfClass == 'L') {
    	            			countL += 0.5;
                    			countLw += (Integer.parseInt(fields[6]) / 100.0) * 0.5;
    	            		} else if(halfClass == 'D') {
    	            			countD += 0.5;
                    			countDw += (Integer.parseInt(fields[6]) / 100.0) * 0.5;
    	            		} else {
    	            			System.out.println("Error. Unknown classification (" + halfClass + ").");
    	            			break;
    	            		}                			
                		}
                	}               	
                }
            }
            System.out.println("Processed " + count + " asteroid observations."); 
            System.out.println("Processed " + countMultiple + " asteroid observations with multiple observations.");             
            System.out.println("Found " + countUnique + " unique asteroids.");
            sc.close();
            outputFile.close();
            
        } catch (Exception e) {         
            e.printStackTrace();
        }
    }	
	
}