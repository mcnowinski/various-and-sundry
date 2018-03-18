import java.awt.*;

public class MyMission extends Mission {

	public MyMission (Orbit earth, Orbit mars, Orbit earth2Mars, Orbit mars2Earth) {
		super(earth, mars, earth2Mars, mars2Earth);
	}	
	
	//
	//Exercise 2B
	//
    @Override
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
	@Override
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
	@Override
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
	@Override
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
	
}
