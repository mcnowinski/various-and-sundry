public class Vector {
	private double x; //x mag
	private double y; //y mag
	
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}	
	
	public double getAmplitude() {
		return Math.sqrt(x*x+y*y);
	}
	
	public double getAngle() {
		return Math.atan2(y, x);
	}		
	
}