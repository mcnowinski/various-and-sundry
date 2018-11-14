import java.awt.*;
import java.time.LocalDateTime;

import javax.swing.*;

public class Mission extends JPanel {
	
	private static final long serialVersionUID = 1;
	
	private Orbit earth;
	private Orbit mars;
	private Orbit earth2Mars;
	private Orbit mars2Earth;

	protected double day = 0.0; //time increment
	
	private double departureDay = -1; //day of departure from Earth to Mars, -1 is unset
	private double returnDay = -1; //day of departure from Mars to Earth, -1 is unset	
      
	//jpanel dimensions
    protected int width;
    protected int height;
    
    private double ang_old = 0.0;
    
    private boolean showEarth2Mars = false;
    private boolean showMars2Earth = false;
    private boolean showEarth2MarsRetrograde = false;
    
    private Position [] marsViewHistory = new Position[10];
    
    private static final double scale = 125.0;  
    
    //orbital constants
	public static final double rEarth = 1.000;
	public static final double rMars = 1.524;
	public static final double eEarth = 0.000; //actual e=0.0167, but assume circular
	public static final double eMars = 0.000;  //actual e=0.0935, but assume circular
	public static final double omegaEarth = 0.0; //actual 102.94719 longitude of the periapsis in degrees
	public static final double omegaMars = 0.0;  //actual 336.04084 longitude of the periapsis	
	
	public static final double aMin_toMars = (rMars + rEarth) / 2.0;
	public static final double aMax_toMars = 5.0;
	public static final double aMin_toEarth = 0.8; //actual is rMars / 2.0, but too close to sun
	public static final double aMax_toEarth = aMin_toMars;	
	
	public static final double eMax = 0.900;
	public static final double eMax_toMars = 1.0 - rEarth/aMax_toMars;	
	public static final double eMax_toEarth = rMars/aMin_toEarth - 1.0;	
	public static final double eMin = (rMars - rEarth) / (rMars + rEarth);

	//http://www.nakedeyeplanets.com/mars-oppositions.htm
	public static final LocalDateTime startDt = LocalDateTime.of(2016, 5, 22, 0, 0); //reference start date, Earth-Mars opposition, http://cseligman.com/text/planets/marsoppositions.htm	
	
	public static final double escapeVelocityEarth = 11200.0; //m/s
	public static final double escapeVelocityMars = 5100.0; //m/s
	
	private static final Color colorMars = Color.RED;
	private static final Color colorEarth = Color.BLUE;
	private static final Color colorEarth2Mars = Color.WHITE;
	private static final Color colorMars2Earth = Color.GREEN;
	
	private static final Color colorSpaceship = Color.MAGENTA;	
	
	public Mission (Orbit earth, Orbit mars, Orbit earth2Mars, Orbit mars2Earth) {
		this.earth = earth;
		this.mars = mars;
    	this.earth2Mars = earth2Mars;
    	this.mars2Earth = mars2Earth;
	}
	
	//
	//Exercise 2B
	//
	public void drawSun(Graphics g) {
    	g.setColor(Color.YELLOW); 
    	g.fillOval(width/2-10, height/2-10, 20, 20);	
    }
    //
    //
    //
	
	//
	//Exercise 3B
	//	
	public void incrementDay(double dDay) {
		if(day + dDay < 0)
			setDay(0.0);
		else
			setDay(day+dDay);
	}
	//
	//
	//
	
	//
	//Exercise 5A
	//
	//get distance between in Earth and Mars in AU
	public double getEarthMarsDistance() {
		double distance = 0.0;	
		
		Position earthPos = getEarth().getPositionAt(getDay());
		Position marsPos = getMars().getPositionAt(getDay());		
		
		distance = earthPos.getDistanceTo(marsPos);
		
		return distance;
	}
	//
	//
	//
	
	//
	//Exercise 5B
	//
	//get absolute angular separation (relative to the Sun) between Earth and Mars in radians
	public double getEarthMarsAngle() {
		double angle = 0.0;
	
		angle = getEarth().getPositionAt(getDay()).getV() - getMars().getPositionAt(getDay()).getV();
		
		//keep angle between PI and -PI
		if(angle > Math.PI) {
			angle = angle - 2*Math.PI;
		} else if(angle < -Math.PI) {
			angle = angle + 2*Math.PI;
		}

		//get absolute value
		angle = Math.abs(angle);
		
		return angle;
	}
	//
	//
	//	
	
	//get distance between in Earth and spacehip in AU
	public double getEarthSpaceshipDistance() {
		double distance = 0.0;	
		
		Position earthPos = getEarth().getPositionAt(getDay());
		
		double spaceshipDay = day;
    	if(getReturnDay() >= 0 && day >= getReturnDay()) {
    		if(mars2Earth.getDestinationArrivalDay() >= 0 && day >= mars2Earth.getDestinationArrivalDay()) {
    		} else {
	    		spaceshipDay = day - getReturnDay() + mars2Earth.getPeriod()/2.0*365.0;
    		}     		
    	} else if(getDepartureDay() >= 0 && day >= getDepartureDay()) {
    		if(earth2Mars.getDestinationArrivalDay() >= 0 && day >= earth2Mars.getDestinationArrivalDay()) {
    		} else {
	    		spaceshipDay = day - getDepartureDay();
    		}
    	}
		Position spaceshipPos = getSpaceship().getPositionAt(spaceshipDay);		
		
		distance = earthPos.getDistanceTo(spaceshipPos);
		
		return distance;
	}	

	//get distance between in Earth and spacehip in AU
	public double getMarsSpaceshipDistance() {
		double distance = 0.0;	
		
		Position marsPos = getMars().getPositionAt(getDay());
		
		double spaceshipDay = day;
    	if(getReturnDay() >= 0 && day >= getReturnDay()) {
    		if(mars2Earth.getDestinationArrivalDay() >= 0 && day >= mars2Earth.getDestinationArrivalDay()) {
    		} else {
	    		spaceshipDay = day - getReturnDay() + mars2Earth.getPeriod()/2.0*365.0;
    		}     		
    	} else if(getDepartureDay() >= 0 && day >= getDepartureDay()) {
    		if(earth2Mars.getDestinationArrivalDay() >= 0 && day >= earth2Mars.getDestinationArrivalDay()) {
    		} else {
	    		spaceshipDay = day - getDepartureDay();
    		}
    	}
		Position spaceshipPos = getSpaceship().getPositionAt(spaceshipDay);		
		
		distance = marsPos.getDistanceTo(spaceshipPos);
		
		return distance;
	}
	
	//get distance between in Earth and spacehip in AU
	public double getSunSpaceshipDistance() {
		double distance = 0.0;	
		
		Position sunPos = new Position(0, 0, 0, 0);
		
		double spaceshipDay = day;
    	if(getReturnDay() >= 0 && day >= getReturnDay()) {
    		if(mars2Earth.getDestinationArrivalDay() >= 0 && day >= mars2Earth.getDestinationArrivalDay()) {
    		} else {
	    		spaceshipDay = day - getReturnDay() + mars2Earth.getPeriod()/2.0*365.0;
    		}     		
    	} else if(getDepartureDay() >= 0 && day >= getDepartureDay()) {
    		if(earth2Mars.getDestinationArrivalDay() >= 0 && day >= earth2Mars.getDestinationArrivalDay()) {
    		} else {
	    		spaceshipDay = day - getDepartureDay();
    		}
    	}
		Position spaceshipPos = getSpaceship().getPositionAt(spaceshipDay);		
		
		distance = sunPos.getDistanceTo(spaceshipPos);
		
		return distance;
	}	
	
    public Orbit getEarth() {
    	return earth;
    }

    public Orbit getMars() {
    	return mars;
    } 
    
    public Orbit getMars2Earth() {
    	return mars2Earth;
    }

    public Orbit getEarth2Mars() {
    	return earth2Mars;
    } 	
    
    //orbit of spaceship depends on rendezvous
    public Orbit getSpaceship() {
    	if(getReturnDay() >= 0 && day >= getReturnDay()) {
    		if(mars2Earth.getDestinationArrivalDay() >= 0 && day >= mars2Earth.getDestinationArrivalDay()) {
    			return earth;
    		} else {
    			return mars2Earth;
    		}     		
    	} else if(getDepartureDay() >= 0 && day >= getDepartureDay()) {
    		if(earth2Mars.getDestinationArrivalDay() >= 0 && day >= earth2Mars.getDestinationArrivalDay()) {
    			return mars;
    		} else {
	    		return earth2Mars;
    		}
    	}
    	return earth; //default
    }
		
    public void setShowEarth2Mars(boolean show) {
    	showEarth2Mars = show;
    }

    public void setShowMars2Earth(boolean show) {
    	showMars2Earth = show;
    } 

    public void showEarth2MarsRetrograde(boolean show) {
    	showEarth2MarsRetrograde = show;
    }     
    
	public double getDay() {
		return day;
	}
	
	public void setDay(double day) {
		this.day = day >= 0 ? day : 0.0;
				
		if(departureDay >= 0) {
			earth2Mars.setOmega(Math.toDegrees(earth.getPositionAt(departureDay).getV())); //set omega to the Earth's current true anomaly
			earth2Mars.setDeltaDayDestination(getDepartureDay());
		} else {
			earth2Mars.setOmega(Math.toDegrees(earth.getPositionAt(day).getV())); //set omega to the Earth's current true anomaly
			earth2Mars.setDeltaDayDestination(-1.0);
		}
		
		if(returnDay >= 0) {
			mars2Earth.setOmega(Math.toDegrees(mars.getPositionAt(returnDay).getV())+180.0); //set omega to Mar's current true anomaly			
			mars2Earth.setDeltaDayDestination(getReturnDay() + mars2Earth.getPeriod()/2.0*365.0);
		} else {
			mars2Earth.setOmega(Math.toDegrees(mars.getPositionAt(day).getV())+180.0); //set omega to Mar's current true anomaly		
			mars2Earth.setDeltaDayDestination(-1.0);
		}	
	}

	public double getDepartureDay() {
		return departureDay;
	}
	
	public void setDepartureDay(double departureDay) {
		this.departureDay = departureDay;
	}	

	public double getReturnDay() {
		return returnDay;
	}
	
	public void setReturnDay(double returnDay) {
		this.returnDay = returnDay;
	}	
    
    public void drawPlanetOrbits(Graphics g) {
        //draw Earth orbit
    	earth.drawOrbit(g, colorEarth, scale, width/2, height/2);
    	
    	//draw Mars orbit
		mars.drawOrbit(g, colorMars, scale, width/2, height/2); 	
    } 
    
    public void drawEarth2MarsOrbit(Graphics g) {
    	//draw return orbit
    	earth2Mars.drawOrbit(g, colorEarth2Mars, scale, width/2, height/2);
    	
    	//calculate intersection of return orbit with Earth orbit
    	double [] psis = earth2Mars.getPsis();
    	        
    	double markerDay = getDepartureDay() >= 0 ? getDepartureDay() : day;
    	if(psis.length > 1) {
    		//intersection #1
    		earth2Mars.drawMarker(earth2Mars.psiToDay(psis[0]), g, colorEarth2Mars, false, true, scale,width/2, height/2); 		
    		mars.drawMarker(markerDay + earth2Mars.psiToDay(psis[0]), g, colorEarth2Mars, true, true, scale, width/2, height/2);	
    		//intersection #2
    		earth2Mars.drawMarker(earth2Mars.psiToDay(psis[1]), g, colorEarth2Mars, false, false, scale,width/2, height/2);    				
    		mars.drawMarker(markerDay + earth2Mars.psiToDay(psis[1]), g, colorEarth2Mars, true, false, scale, width/2, height/2);    		
    	} else if(psis.length > 0) {
    		//intersection #1bv
    		earth2Mars.drawMarker(earth2Mars.psiToDay(psis[0]), g, colorEarth2Mars, false, true, scale,width/2, height/2);		
    		mars.drawMarker(markerDay + earth2Mars.psiToDay(psis[0]), g, colorEarth2Mars, true, true, scale, width/2, height/2);	
    	}
    } 
       
    public void drawMars2EarthOrbit(Graphics g) {
    	//draw return orbit
    	mars2Earth.drawOrbit(g, colorMars2Earth, scale, width/2, height/2);
    	
    	//calculate intersection of return orbit with Earth orbit
    	double [] psis = mars2Earth.getPsis();
    	
    	double markerDay = getReturnDay() >= 0 ? getReturnDay() : day;
    	if(psis.length > 1) {
    		//intersection #1
    		mars2Earth.drawMarker(mars2Earth.psiToDay(psis[0]), g, colorMars2Earth, false, true, scale,width/2, height/2);
    		earth.drawMarker(markerDay + mars2Earth.psiToDay(psis[0]) - mars2Earth.getPeriod()/2.0*365.0, g, colorMars2Earth, true, true, scale, width/2, height/2);	
    		//intersection #2
    		mars2Earth.drawMarker(mars2Earth.psiToDay(psis[1]), g, colorMars2Earth, false, false, scale,width/2, height/2);    		
    		earth.drawMarker(markerDay + mars2Earth.getPeriod()/2.0*365.0 + mars2Earth.psiToDay(psis[1]), g, colorMars2Earth, true, false, scale, width/2, height/2);    		
    	} else if(psis.length > 0) {
    		//intersection #1
    		mars2Earth.drawMarker(mars2Earth.psiToDay(psis[0])+mars2Earth.getPeriod()*365, g, colorMars2Earth, false, true, scale,width/2, height/2);
    		earth.drawMarker(markerDay + mars2Earth.psiToDay(psis[0]) + mars2Earth.getPeriod()/2.0*365.0, g, colorMars2Earth, true, true, scale, width/2, height/2);	
    	}
    }
        
    public void drawPlanets(Graphics g) {
    	earth.drawPosition(day, g, colorEarth, true, scale, width/2, height/2);    	
		mars.drawPosition(day, g, colorMars, true, scale, width/2, height/2);     	
    }
    
    public void drawSpaceship(Graphics g) {
    	if(getReturnDay() >= 0 && day >= getReturnDay()) {
    		if(mars2Earth.getDestinationArrivalDay() >= 0 && day >= mars2Earth.getDestinationArrivalDay()) {
    			earth.drawPosition(day, g, colorSpaceship, true, scale, width/2, height/2, 6, 6);
    		} else {
	    		double spaceshipDay = day - getReturnDay() + mars2Earth.getPeriod()/2.0*365.0;
	    		mars2Earth.drawPosition(spaceshipDay, g, colorSpaceship, true, scale, width/2, height/2, 6, 6);
    		}     		
    	} else if(getDepartureDay() >= 0 && day >= getDepartureDay()) {
    		if(earth2Mars.getDestinationArrivalDay() >= 0 && day >= earth2Mars.getDestinationArrivalDay()) {
    			mars.drawPosition(day, g, colorSpaceship, true, scale, width/2, height/2, 6, 6);
    		} else {
	    		double spaceshipDay = day - getDepartureDay();
				earth2Mars.drawPosition(spaceshipDay, g, colorSpaceship, true, scale, width/2, height/2, 6, 6);
    		}
    	}
    }
    
    public void drawEarth2MarsRetrograde(Graphics g) {
    	
    	Position earth = getEarth().getPositionAt(getDay());
    	Position mars = getMars().getPositionAt(getDay());  

    	//double d = earth.getDistanceTo(mars);
    	//double ang = earth.getAngleTo(mars);
    	
    	//double dang = ang - ang_old;
    	//ang_old = ang;
    	
    	//Position trace = new Position(mars.getX() + 1.0*Math.cos(ang), mars.getY() + 1.0*Math.sin(ang), 0, 0);
    	Position diff = new Position(mars.getX() - earth.getX(), mars.getY() - earth.getY(), 0, 0);
    	
    	//System.out.println("d=" + d + ", ang=" + Math.toDegrees(ang) + ", dang=" + Math.toDegrees(dang));
    	
    	g.setColor(Color.WHITE);
    	g.drawLine((int) Math.round(earth.getX()*scale+width/2), (int) Math.round(earth.getY()*scale+height/2), (int) Math.round(mars.getX()*scale+width/2), (int) Math.round(mars.getY()*scale+height/2));

    	int diameter = 5;
    	double scale = 25.0;
    	double x_offset = width/1.15;
    	double y_offset = height/8.0;
    	g.setColor(Color.RED);
    	g.fillOval((int) Math.round(diff.getX()*scale-diameter/2.0+x_offset), (int) Math.round(diff.getY()*scale-diameter/2.0+y_offset), diameter, diameter);
    	g.setColor(Color.BLUE);
    	g.fillOval((int) Math.round(-diameter/2.0+x_offset), (int) Math.round(-diameter/2.0+y_offset), diameter, diameter);
    }
    
    @Override
	public void paint(Graphics g){
		//draw other stuff
        super.paint(g);

		//get current panel dimensions
		width = (int) this.getSize().getWidth();
		height = (int) this.getSize().getHeight();
		
        drawSun(g);
        drawPlanetOrbits(g);
        
		if(showEarth2Mars) {
			drawEarth2MarsOrbit(g);
		}	
					
		if(showMars2Earth) {
			drawMars2EarthOrbit(g);
		}	

        if(showEarth2MarsRetrograde) {
            drawEarth2MarsRetrograde(g);        	
        }		
		
        drawPlanets(g);   
        drawSpaceship(g);
    }    
}
