

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseLCDB{

    public static void main(String[] args) {        
        try {   	
        	FileWriter outputFile = new FileWriter("C:\\Users\\Me\\Desktop\\lcdb.csv");
            File inputFile = new File("C:\\Users\\Me\\Desktop\\LC_SUM_PUB.txt");
            String line;
            
            Scanner sc = new Scanner(inputFile);
            
            //title lines
            sc.nextLine();
            sc.nextLine();         
            sc.nextLine();
            
            //header column titles
            line = sc.nextLine();
//            line = line.replaceFirst("P DESC", "PDESC");
//            String [] columns = line.split(" ");
            String csv = "";
            boolean firstColumn = true;
//            for (String columnTitle : columns) {
//            	if(columnTitle.length() > 0) {
//            		if(firstColumn) {
//            			csv = columnTitle;
//            			firstColumn = false;
//            		} else {
//            			csv += "," + columnTitle;
//            		}
//            	}
//            }
            csv = "NUMBER,NAME,DESIG,FAM,S_TAX,CLASS,S_DIA,F_DIA,DIA,S_H,H,S_A,F_A,A,F_PERIOD,PERIOD,PDESC,F_AMP,AMIN,AMAX,U,NOTES,BIN,PRI,SAM,SD,WF";
            outputFile.write(csv + "\n");
            //System.out.println(columnCsv);
                 
            //space
            sc.nextLine();
            
            int count = 0;
            while(sc.hasNextLine()){
                line = sc.nextLine();
                
                //increase dimension to handle more columns!
                String [] data = new String[27];
                
                int index = 0;
                /*String NUMBER*/ data[index++] = line.substring(0, 7); 
                /*String NAME*/ data[index++] = line.substring(10, 40); 
                /*String DESIG*/ data[index++] = line.substring(41, 61);  
                /*String FAM*/ data[index++] = line.substring(62, 70);
                /*String S_TAX*/ data[index++] = line.substring(71, 72);
                /*String CLASS*/ data[index++] = line.substring(73, 78); 
                /*String S_DIA*/ data[index++] = line.substring(79, 80);
                /*String F_DIA*/ data[index++] = line.substring(81, 82);
                /*String DIA*/ data[index++] = line.substring(83, 92);
                /*String S_H*/ data[index++] = line.substring(93, 94);
                /*String H*/ data[index++] = line.substring(95, 100);
                /*String S_A*/ data[index++] = line.substring(104, 105);
                /*String F_A*/ data[index++] = line.substring(106, 107);
                /*String A*/ data[index++] = line.substring(108, 114);
                /*String F_PERIOD*/ data[index++] = line.substring(115, 116);
                /*String PERIOD*/ data[index++] = line.substring(117, 130);  
                /*String PDESC*/ data[index++] = line.substring(131, 146); 
                /*String F_AMP*/ data[index++] = line.substring(147, 148);
                /*String AMIN*/ data[index++] = line.substring(149, 153);                 
                /*String AMAX*/ data[index++] = line.substring(154, 158);
                /*String U*/ data[index++] = line.substring(159, 161);
                /*String NOTES*/ data[index++] = line.substring(162, 167);
                /*String BIN*/ data[index++] = line.substring(168, 171);  
                /*String PRI*/ data[index++] = line.substring(172, 175);
                /*String SAM*/ data[index++] = line.substring(176, 179);
                /*String SD*/ data[index++] = line.substring(180, 182);
                /*String WF*/ data[index++] = line.substring(183, 185);
                
                csv = "";
                firstColumn = true;
                for(String column : data) {
                	if(firstColumn) {
                		csv = column.trim();
                		firstColumn = false;
                	} else {
                		csv += "," + column.trim();
                	}
                }
                
                outputFile.write(csv + "\n");
                //System.out.println(csv);
                count++;
            }
            System.out.println("Count="+count);
            sc.close();
            outputFile.close();     
            
        } catch (Exception e) {         
            e.printStackTrace();
        }
    }	
	
}

//Field      Header   Format        Pos   Notes
//--------------------------------------------------------------------------------
//Number     NUMBER   I7            1-7   Blank if no MPC assigned number
//EntryFlag           A1              9   * new record since last pubic release (see notes)
//Name       NAME     A30         11-40   Summary: Name or designation
//                                        Details: Publication reference
//Desig      DESIG    A20         42-61   Summary: MPC Designation (see notes)
//                                        Details: Appxoimate mid-date of observations
//Family     FAM      A8          63-70   Family/Group association
//CSource             A1             72   Source of taxonomic class
//Class      CLASS    A5          74-78   Taxonomic class
//DSource             A1             80   Source of diameter
//DFlag               A1             82   Diameter flhag
//Diameter   DIA.     F9.4        84-92   in km
//HSource             A1             94   Source of H value
//H          H        F5.2       96-100   H (absolute magnitude)
//HBand               A2            102   BVRI,S(UGRIZ), V if Blank
//ASource             A1            105   Source of albedo value
//AlbedoFlag          A1            107   Albedo qualifier (> or <)
//Albedo     A        F6.4      109-114   Albedo
//PFlag               A1            116   Period qualifier
//Period     PERIOD   F13.8     118-130   Period, in hours
//PDescrip   P DESC   A15       132-146   Period description if not numeric value,
//                                        e.g., long
//AmpFlag             A1            148   Amplitude flag
//AmpMin     AMIN     F4.2      150-153   Minimum amplitude of a range.
//AmpMax     AMAX     F4.2      155-158   Maximum amplitude of a range OR
//                                        amplitude if no range.
//U          U        A2        160-161   Lightcurve Quality
//Notes      NOTES    A5        163-167   Qualifying flags for lightcurve record
//Binary     BIN      A3        169-171   Binary Flag
//Private    PRI      A3        173-175   Y = Unpublished, contact named observer
//                                        to request details
//Pole       SAM      A3        177-179   Spin Axis and/or Shape Model available
//
//SparseData SD	    A2        181-182   Y = Result based on sparse data (see Sec. 5)
//
//WideField  WF       A2        184-185   Y = Result based on wide field data (see Sec. 5)
//
//Notes:
//Entry flags (column 9)
//The once-a-year "official" release of the LCDB will have only of two values in
//column 9
//
//  <blank>  The entry was last entered/updated more than one annual release
//           prior to the current version.
//
//     *     The entry is new since the last annual release.
