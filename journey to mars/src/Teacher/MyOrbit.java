import java.awt.Color;
import java.awt.Graphics;

public class MyOrbit extends Orbit {

	public MyOrbit(String name, double semiMajorAxis, double eccentricity) {
		super(name, semiMajorAxis, eccentricity);
	}	
	
	//
	//Exercise 4A
	//
	@Override
	public void drawOrbit(Graphics g, Color color, double scale, double xOffset, double yOffset) {	
		int [] x = new int[positions.length];
		int [] y = new int[positions.length];
	
		for(int i=0; i<positions.length; i++) {
			x[i] = (int) Math.round(positions[i].getX()*scale + xOffset);
			y[i] = (int) Math.round(positions[i].getY()*scale + yOffset);
		}
	
		g.setColor(color);
		g.drawPolyline(x, y, positions.length);
	}	
	//
	//
	//
}
