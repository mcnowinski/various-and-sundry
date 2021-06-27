

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseLCDB{

    public static void main(String[] args) {        
        try {   	
        	FileWriter outputFile = new FileWriter("C:\\Users\\mcnow\\Desktop\\lcdb.csv");
            File inputFile = new File("C:\\Users\\mcnow\\Desktop\\lc_summary_pub.txt");
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
//NUMBER    NAME                           DESIG                FAM      S CLASS      S F DIA.     S H      B  S G      G2     S F ALBEDO F PERIOD        P DESC          F AMIN AMAX U  NOTES BIN SAM SurvA NEX PRI
/*
Number I7 1-7 MPC-assigned number; empty if no number assigned
EntryFlag A1 9 “*” indicates a vetted new/revised record since the last major release. Other flags are used during the vetting
process that will be removed for the annual vetted release.
Name A30 11-40 Summary: MPC-assigned name, or designation if not named Details: Approximate mid-date of observations
Desig A20 42-61 MPC primary designation, if assigned
Family A8 63-70 The orbital group or collisional family
CSource A1 72 Flag indicating source for taxonomic classification
Class A10 74-83 The taxonomic class
DiamSource A1 85 Flag indicating the source for the diameter
DiamFlag A1 87 Flag (e.g., < or >) that qualifies the diameter
Diam F8.3 89-96 Adopted Diameter (km)
HSource A1 98 Flag indicating the source of the H value
H F6.3 100-105 Adopted absolute magnitude H
HBand A2 107-108 The photometric band of H
GSource A1 110 Flag indicating the source of the G value
G F6.3 112-117 Adopted phase slope parameter (G or G1; see 3.1.1)
G2 F6.3 119-124 Adopted phase slope parameter (G2; 3.1.1)
AlbSource A1 126 Flag indicating the source of the albedo value
AlbFlag A1 128 Flag (e.g., < or >) qualifying the albedo value
Albedo F6.4 130-135 Adopted Albedo (same band as H)
PFlag A1 137 Period qualifier
Period F13.8 139-151 Rotation period, in hours; usually synodic
PDescrip A15 153-167 Description of period if PFlag = 'D'; e.g., "long"
AmpFlag A1 169 Amplitude flag, e.g., > or <
AmpMin F4.2 171-174 Minimum reported amplitude
AmpMax F4.2 176-179 Maximum reported amplitude
U A2 181-182 Lightcurve Quality
Notes A5 184-188 Qualifying flags for record
Binary A3 190-192 ? = Suspected; B = Binary; M = Multiple; blank if none
Pole A3 194-196 Y/N; Y = Pole position reported in spin axis table
Survey A5 198-202 Type of Survey if result from large survey programs, e.g., PTF, WTF, Kepler, TESS, PanSTARRS, etc. See Notes below and Section 5
NotesEx A3 204-206 Y/N; Y = Entry in lc_notesex table
Private PRI A3 208-210 Y/N; Y = Unpublished, contact named observer to request details
 */
			csv = "Number,EntryFlag,Name,Desig,Family,CSource,Class,DSource,DFlag,Diameter,HSource,H,HBand,ASource,AlbedoFlag,Albedo,PFlag,Period,PDescrip,AmpFlag,AmpMin,AmpMax,U,Notes,Binary,Private,Pole,SparseData,WideField";           
            outputFile.write(csv + "\n");
            //System.out.println(columnCsv);
                 
            //space
            sc.nextLine();
            
            int count = 0;
            while(sc.hasNextLine()){
                line = sc.nextLine();
                System.out.println(line);
                
                //increase dimension to handle more columns!
                String [] data = new String[32];
                
                int index = 0;
                //new LCDB format
/*              Number I7 1-7 */data[index++] = line.substring(0, 7); //MPC-assigned number; empty if no number assigned
/*              EntryFlag A1 9 */data[index++] = line.substring(8, 9); //“*” indicates a vetted new/revised record since the last major release. Other flags are used during the vetting process that will be removed for the annual vetted release.
/*              Name A30 11-40 */data[index++] = line.substring(10, 40); //Summary: MPC-assigned name, or designation if not named Details: Approximate mid-date of observations
/*              Desig A20 42-61 */data[index++] = line.substring(41, 61); //MPC primary designation, if assigned
/*              Family A8 63-70 */data[index++] = line.substring(62, 70); //The orbital group or collisional family
/*              CSource A1 72 */data[index++] = line.substring(71, 72); //Flag indicating source for taxonomic classification
/*              Class A10 74-83 */data[index++] = line.substring(73, 83); //The taxonomic class
/*              DiamSource A1 85 */data[index++] = line.substring(84, 85); //Flag indicating the source for the diameter
/*              DiamFlag A1 87 */data[index++] = line.substring(86, 87); //Flag (e.g., < or >) that qualifies the diameter
/*              Diam F8.3 89-96 */data[index++] = line.substring(88, 96); //Adopted Diameter (km)
/*              HSource A1 98 */data[index++] = line.substring(97, 98); //Flag indicating the source of the H value
/*              H F6.3 100-105 */data[index++] = line.substring(99, 105); //Adopted absolute magnitude H
/*              HBand A2 107-108 */data[index++] = line.substring(106, 108); //The photometric band of H
/*              GSource A1 110 */data[index++] = line.substring(109, 110); //Flag indicating the source of the G value
/*              G F6.3 112-117 */data[index++] = line.substring(111, 117); //Adopted phase slope parameter (G or G1; see 3.1.1)
/*              G2 F6.3 119-124 */data[index++] = line.substring(118, 124); //Adopted phase slope parameter (G2; 3.1.1)
/*              AlbSource A1 126 */data[index++] = line.substring(125, 126); //Flag indicating the source of the albedo value
/*              AlbFlag A1 128 */data[index++] = line.substring(127, 128); //Flag (e.g., < or >) qualifying the albedo value
/*              Albedo F6.4 130-135 */data[index++] = line.substring(129, 135); //Adopted Albedo (same band as H)
/*              PFlag A1 137 */data[index++] = line.substring(136, 137); //Period qualifier
/*              Period F13.8 139-151 */data[index++] = line.substring(138, 151); //Rotation period, in hours; usually synodic
/*              PDescrip A15 153-167 */data[index++] = line.substring(152, 167); //Description of period if PFlag = 'D'; e.g., "long"
/*              AmpFlag A1 169 */data[index++] = line.substring(168, 169); //Amplitude flag, e.g., > or <
/*              AmpMin F4.2 171-174 */data[index++] = line.substring(170, 174); //Minimum reported amplitude
/*              AmpMax F4.2 176-179 */data[index++] = line.substring(175, 179); //Maximum reported amplitude
/*              U A2 181-182 */data[index++] = line.substring(180, 182); //Lightcurve Quality
/*              Notes A5 184-188 */data[index++] = line.substring(183, 188); //Qualifying flags for record
/*              Binary A3 190-192 */data[index++] = line.substring(189, 192); //? = Suspected; B = Binary; M = Multiple; blank if none
/*              Pole A3 194-196 */data[index++] = line.substring(193, 196); //Y/N; Y = Pole position reported in spin axis table
/*              Survey A5 198-202 */data[index++] = line.substring(197, 202); //Type of Survey if result from large survey programs, e.g., PTF, WTF, Kepler, TESS, PanSTARRS, etc. See Notes below and Section 5
/*              NotesEx A3 204-206 */data[index++] = line.substring(203, 206); //Y/N; Y = Entry in lc_notesex table
/*              Private PRI A3 208-210 */data[index++] = line.substring(207, 210); //Y/N; Y = Unpublished, contact named observer to request details
//                /*Number     NUMBER   I7            */data[index++] = line.substring(0, 7);   //Blank if no MPC assigned number
//                /*EntryFlag           A1              */ data[index++] = line.substring(8,9); //   * new record since last pubic release (see notes)
//                /*Name       NAME     A30         */ data[index++] = line.substring(10,40); //   Summary: Name or designation
//                //                                        Details: Publication reference
//                /*Desig      DESIG    A20         */ data[index++] = line.substring(41,61); //   Summary: MPC Designation (see notes)
//                //                                        Details: Appxoimate mid-date of observations
//                /*Family     FAM      A8          */ data[index++] = line.substring(62,70); //   Family/Group association
//                /*CSource             A1             */ data[index++] = line.substring(71,72); //   Source of taxonomic class
//                /*Class      CLASS    A5          */ data[index++] = line.substring(73,78); //   Taxonomic class
//                /*DSource             A1             */ data[index++] = line.substring(79,80); //   Source of diameter
//                /*DFlag               A1             */ data[index++] = line.substring(81,82); //   Diameter flag
//                /*Diameter   DIA.     F9.4        */ data[index++] = line.substring(83,92); //   in km
//                /*HSource             A1             */ data[index++] = line.substring(93,94); //   Source of H value
//                /*H          H        F5.2       */ data[index++] = line.substring(95,100); //   H (absolute magnitude)
//                /*HBand               A2            */ data[index++] = line.substring(101,102); //   BVRI,S(UGRIZ), V if Blank
//                /*ASource             A1            */ data[index++] = line.substring(104,105); //   Source of albedo value
//                /*AlbedoFlag          A1            */ data[index++] = line.substring(106,107); //   Albedo qualifier (> or <)
//                /*Albedo     A        F6.4      */ data[index++] = line.substring(108,114); //   Albedo
//                /*PFlag               A1            */ data[index++] = line.substring(115,116); //   Period qualifier
//                /*Period     PERIOD   F13.8     */ data[index++] = line.substring(117,130); //   Period, in hours
//                /*PDescrip   P DESC   A15       */ data[index++] = line.substring(131,146); //   Period description if not numeric value,
//                //                                        e.g., long
//                /*AmpFlag             A1            */ data[index++] = line.substring(147,148); //   Amplitude flag
//                /*AmpMin     AMIN     F4.2      */ data[index++] = line.substring(149,153); //   Minimum amplitude of a range.   (See Notes)
//                /*AmpMax     AMAX     F4.2      */ data[index++] = line.substring(154,158); //   Maximum amplitude of a range OR (See Notes)
//                //                                        amplitude if no range.
//                /*U          U        A2        */ data[index++] = line.substring(159,161); //   Lightcurve Quality
//                /*Notes      NOTES    A5        */ data[index++] = line.substring(162,167); //   Qualifying flags for lightcurve record
//                /*Binary     BIN      A3        */ data[index++] = line.substring(168,171); //   Binary Flag
//                /*Private    PRI      A3        */ data[index++] = line.substring(172,175); //   Y = Unpublished, contact named observer
//                //                                        to request details
//                /*Pole       SAM      A3        */ data[index++] = line.substring(176,179); //   Spin Axis and/or Shape Model availableb
//                /*SparseData SD	    A2        */ data[index++] = line.substring(180,182); //   Y = Result based on sparse data (see Sec. 5)    
//                /*WideField  WF       A2        */ data[index++] = line.substring(183,185); //   Y = Result based on wide field data (see Sec. 5)               
                               
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
                System.out.println(csv);
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
//DFlag               A1             82   Diameter flag
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
//AmpMin     AMIN     F4.2      150-153   Minimum amplitude of a range.   (See Notes)
//AmpMax     AMAX     F4.2      155-158   Maximum amplitude of a range OR (See Notes)
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
//--------------
//Entry flags (column 9)
//The once-a-year "official" release of the LCDB will have only of two values in
//column 9
//
//  <blank>  The entry was last entered/updated more than one annual release
//           prior to the current version.
//
//     *     The entry is new since the last annual release.