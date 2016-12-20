import java.awt.*;

public class Orbit {
	
	private static final double G = 6.674E-11; 	//gravitational constant, (Nm2)/(kg2)
	private static final double M = 1.989E+30; 	//mass of sun, kg
	private static final double AU = 1.496E+11; //astronomical unit, m
	
	//https://www.researchgate.net/publication/228650471_Journey_to_Mars_the_physics_of_travelling_to_the_red_planet
	private static final double sphereOfInfluence = 20E+8/AU; //based on Mars = 5.7x10E8 m and Earth = 9.2x10E8 m, with a little wiggle room
	
	private String name;
	private double a; 		//semimajor axis in AU
	private double e; 		//eccentricity 
	private double T; 		//orbital period in years
	private double omega; 	//angle between vernal equinox (x-axis) and perihelion
	
	protected Position [] positions;
	
	private double [] psis = new double[0]; //intersections
	
	private Orbit destinationOrbit = null; //for transfer orbit, what is radius of destination orbit in AU?
	private double deltaDayDestination = -1.0;	//difference between transfer orbit day and real day
	private double destinationArrivalDay = -1.0;
	
	private static final int incV = 10000; //true anomaly increment
	
	public Orbit(String name, double semiMajorAxis, double eccentricity) {
		//set properties
		this.name = name;
		a = semiMajorAxis;
		e = eccentricity;
		omega = 0.0; //default to zero

		//period in AU
		T = Math.sqrt(a*a*a); 		
		
		//calc orbit parameters
		calcPositions();
	}	

	//
	//Exercise 4A
	//
	public void drawOrbit(Graphics g, Color color, double scale, double xOffset, double yOffset) {
	
	}
	//
	//
	//
	
	private void calcPositions() {
		double x, y, psi, t; //psi is a parameter that goes from 0 to 2*PI
		
		positions = new Position[incV];
		
		//calculate orbit
		//http://www.pa.msu.edu/~stump/champ/mech3f01.pdf
		double deltaPsi = 2.0*Math.PI/incV;
		for(int i=0; i<incV; i++) {
			psi = deltaPsi*i;
			
			//parametric Kepler equations
			t = (psi-e*Math.sin(psi))/(2.0*Math.PI);
			x = a*(Math.cos(psi)-e);
			y = a*Math.sqrt(1.0-e*e)*Math.sin(psi);
			
			//translate by omega
			double d = Math.sqrt(x*x+y*y);
			double v = Math.atan2(y, x);
			x = d * Math.cos(v + omega);
			y = d * Math.sin(v + omega);			
			
			if(v < 0) v += 2*Math.PI;
			
			positions[i] = new Position(x, -y, v, t); //translate y to pixel axis
		}
		
		//update intersection with destination orbit, if applicable
		if(destinationOrbit != null)
			calcIntersectionDestinationOrbit();
		
		if(deltaDayDestination >= 0)
			calcDestinationRendezvous();
	}	
	
	public double psiToDay(double psi) {
		double t = (psi-e*Math.sin(psi))/(2.0*Math.PI); //time as fraction of period
		
		return t*T*365.0; //convert to days
	}
	
	public double vToDay(double v_deg) { //given angle, get day in first period
		v_deg = normalizeAngle(v_deg);
		
		int i = 0;
		while(i<incV && normalizeAngle(Math.toDegrees(positions[i].getV())) < v_deg) {
			i++;
		}		
		
		if(i >= incV) {
			System.out.println("Error. Invalid angle in vToDay.");
			return 0.0;
		}
		
		//System.out.println("Found matching angle at i = " + i);
		
		return positions[i].getT()*getPeriod()*365.0;
	}
	
	public double normalizeAngle(double v_deg) {
		//normalize the angle
		//reduce the angle  
		v_deg =  v_deg % 360; 
		// force it to be the positive remainder, so that 0 <= angle < 360  
		v_deg = (v_deg + 360) % 360; 
		
		return v_deg;
	}
	
	public Position getPositionAt(double day) {
		long numDaysInPeriod = Math.round(T*365);
		long numCompletedPeriods = Math.round(day) / numDaysInPeriod;
		double numRemainingDaysInPeriod = day - numCompletedPeriods*numDaysInPeriod;
		
		//get fractional time in period
		double t = (double) numRemainingDaysInPeriod / (double) numDaysInPeriod;
		//find the closest position to this fractional time in the orbital period	
		int i = 0;
		while(i<incV && positions[i].getT() < t) {
			i++;
		}			
	
		if(i >= incV) {
			System.out.println("Error. Invalid day in getPosition.");
			Position dummy = new Position(0.0, 0.0, 0.0, 0.0);
			return dummy;
		}
		
		return positions[i];
	}

	public Vector getVelocityVectorAt(double day) {
		long numDaysInPeriod = Math.round(T*365);
		long numCompletedPeriods = Math.round(day) / numDaysInPeriod;
		double numRemainingDaysInPeriod = day - numCompletedPeriods*numDaysInPeriod;
		
		//get fractional time in period
		double t = (double) numRemainingDaysInPeriod / (double) numDaysInPeriod;
		//find the closest position to this fractional time in the orbital period	
		int i = 0;
		while(i<incV && positions[i].getT() < t) {
			i++;
		}			
	
		//should not get here!
		if(i >= incV) {
			Vector dummy = new Vector(0.0, 0.0);
			return dummy;
		}
	
		//get velocity
		double V = getVelocity(day);
		
		//calculate angle
		double dx = 0.0;
		double dy = 0.0;
		if(i == incV-1) {
			dx = positions[0].getX() - positions[incV-2].getX();
			dy = positions[0].getY() - positions[incV-2].getY();
		} else if(i == 0) {
			dx = positions[1].getX() - positions[incV-1].getX();
			dy = positions[1].getY() - positions[incV-1].getY();				
		} else {
			dx = positions[i+1].getX() - positions[i-1].getX();
			dy = positions[i+1].getY() - positions[i-1].getY();			
		}
		double angle = Math.atan2(-dy, dx);
		//System.out.println("Tangential angle = " + Math.toDegrees(angle) + " deg");
		double x = V*Math.cos(angle);
		double y = V*Math.sin(angle);
		
		return new Vector(x, y);
	}	
	
	public double getDestinationArrivalDay() {
		return destinationArrivalDay;
	}
	
	public double getPeriod() {
		return T;
	}
    
	public double getSemimajorAxis() {
		return a;
	}	

	public double getEccentricity() {
		return e;
	}
	
	public double [] getPsis() {
		return psis;
	}
	
	public String getName() {
		return name;
	}
	
	public void setSemimajorAxis(double a) {
		this.a = a;
		//recalc period in AU
		T = Math.sqrt(a*a*a);
		calcPositions(); //doc recalc orbit
	}	

	public void setEccentricity(double e) {
		this.e = e;
		calcPositions(); //doc recalc orbit
	}
	
	public void setOmega(double omega_deg) {
		this.omega = Math.toRadians(omega_deg);
		calcPositions(); //doc recalc orbit
	}	
	
	public double getOmega() {
		return omega;
	}

	public void setDestinationOrbit(Orbit orbit) {
		destinationOrbit = orbit;
		calcPositions(); //doc recalc orbit		
	}	
	
	public void setDeltaDayDestination(double deltaDay) {
		deltaDayDestination = deltaDay;
	}
	
	public double getVelocity(double day) {
		Position position = getPositionAt(day);
		//get from sun to current orbital position
		double r = Math.sqrt(position.getX()*position.getX() + position.getY()*position.getY());
		r *= AU; //convert AU to m
		
		double a_m = a * AU; //get semimajor axis in m
		
		//vis-viva equation
		//v2 = G M (2/r - 1/a)
		double velocity = Math.sqrt(G * M * (2.0/r - 1.0/a_m)); //in m/s
		
		return velocity;
	}	

	public void drawPosition(double days, Graphics g, Color color, boolean fill, double scale, double xOffset, double yOffset) {	
		drawPosition(days, g, color, fill, scale, xOffset, yOffset, 10, 10);
	}

	public void drawPosition(double days, Graphics g, Color color, boolean fill, double scale, double xOffset, double yOffset, int symbolWidth, int symbolHeight) {	
		Position pos = 	getPositionAt(days);
		
        g.setColor(color);
        if(fill)
        	g.fillOval((int) Math.round(pos.getX()*scale+xOffset-symbolWidth/2), (int) Math.round(pos.getY()*scale+yOffset-symbolHeight/2), symbolWidth, symbolHeight);
        else
        	g.drawOval((int) Math.round(pos.getX()*scale+xOffset-symbolWidth/2), (int) Math.round(pos.getY()*scale+yOffset-symbolHeight/2), symbolWidth, symbolHeight);        	
	}	
	
	public void drawMarker(double days, Graphics g, Color color, boolean fill, boolean rect, double scale, double xOffset, double yOffset) {	
		Position pos = 	getPositionAt(days);
		
        g.setColor(color);
        if(fill)
        	if(rect)
        		g.fillRect((int) Math.round(pos.getX()*scale+xOffset-5), (int) Math.round(pos.getY()*scale+yOffset-5), 10, 10);
        	else
        		g.fillOval((int) Math.round(pos.getX()*scale+xOffset-5), (int) Math.round(pos.getY()*scale+yOffset-5), 10, 10);        		
        else
        	if(rect)
        		g.drawRect((int) Math.round(pos.getX()*scale+xOffset-5), (int) Math.round(pos.getY()*scale+yOffset-5), 10, 10); 
        	else
        		g.drawOval((int) Math.round(pos.getX()*scale+xOffset-5), (int) Math.round(pos.getY()*scale+yOffset-5), 10, 10);        		
	}
	
	public void print() {
		System.out.println("*");
		System.out.println("Name:                   " + name);		
		System.out.println("Semimajor Axis, a (AU): " + a);	
		System.out.println("Eccentricity, e:        " + e);
		System.out.println("Period, T (years):      " + T);
		System.out.println("Orbit (t/T, v (rad), x (AU), y (AU)):");		
		for(int i=0; i<incV; i++) {
			System.out.println(positions[i].getT() + "," + positions[i].getV() + "," + positions[i].getX() + "," + positions[i].getY());
		}
		System.out.println("*");		
	}
	
	public void calcDestinationRendezvous() {
		int numOrbits = 0;
		final int maxOrbits = 100;
		while(numOrbits < maxOrbits) {
			for(int i=0; i<psis.length; i++) {
	    		double spaceshipDay = psiToDay(psis[i]) + numOrbits*this.getPeriod()*365.0;
	    		Position spaceshipPosition = getPositionAt(spaceshipDay);
	    		Position marsPosition = destinationOrbit.getPositionAt(spaceshipDay + deltaDayDestination);
	    		double distance = spaceshipPosition.getDistanceTo(marsPosition);
	    		if(distance <= sphereOfInfluence) {
	    			destinationArrivalDay = spaceshipDay + deltaDayDestination;
	    			return;
	    		}
			}
			numOrbits++;
		}
		
		destinationArrivalDay = -1.0;
	}
	
    public double [] calcIntersectionDestinationOrbit() {
    	psis = new double[0];
    	
        double r = destinationOrbit.getSemimajorAxis();
        
        double parameter = (a-r)/(a*e);
        //round to two decimal places to help with == 1
        parameter = Math.round(parameter*100)/100.0;
        if(Math.abs(parameter) > 1) {
        	//no solutions
        } else if(Math.abs(parameter) == 1) {
        	//one solution
        	psis = new double[1];
        	psis[0] = Math.acos(parameter);       	
        } else {
        	//two solutions
        	psis = new double[2];
        	psis[1] = Math.acos(parameter);
        	psis[0] = Math.acos(-parameter) + Math.PI;  //get intersection on the "right" side 
        }
        
    	return psis;
    }	
}
