
public class MyJourneyToMars extends JourneyToMars {
	
	//
	//Exercise 3A
	//
    @Override
	public void resetDay() {
		mission.setDay(0.0);   	
    }
    //
    //
    //

	//
	//Exercise 3C
	//    
    @Override
	public void addDay() {
    	mission.incrementDay(1.0);   	
    }
    //
    //
    //
    
    //
	//Exercise 3D
	//
    @Override
	public void minusDay() {
    	mission.incrementDay(-1.0);   	
    } 
    //
    //
    //
    
	//main routine
	public static void main(String [] args) {
		//create an instance of MyJourneyToMars
		MyJourneyToMars j2m = new MyJourneyToMars();
		
		//orbits
		MyOrbit earth = new MyOrbit("Earth", Mission.rEarth, Mission.eEarth);
		MyOrbit mars = new MyOrbit("Mars", Mission.rMars, Mission.eMars);
		MyOrbit earth2Mars = new MyOrbit("Earth to Mars", Mission.aMin_toMars, Mission.eMin);
    	earth2Mars.setDestinationOrbit(mars);
    	MyOrbit mars2Earth = new MyOrbit("Mars to Earth", Mission.aMax_toEarth, Mission.eMin);
    	mars2Earth.setOmega(180.0); //return trip
    	mars2Earth.setDestinationOrbit(earth);		
		
		//launch the app with a new mission
		j2m.launch(new MyMission(earth, mars, earth2Mars, mars2Earth));
	}

}
