public class Position {
	
	private double x; //y pos in AU
	private double y; //x pos in AU
	private double v; //true anomaly
	private double t; //time in percent of orbital period (T)
	
	public Position(double x, double y, double v, double t) {
		this.x = x;
		this.y = y;
		this.v = v;
		this.t = t;
	}
	
	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getV() { //get true anomaly
		return v;
	}	

	public double getT() { //get time in percent of orbital period
		return t;
	}
	
	public double getDistanceTo(Position position) {
		return Math.sqrt(Math.pow(position.x-x, 2) + Math.pow(position.y-y,2));
	}
	
	public double getAngleTo(Position position) {
		return Math.atan2(position.y-y, position.x-x);
	}
	
	public void print() {
		System.out.println("*");
		System.out.println("x (AU): " + x);		
		System.out.println("y (AU): " + y);	
		System.out.println("True anomaly, v (deg): " + Math.toDegrees(v));
		System.out.println("Time/T: " + t);
		System.out.println("*");		
	}
}