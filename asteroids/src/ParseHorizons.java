import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class ParseHorizons {
	
	//constructor
	public ParseHorizons() {
		
	}
	
	//parse file
	public static void parseHorizonsFile(String path) {
		
		try {
			
			//open input file
			File inputFile = new File(path);
			//does file exist?
			if(inputFile.isDirectory() || !inputFile.exists()) { 
				System.out.println("Input file (" + path + ") does not exist!");
			    return;
			}
			
	        Scanner sc = new Scanner(inputFile);
	        while(sc.hasNextLine()) {
	            String line = sc.nextLine();
	            
	            System.out.println(line);	
	        }
	        
	        sc.close();
	        
        } catch (Exception e) {         
            e.printStackTrace();
        }
	}
	
	//main
    public static void main(String[] args) {        
    	parseHorizonsFile("C:\\temp\\out.txt");
    }	
	
}