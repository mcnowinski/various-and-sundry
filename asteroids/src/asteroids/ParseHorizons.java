package asteroids;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class ParseHorizons {

    public static void main(String[] args) {        
        try {
        	FileWriter indexFile = new FileWriter("C:\\Users\\mcnow\\Desktop\\asteroid_master_index.csv");
        	FileWriter spkidFile = new FileWriter("C:\\Users\\mcnow\\Desktop\\asteroid_spk_id.csv");
        	FileWriter desigFile = new FileWriter("C:\\Users\\mcnow\\Desktop\\asteroid_names_spk_id.csv");
        	
            File inputFile = new File("C:\\Users\\mcnow\\Desktop\\MI.DB");
            Scanner sc = new Scanner(inputFile);
            while(sc.hasNextLine()){
                String line = sc.nextLine();
                String[] fields = line.split(";");
                int id = Integer.parseInt(fields[0]);
                if(id < 90000000) { //asteroids only, comets >= 90000000
	                String name = fields[1];
	                String spk_id_string = fields[2];
	                String[] spk_ids = spk_id_string.split(",");
	                int spk_id = Integer.parseInt(spk_ids[0]);
	                //for(int i = 0; i<spk_ids.length; i++) {
	                	//System.out.println("	spk_id="+spk_ids[i]);
	                //	if(spk_ids.length > 0 && spk_ids[0].length() > 0) {
	                //		spkidFile.write(spk_ids[0]+","+id+"\n");
	                //	}
	                //}
                    desigFile.write(id+", "+name+", "+spk_id+"\n");	                
	                String desig = "";
	                if(fields.length > 3) {
	                	desig = fields[3];
	                    String[] desigs = desig.split(",");
	                    for(int i = 0; i<desigs.length; i++) {
	                    	if(desigs[i].length() > 0 && !desigs[i].equals(name)) {
	                    		desigFile.write(id+", "+desigs[i]+","+spk_id+"\n");
	                    	}                    	
	                    	//System.out.println("	desig="+desigs[i]);
	                    }
	                }
	                indexFile.write(id+","+name+"\n");
	                //System.out.println("id="+id+", name="+name+", spk_id="+spk_id+", des="+desig);
                }
            }
            sc.close();
            
            indexFile.close();
            spkidFile.close();
            desigFile.close();            
            
        } catch (Exception e) {         
            e.printStackTrace();
        }
    }	
	
}
