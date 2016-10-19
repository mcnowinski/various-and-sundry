import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Carvano {	
		
	private String line = "", id = "", lastId = "", spkid = "", taxClass = "", taxScore = "", taxScoreMatch = "", finalTaxClass = "";
	private int countObs = 0; //number of observations
	private int countObj = 0;
	private int countObjBad = 0;
	private int countObjMultObs = 0; //number of objects with multiple observations
	private int countObsForObj = 0; //number of observations for current object
	private int countClasses = 0;
	private double score; //observation taxonomic score (probability)
	private int bad; //observation has one or more colors with large photometric error?
	private int badO = 0, badV = 0, badQ = 0, badS = 0, badA = 0, badC = 0, badX = 0, badL = 0, badD = 0; //did this observation/classification have bad set?
	private int countO = 0, countV = 0, countQ = 0, countS = 0, countA = 0, countC = 0, countX = 0, countL = 0, countD = 0; //count classifications for this object
	private double countOw = 0.0, countVw = 0.0, countQw = 0.0, countSw = 0.0, countAw = 0.0, countCw = 0.0, countXw = 0.0, countLw = 0.0, countDw = 0.0; //sum scores of classifications for this object       
	private double aveScore = 0.0, stdevScore = 0.0;
	private FileWriter outputFile;
	
	// class combinations to filter by (separate by semicolons)
	private static boolean excludeBad = false;
	//private static final String taxClassesToMatch = "C;S";
	//private static final String taxClassesToMatch = "D;S";
	//private static final String taxClassesToMatch = "X;S";
	private static final String taxClassesToMatch = "C;Q";
	//private static final String taxClassesToMatch = "C;V";
	//private static final String taxClassesToMatch = "D;V";
	//private static final String taxClassesToMatch = "X;V";
	
	int countMatchObj = 0;
	int countMatchObjMultObs = 0;
	//
	
    public static void main(String[] args) { 	
    	Carvano carvano = new Carvano();
    	carvano.run();
	}

    private void processObject() {
    	countObj++; //increment object count
		if(countObsForObj > 1) { //track objects with multiple observations
			countObjMultObs++;
		}
		//calculate final combined class based on single taxonomic types
    	finalTaxClass = ((countC > 0) ? "C" : "") + ((countX > 0) ? "X" : "") + ((countD > 0) ? "D" : "") + 
						((countL > 0) ? "L" : "") + ((countA > 0) ? "A" : "") + ((countS > 0) ? "S" : "") +
						((countQ > 0) ? "Q" : "") + ((countO > 0) ? "O" : "") + ((countV > 0) ? "V" : "");
    	if(finalTaxClass.length() == 0 && excludeBad) {
    		countObjBad++;
    		taxScore = taxScoreMatch = "";
    		aveScore = 0.0;
    		stdevScore = 0.0;
    		System.out.println("Asteroid: " + lastId + "(" + spkid + ")");
    		System.out.println("	Does not have any good observations.");    		
    		return;
    	}
    	//
    	//if search terms are not *all* found, stop processing
    	//
    	String [] keywords = taxClassesToMatch.split(";");
    	boolean isMatch = true;
    	for(int i=0; i<keywords.length; i++) {
    		isMatch &= finalTaxClass.contains(keywords[i]);
    	}
    	if(!isMatch) {
    		taxScore = taxScoreMatch = "";
    		aveScore = 0.0;
    		stdevScore = 0.0;
    		System.out.println("Asteroid: " + lastId + " (" + spkid + ")");
    		System.out.println("	Classes = " + taxClass);
    		System.out.println("	Does not match " + taxClassesToMatch + ".");    		
    		return;
    	}
    	countMatchObj++;
		if(countObsForObj > 1) { //track objects with multiple observations
			countMatchObjMultObs++;
		} else {
			System.out.println("Asteroid: " + lastId + " (" + spkid + ")");
    		System.out.println("	Classes = " + taxClass);
    		System.out.println("	Does not have multiple observations.");  
			return; //only print out 
		}
    	//calc score of matching classes
		taxScoreMatch = ((taxClassesToMatch.contains("C") && countC > 0) ? String.format("%.5f", countCw/countC) + ";" : "") + ((taxClassesToMatch.contains("X") && countX > 0) ? String.format("%.5f", countXw/countX) + ";" : "") +
    			   		((taxClassesToMatch.contains("D") && countD > 0) ? String.format("%.5f", countDw/countD) + ";" : "") + ((taxClassesToMatch.contains("L") && countL > 0) ? String.format("%.5f", countLw/countL) + ";" : "") +
    			   		((taxClassesToMatch.contains("A") && countA > 0) ? String.format("%.5f", countAw/countA) + ";" : "") + ((taxClassesToMatch.contains("S") && countS > 0) ? String.format("%.5f", countSw/countS) + ";" : "") +
    			   		((taxClassesToMatch.contains("Q") && countQ > 0) ? String.format("%.5f", countQw/countQ) + ";" : "") + ((taxClassesToMatch.contains("O") && countO > 0) ? String.format("%.5f", countOw/countO) + ";" : "") +
    			   		((taxClassesToMatch.contains("V") && countV > 0) ? String.format("%.5f", countVw/countV) + ";" : "");
    	if(taxScoreMatch.substring(taxScoreMatch.length()-1, taxScoreMatch.length()).equals(";"))
    		taxScoreMatch = taxScoreMatch.substring(0, taxScoreMatch.length()-1);
    	//calc ave and stdev of matching class scores
    	String [] scores = taxScoreMatch.split(";");
    	for(int i=0; i<scores.length; i++) {
        	aveScore += Double.parseDouble(scores[i])/scores.length;
    	}
    	for(int i=0; i<scores.length; i++) {
    		stdevScore += Math.pow((aveScore-Double.parseDouble(scores[i])),2)/scores.length;
    	}
    	stdevScore = Math.sqrt(stdevScore);
		printObject();
    }
    
    private void printObject() {
		//print out previous object values
		System.out.println("Asteroid: " + lastId + " (" + spkid + ")");
		System.out.println("	# Obs = " + countObsForObj);  
		System.out.println("	Classes = " + taxClass);
		System.out.println("	# Classes = " + countClasses);
		System.out.println("	Scores = " + taxScore);	
		System.out.println("	Scores (" + taxClassesToMatch + ") = " + taxScoreMatch);			
		System.out.println("	Class = " + finalTaxClass);		
		System.out.println("	Cnt C/X/D/L/A/S/Q/O/V = " +
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
		System.out.println("	Ave Score = " + String.format("%.3f", aveScore));
		System.out.println("	Stdev Score = " + String.format("%.3f", stdevScore));
		
		try {
			if(taxScoreMatch.length() > 0) {
				String horizon_command = "./obs_tbl 'DES=" + spkid + ";' '" + String.format("%.3f", aveScore) + "_" + String.format("%.3f", stdevScore) + "_" + taxClassesToMatch.replace(";","") + "_" + spkid + ".txt';";
				outputFile.write(lastId + "," + spkid + "," + countObsForObj + "," + taxClass + "," + finalTaxClass + "," + countClasses + "," + taxScore + "," + taxScoreMatch + "," +
						String.format("%.5f", aveScore) + "," + String.format("%.5f", stdevScore) + "," + String.format("%.5f", stdevScore/aveScore) + "," + horizon_command + "\n");
			}	
		} catch (Exception e) {         
			e.printStackTrace();
		}		
    }
    
    private void processObservation(String [] fields) {  
        spkid = fields[19]; //grab jpl id
    	countObsForObj++; //increment obs count for this object
    	score = Double.parseDouble(fields[6])/100.0;
    	bad = Integer.parseInt(fields[8]);
    	//process taxonomic classification for this asteroid
    	if(fields[5].length() > 2 || fields[5].length() == 0) {
    		System.out.println("Error. Invalid classification (" + fields[5] + ").");
    	} else {
        	//count unique classes for this object
        	String [] classes = taxClass.split(";");
        	boolean foundMatch = false;
        	for(int i=0; i<classes.length; i++) {
        		if(classes[i].equals(fields[5])) {
        			foundMatch = true;
        			break;
        		}
        	}
        	if(!foundMatch)	//new class!
        		countClasses++;
        	//add this class to the list
        	taxClass += (taxClass.length() > 0 ? ";" : "")  + fields[5];
        	taxScore += (taxScore.length() > 0 ? ";" : "")  + String.format("%.2f", Double.parseDouble(fields[6])/100.0);
        	//process class
    		for(int i=0; i<fields[5].length(); i++) {
            	//taxScore += (taxScore.length() > 0 ? ";" : "")  + String.format("%.2f", Double.parseDouble(fields[6])/100.0);
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
    }
	
    private void run() {        
        try { 
        	
            //output file
        	String outputPathname = "C:\\Users\\me\\Desktop\\carvano_obs_out." + taxClassesToMatch.replace(";","")  + ".csv";
            outputFile = new FileWriter(outputPathname);
            outputFile.write("id,spkid,obs,classes,class,num_class,scores,scores_match,ave_score,sd_score,sd_score_pct,horizon\n");
                    
        	//input file
            //
			//"ID","AST_NUMBER","AST_NAME","AST_NAME_PROV_DESIG","AST_NUM_PROV_DESIG","TAX_CLASS","SCORE","MOID","BAD","LOG_REFLECTANCE_U","LOG_REFLECTANCE_ERROR_U","LOG_REFLECTANCE_G","LOG_REFLECTANCE_ERROR_G","LOG_REFLECTANCE_R","LOG_REFLECTANCE_ERROR_R","LOG_REFLECTANCE_I","LOG_REFLECTANCE_ERROR_I","LOG_REFLECTANCE_Z","LOG_REFLECTANCE_ERROR_Z","SPK
			//1,166,"Rhodope","-","166","C",78,"s394c9",0,0.86,8.0e-03,1,8.0e-03,1.03,4.0e-03,1.05,8.0e-03,1.04,0.01             	
            //
            File inputFile = new File("C:\\Users\\me\\Desktop\\carvano_obs.csv");
            //process input file
            String [] fields;
            Scanner sc = new Scanner(inputFile);
            sc.nextLine(); //skip header
            //process all lines
            while(sc.hasNextLine()){   
            	countObs++;
            	
                line = sc.nextLine();
                //System.out.println(line);
                fields = line.split(",");
                
                id = fields[3]; //AST_NAME_PROV_DESIG
                
                //set lastId after first read
                if(countObs == 1)
                	lastId = id;
                
                //new object? process previous object
                if(!id.equals(lastId)) {
                	processObject();
                	
            		//reset all values
                	lastId = id;
                	countObsForObj = 0;
                	countClasses = 0;
                	countO = countV = countQ = countS = countA = countC = countX = countL = countD = 0; 
                	badO = badV = badQ = badS = badA = badC = badX = badL = badD = 0;                 	
                	countOw = countVw = countQw = countSw = countAw = countCw = countXw = countLw = countDw = 0.0;
                	aveScore = stdevScore = 0.0;
                	taxClass = taxScore = "";
                }
                
                //process this observation of the current object
                if(excludeBad && Integer.parseInt(fields[8]) == 1) {
                } else {
                	processObservation(fields); //process current observation 
                }
            } //next observation
            
        	//if(lastId.length() > 0) { //wrap up last object
        	processObject();	
        	//}
        	
            System.out.println("Processed " + countObs + " observations.");
            if(excludeBad)
                System.out.println("Found " + countObj + " asteroids (" + (countObj-countObjBad) + " with at least one good observation).");
            else
            	System.out.println("Found " + countObj + " asteroids.");
            System.out.println("Processed " + countObjMultObs + " asteroids with multiple " + (excludeBad ? "good " : "") + "observations.");
            System.out.println("Found " + countMatchObj + " matching (" + taxClassesToMatch + ") asteroids (" + countMatchObjMultObs + " with multiple " + (excludeBad ? "good " : "") + "observations).");            
            
            sc.close();
            outputFile.close();
            System.out.println("Saved output to " + outputPathname + ".");
            
        } catch (Exception e) {         
            e.printStackTrace();
        }
    }	
	
}