import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.event.*;

public class JourneyToMars extends JFrame implements ActionListener, ChangeListener, FocusListener, Runnable {

	public class ChangeDay extends AbstractAction {
		
		private static final long serialVersionUID = 1;

	    double dDay;
	    JourneyToMars j2m;

	    ChangeDay(JourneyToMars j2m, double dDay) {
	    	this.j2m = j2m;
	        this.dDay = dDay;
	    }

	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	//j2m.mission.incrementDay(dDay);
			if(j2m.mission.getDay() + dDay < 0)
				j2m.mission.setDay(0.0);
			else
				j2m.mission.setDay(j2m.mission.getDay() + dDay);	    	
	    	j2m.repaint();
	    }
	}
	
	private static final long serialVersionUID = 1;
	
	private JButton reset;
	private JButton resetTrip;
	private JButton addDay;
	private JButton minusDay;
	private JTextPane dataText;
	private JTextField dayField;
	
	private JTextField departureDayField;
	private JTextField returnDayField;	
	
	private JButton showEarth2Mars;
	private JButton showMars2Earth;
	private JButton showEarth2MarsRetrograde;
	
	protected Mission mission;
	private JPanel toMars;
	private JPanel toEarth;
	private JTextField semimajorAxis_toMars;
	private JSlider semimajorAxisSlider_toMars;
	private JTextField eccentricity_toMars;
	private JTextField semimajorAxis_toEarth;
	private JSlider semimajorAxisSlider_toEarth;
	private JTextField eccentricity_toEarth;

	private static final int sliderIncrement = 500;
	
	public JourneyToMars() {
	}
	
	//
	//Exercise 3
	//
    public void resetDay() {
    	//Exercise 3A      	
    }
    
    public void addDay() {
    	//Exercise 3C  	
    }
    
    public void minusDay() {
    	//Exercise 3D	
    } 
    //
    //
    //
	
	@Override
	public void paint(Graphics g) {
		
		super.paint(g);
		
		String earth2MarsLegs = "";
		double [] psis = mission.getEarth2Mars().getPsis();
		//get transit orbit velocity vector at one of the intersections
		Vector vEarth2Mars = mission.getEarth2Mars().getVelocityVectorAt(mission.getEarth2Mars().psiToDay(psis[0]));
		//get destination orbit velocity
		Position vEarth2MarsX = mission.getEarth2Mars().getPositionAt(mission.getEarth2Mars().psiToDay(psis[0]));
		//System.out.println("Amplitude = " + vEarth2Mars.getAmplitude() + ", Angle = " + Math.toDegrees(vEarth2Mars.getAngle()) + " deg, " + Math.toDegrees(vEarth2MarsX.getV()+mission.getEarth2Mars().getOmega()) + " deg");
		double marsDay = mission.getMars().vToDay(Math.toDegrees(vEarth2MarsX.getV()+mission.getEarth2Mars().getOmega()));
		Vector vMars = mission.getMars().getVelocityVectorAt(marsDay);
		//System.out.println("Amplitude = " + vMars.getAmplitude() + ", Angle = " + Math.toDegrees(vMars.getAngle()) + " deg");
		//Vector vEarth2Mars = mission.getEarth2Mars().getVelocityVectorAt(mission.getEarth2Mars().psiToDay(psis[0]));
		//System.out.println("Amplitude = " + vEarth2Mars.getAmplitude() + ", Angle = " + Math.toDegrees(vEarth2Mars.getAngle()) + " deg");
		double dvArrival = Math.sqrt(Math.pow(vEarth2Mars.getX()-vMars.getX(),2) + Math.pow(vEarth2Mars.getY()-vMars.getY(),2));
		//escape velocity + tangential departure
		double dVEarthToMars = Mission.escapeVelocityEarth + Math.abs(mission.getEarth2Mars().getVelocity(0.0) - mission.getEarth().getVelocity(0.0)) + dvArrival;
		if(psis.length == 2) {
			earth2MarsLegs = "X1 = " + String.format("%.3f", mission.getEarth2Mars().psiToDay(psis[1])/365.0) + " years, " + String.format("%.1f", mission.getEarth2Mars().psiToDay(psis[1])) + " days\n";
			earth2MarsLegs += "X2 = " + String.format("%.3f", mission.getEarth2Mars().psiToDay(psis[0])/365.0) + " years, " + String.format("%.1f", mission.getEarth2Mars().psiToDay(psis[0])) + " days\n";
		} else if(psis.length == 1) {
			earth2MarsLegs = "X1 = " + String.format("%.3f", mission.getEarth2Mars().psiToDay(psis[0])/365.0) + " years, " + String.format("%.1f", mission.getEarth2Mars().psiToDay(psis[0])) + " days\n";
		}	
		earth2MarsLegs += 	"dV = " + String.format("%.2f", Mission.escapeVelocityEarth/1000.0) + " + " +
				String.format("%.2f", Math.abs((mission.getEarth2Mars().getVelocity(0.0) - mission.getEarth().getVelocity(0.0))/1000.0)) + " + " +
				String.format("%.2f", dvArrival/1000.0) +		
				/*String.format("%.2f", Math.abs((mission.getEarth2Mars().getVelocity(mission.getEarth2Mars().psiToDay(psis[0])) - mission.getMars().getVelocity(mission.getEarth2Mars().psiToDay(psis[0])))/1000.0)) +*/
				" = " + String.format("%.2f", dVEarthToMars/1000.0/* + Math.abs(mission.getEarth2Mars().getVelocity(mission.getEarth2Mars().psiToDay(psis[0])) - mission.getMars().getVelocity(mission.getEarth2Mars().psiToDay(psis[0])))/1000.0*/) +
		 		" km/s\n";

		String mars2EarthLegs = "";
		psis = mission.getMars2Earth().getPsis();
		//get transit orbit velocity vector at one of the intersections
		Vector vMars2Earth = mission.getMars2Earth().getVelocityVectorAt(mission.getMars2Earth().psiToDay(psis[0])/*+mission.getMars2Earth().getPeriod()/2.0*365.0*/);
		//get destination orbit velocity
		Position vMars2EarthX = mission.getMars2Earth().getPositionAt(mission.getMars2Earth().psiToDay(psis[0]));
		//System.out.println("Amplitude = " + vMars2Earth.getAmplitude() + ", Angle = " + Math.toDegrees(vMars2Earth.getAngle()) + " deg");
		double earthDay = mission.getEarth().vToDay(Math.toDegrees(vMars2EarthX.getV()+mission.getMars2Earth().getOmega()));
		//System.out.println("Earth day = " + earthDay);
		Vector vEarth = mission.getEarth().getVelocityVectorAt(earthDay);
		//System.out.println("Amplitude = " + vEarth.getAmplitude() + ", Angle = " + Math.toDegrees(vEarth.getAngle()) + " deg");
		//System.out.println("Amplitude = " + vEarth2Mars.getAmplitude() + ", Angle = " + Math.toDegrees(vEarth2Mars.getAngle()) + " deg");
		dvArrival = Math.sqrt(Math.pow((vMars2Earth.getX()-vEarth.getX()),2) + Math.pow((vMars2Earth.getY()-vEarth.getY()),2));
		//escape velocity + tangential departure
		double dVMarsToEarth = Mission.escapeVelocityMars + Math.abs(mission.getMars2Earth().getVelocity(mission.getMars2Earth().getPeriod()/2.0*365.0) - mission.getMars().getVelocity(0.0)) + dvArrival;
		if(psis.length == 2) {
			mars2EarthLegs = "X1 = " + String.format("%.3f", mission.getMars2Earth().psiToDay(psis[0])/365.0 - mission.getMars2Earth().getPeriod()/2.0) + " years, " +	String.format("%.1f", mission.getMars2Earth().psiToDay(psis[0]) - mission.getMars2Earth().getPeriod()/2.0*365.0) + " days\n";
			mars2EarthLegs += "X2 = " + String.format("%.3f", mission.getMars2Earth().psiToDay(psis[1])/365.0 + mission.getMars2Earth().getPeriod()/2.0) + " years, " + String.format("%.1f", mission.getMars2Earth().psiToDay(psis[1]) + mission.getMars2Earth().getPeriod()/2.0*365.0) + " days\n";
		} else if(psis.length == 1) {
			mars2EarthLegs = "X1 = " + String.format("%.3f", mission.getMars2Earth().psiToDay(psis[0])/365.0 + mission.getMars2Earth().getPeriod()/2.0) + " years, " + String.format("%.1f", mission.getMars2Earth().psiToDay(psis[0]) + mission.getMars2Earth().getPeriod()/2.0*365.0) + " days\n";	
		}
		mars2EarthLegs += 	"dV = " + String.format("%.2f", Mission.escapeVelocityMars/1000.0) + " + " +
				String.format("%.2f", Math.abs((mission.getMars2Earth().getVelocity(mission.getMars2Earth().getPeriod()/2.0*365.0) - mission.getMars().getVelocity(0.0))/1000.0)) + " + " +
				String.format("%.2f", dvArrival/1000.0) +
				/*String.format("%.2f", Math.abs((mission.getMars2Earth().getVelocity(mission.getMars2Earth().psiToDay(psis[0])+mission.getMars2Earth().getPeriod()/2.0*365.0) - mission.getEarth().getVelocity(mission.getMars2Earth().psiToDay(psis[0])))/1000.0)) +*/
				" = " + String.format("%.2f", dVMarsToEarth/1000.0/* + Math.abs(mission.getMars2Earth().getVelocity(mission.getMars2Earth().psiToDay(psis[0])+mission.getMars2Earth().getPeriod()/2.0*365.0)/* - mission.getEarth().getVelocity(mission.getMars2Earth().psiToDay(psis[0])))/1000.0*/) +
		 		" km/s\n";
		
		LocalDateTime dt = Mission.startDt.plusDays(Math.round(mission.getDay()));
		dataText.setText(
				"Day = " + mission.getDay() + " = " + dt.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + "\n" +
				//"dAng = " + String.format("%.2f", Math.toDegrees(mission.getEarthMarsAngle())) + " deg\n" +
				"d (Earth-Mars) = " + String.format("%.3f", mission.getEarthMarsDistance()) + " AU\n" +
				"d (Earth-Spaceship) = " + String.format("%.3f", mission.getEarthSpaceshipDistance()) + " AU\n" +
				"d (Mars-Spaceship) = " + String.format("%.3f", mission.getMarsSpaceshipDistance()) + " AU\n" +
				"d (Sun-Spaceship) = " + String.format("%.3f", mission.getSunSpaceshipDistance()) + " AU\n" + 
				"\nEarth:\n" +
				"a, e = " + String.format("%.3f", mission.getEarth().getSemimajorAxis()) + " AU, " + String.format("%.3f", mission.getEarth().getEccentricity()) + "\n" +	
				"T = " + String.format("%.3f", mission.getEarth().getPeriod()) + " years, " +	String.format("%.1f", mission.getEarth().getPeriod()*365.0) + " days\n" +					
				"Ang = " + String.format("%.1f", Math.toDegrees(mission.getEarth().getPositionAt(mission.getDay()).getV())) + " deg\n" +
				"V = " + String.format("%.2f", mission.getEarth().getVelocity(mission.getDay())/1000.0) + " km/s\n" +				
				"\nMars:\n" +				
				"a, e = " + String.format("%.3f", mission.getMars().getSemimajorAxis()) + " AU, " + String.format("%.3f", mission.getMars().getEccentricity()) + "\n" +
				"T = " + String.format("%.3f", mission.getMars().getPeriod()) + " years, " +	String.format("%.1f", mission.getMars().getPeriod()*365.0) + " days\n" +	
				"Ang = " + String.format("%.1f", Math.toDegrees(mission.getMars().getPositionAt(mission.getDay()).getV())) + " deg\n" +
				"V = " + String.format("%.2f", mission.getMars().getVelocity(mission.getDay())/1000.0) + " km/s\n"	+
				"\nEarth to Mars:\n" +				
				"a, e = " + String.format("%.3f", mission.getEarth2Mars().getSemimajorAxis()) + " AU, " + String.format("%.3f", mission.getEarth2Mars().getEccentricity()) + "\n" +	
				//"T = " + String.format("%.3f", mission.getEarth2Mars().getPeriod()) + " years, " +	String.format("%.1f", mission.getEarth2Mars().getPeriod()*365.0) + " days\n" +
				earth2MarsLegs +
				"Dep/Arr Days = " + (mission.getDepartureDay() >= 0 ? String.format("%.1f", mission.getDepartureDay()) : "?") + "/" + (mission.getEarth2Mars().getDestinationArrivalDay() >= 0 ? String.format("%.1f", mission.getEarth2Mars().getDestinationArrivalDay()) : "?") + "\n" + 
				"\nMars to Earth:\n" +				
				"a, e = " + String.format("%.3f", mission.getMars2Earth().getSemimajorAxis()) + " AU, " + String.format("%.3f", mission.getMars2Earth().getEccentricity()) + "\n" +	
				//"T = " + String.format("%.3f", mission.getMars2Earth().getPeriod()) + " years, " +	String.format("%.1f", mission.getMars2Earth().getPeriod()*365.0) + " days\n" +		
				mars2EarthLegs + 
				"Dep/Arr Days = " + (mission.getReturnDay() >= 0 ? String.format("%.1f", mission.getReturnDay()) : "?") + "/" + (mission.getMars2Earth().getDestinationArrivalDay() >= 0 ? String.format("%.1f", mission.getMars2Earth().getDestinationArrivalDay()) : "?") + "\n"
				);
		
		dayField.setText(String.format("%.1f",  mission.getDay()));
		
		if(mission.getDepartureDay() >= 0) {
			departureDayField.setText(String.format("%.1f",  mission.getDepartureDay()));
		} else {
			departureDayField.setText("");
		}
		
		if(mission.getReturnDay() >= 0) {
			returnDayField.setText(String.format("%.1f",  mission.getReturnDay()));
		} else {
			returnDayField.setText("");
		}
		
		semimajorAxis_toMars.setText(String.format("%.3f",  mission.getEarth2Mars().getSemimajorAxis()));
		semimajorAxis_toEarth.setText(String.format("%.3f",  mission.getMars2Earth().getSemimajorAxis()));	
		eccentricity_toMars.setText(String.format("%.3f",  mission.getEarth2Mars().getEccentricity()));
		eccentricity_toEarth.setText(String.format("%.3f",  mission.getMars2Earth().getEccentricity()));
	}

	//slider bar updates
	@Override	
    public void stateChanged(ChangeEvent event) {
		if(event.getSource() == semimajorAxisSlider_toEarth) {
            if(!semimajorAxisSlider_toEarth.getValueIsAdjusting())
            {
    			int value = semimajorAxisSlider_toEarth.getValue();
    			double a = Mission.aMin_toEarth + (Mission.aMax_toEarth-Mission.aMin_toEarth) / (sliderIncrement) * value;
    			mission.getMars2Earth().setSemimajorAxis(a);				
    			//adjust eccentricity accordingly
    			mission.getMars2Earth().setEccentricity(Mission.rMars/a-1.0);	
            }
		} else if(event.getSource() == semimajorAxisSlider_toMars) {
            if(!semimajorAxisSlider_toMars.getValueIsAdjusting())
            {
    			int value = semimajorAxisSlider_toMars.getValue();
    			double a = Mission.aMin_toMars + (Mission.aMax_toMars-Mission.aMin_toMars) / (sliderIncrement) * value;  			
    			mission.getEarth2Mars().setSemimajorAxis(a);			
    			//adjust eccentricity accordingly
    			mission.getEarth2Mars().setEccentricity(1.0-Mission.rEarth/a);	    			
            }			
		}
		
		mission.requestFocusInWindow();
		
		repaint();		
    }	

	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}	

    @Override
	public void focusGained(FocusEvent event) {
    }
	
    @Override
	public void focusLost(FocusEvent event) {
    	if(event.getSource() == dayField) {
			if(isNumeric(dayField.getText())) {
				double day = Double.parseDouble(dayField.getText());
				mission.setDay(day >= 0 ? day : 0.0);
			} else {
				mission.setDay(0.0);
			}
		} else if(event.getSource() == departureDayField) {
			if(!departureDayField.getText().equals("")) {
				if(isNumeric(departureDayField.getText())) {
					double departureDay = Double.parseDouble(departureDayField.getText());
					mission.setDepartureDay(departureDay >= 0 ? departureDay : 0.0);
				} else {
					mission.setDepartureDay(0.0);
				}				
			} else {
				mission.setDepartureDay(-1.0); //default value		
			}
		} else if(event.getSource() == returnDayField) {
			if(!returnDayField.getText().equals("")) {
				if(isNumeric(returnDayField.getText())) {
					double returnDay = Double.parseDouble(returnDayField.getText());
					mission.setReturnDay(returnDay >= 0 ? returnDay : 0.0);
				} else {
					mission.setReturnDay(0.0);
				}				
			} else {
				mission.setReturnDay(-1.0); //default value		
			}
		}
 
		mission.setDay(mission.getDay()); //refresh transfer orbits accordingly
		mission.requestFocusInWindow(); //put focus on orbit window to enable keyboard controls
		
		repaint();
    }	   
    
	//button clicks, text field updates, etc.
	@Override
	public void actionPerformed(ActionEvent event) {	
		
		if(event.getSource() == reset) {
			resetDay();
		} else if(event.getSource() == addDay) {
			addDay();
		} else if(event.getSource() == minusDay) {
			minusDay();
		} else if(event.getSource() == dayField) {
			if(isNumeric(dayField.getText())) {
				double day = Double.parseDouble(dayField.getText());
				mission.setDay(day >= 0 ? day : 0.0);
			} else {
				mission.setDay(0.0);
			}
		} else if(event.getSource() == departureDayField) {
			if(!departureDayField.getText().equals("")) {
				if(isNumeric(departureDayField.getText())) {
					double departureDay = Double.parseDouble(departureDayField.getText());
					mission.setDepartureDay(departureDay >= 0 ? departureDay : 0.0);
				} else {
					mission.setDepartureDay(0.0);
				}				
			} else {
				mission.setDepartureDay(-1.0); //default value		
			}
		} else if(event.getSource() == returnDayField) {
			if(!returnDayField.getText().equals("")) {
				if(isNumeric(returnDayField.getText())) {
					double returnDay = Double.parseDouble(returnDayField.getText());
					mission.setReturnDay(returnDay >= 0 ? returnDay : 0.0);
				} else {
					mission.setReturnDay(0.0);
				}				
			} else {
				mission.setReturnDay(-1.0); //default value		
			}
		} else if(event.getSource() == resetTrip) {
			departureDayField.setText("");
			returnDayField.setText("");
			mission.setDepartureDay(-1.0);
			mission.setReturnDay(-1.0);
		} else if(event.getSource() == semimajorAxis_toMars) {
			double a_toMars = Double.parseDouble(semimajorAxis_toMars.getText());
			if(a_toMars > Mission.aMax_toMars)
				a_toMars = Mission.aMax_toMars;
			else if(a_toMars < Mission.aMin_toMars)
				a_toMars = Mission.aMin_toMars;
			mission.getEarth2Mars().setSemimajorAxis(a_toMars);				
			//adjust eccentricity accordingly
			mission.getEarth2Mars().setEccentricity(1.0-Mission.rEarth/a_toMars);			
		} else if(event.getSource() == semimajorAxis_toEarth) {
			double a_toEarth = Double.parseDouble(semimajorAxis_toEarth.getText());
			if(a_toEarth > Mission.aMax_toEarth)
				a_toEarth = Mission.aMax_toEarth;
			else if(a_toEarth < Mission.aMin_toEarth)
				a_toEarth = Mission.aMin_toEarth;				
			mission.getMars2Earth().setSemimajorAxis(a_toEarth);			
			//adjust eccentricity accordingly
			mission.getMars2Earth().setEccentricity(Mission.rMars/a_toEarth-1.0);
		} else if(event.getSource() == eccentricity_toMars) {
			double e_toMars = Double.parseDouble(eccentricity_toMars.getText());
			if(e_toMars > Mission.eMax_toMars)
				e_toMars = Mission.eMax_toMars;
			else if(e_toMars < Mission.eMin)
				e_toMars = Mission.eMin;
			mission.getEarth2Mars().setEccentricity(e_toMars);				
			//adjust semimajor accordingly
			mission.getEarth2Mars().setSemimajorAxis(Mission.rEarth/(1-e_toMars));			
		} else if(event.getSource() == eccentricity_toEarth) {
			double e_toEarth = Double.parseDouble(eccentricity_toEarth.getText());
			if(e_toEarth > Mission.eMax_toEarth)
				e_toEarth = Mission.eMax_toEarth;
			else if(e_toEarth < Mission.eMin)
				e_toEarth = Mission.eMin;
			mission.getMars2Earth().setEccentricity(e_toEarth);				
			//adjust semimajor accordingly
			mission.getMars2Earth().setSemimajorAxis(Mission.rMars/(1+e_toEarth));	
		} else if(event.getSource() == showEarth2Mars) {
			if(event.getActionCommand().equals("show")) {
				showEarth2Mars.setActionCommand("hide");
				showEarth2Mars.setText("Hide Departure");
				mission.setShowEarth2Mars(true);
			} else {
				showEarth2Mars.setActionCommand("show");				
				showEarth2Mars.setText("Show Departure");
				mission.setShowEarth2Mars(false);
			}
		} else if(event.getSource() == showMars2Earth) {
			if(event.getActionCommand().equals("show")) {
				showMars2Earth.setActionCommand("hide");
				showMars2Earth.setText("Hide Return");
				mission.setShowMars2Earth(true);
			} else {
				showMars2Earth.setActionCommand("show");				
				showMars2Earth.setText("Show Return");
				mission.setShowMars2Earth(false);
			}
		} else if(event.getSource() == showEarth2MarsRetrograde) {
			if(event.getActionCommand().equals("show")) {
				showEarth2MarsRetrograde.setActionCommand("hide");
				showEarth2MarsRetrograde.setText("Hide Retrograde");
				mission.showEarth2MarsRetrograde(true);
			} else {
				showEarth2MarsRetrograde.setActionCommand("show");				
				showEarth2MarsRetrograde.setText("Show Retrograde");
				mission.showEarth2MarsRetrograde(false);
			}
		}
			
		mission.setDay(mission.getDay()); //refresh transfer orbits accordingly
		mission.requestFocusInWindow(); //put focus on orbit window to enable keyboard controls
		
		repaint();
	}

	//main routine
	public static void main(String [] args) {
		//create an instance of MyJourneyToMars
		JourneyToMars j2m = new JourneyToMars();
		
		//orbits
		Orbit earth = new Orbit("Earth", Mission.rEarth, Mission.eEarth);
		Orbit mars = new Orbit("Mars", Mission.rMars, Mission.eMars);
		Orbit earth2Mars = new Orbit("Earth to Mars", Mission.aMin_toMars, Mission.eMin);
    	earth2Mars.setDestinationOrbit(mars);
    	Orbit mars2Earth = new Orbit("Mars to Earth", Mission.aMax_toEarth, Mission.eMin);
    	mars2Earth.setOmega(180.0); //return trip
    	mars2Earth.setDestinationOrbit(earth);
		
		//launch the app with a new mission
		j2m.launch(new Mission(earth, mars, earth2Mars, mars2Earth));
	}

	//Runnable member function to show window
	@Override
	public void run() {
		try {
			this.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	//event loop for runnable, where the magic happens
	public void launch(Mission mission) {
		//assign mission
		this.mission = mission;
		//initialize window
		initialize();
		//start event queue
		EventQueue.invokeLater(this);
	}

	private void initialize() {
		//mission = new Mission();
		
		//build the GUI
		setResizable(false);
		setTitle("Journey To Mars");
		setBounds(100, 100, 700, 691);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel bottom = new JPanel(new GridLayout(4,1));
		getContentPane().add(bottom, BorderLayout.SOUTH);
		
		JPanel controls = new JPanel();
		controls.setBorder(UIManager.getBorder("MenuBar.border"));
		FlowLayout flowLayout = (FlowLayout) controls.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		bottom.add(controls);
		
		reset = new JButton("Reset Day");
		controls.add(reset);
        reset.addActionListener(this);
		
		JLabel daysLabel = new JLabel();
		daysLabel.setText("Day: ");
		controls.add(daysLabel);
		
		dayField = new JTextField();
		dayField.setColumns(5);
		controls.add(dayField);
		dayField.addActionListener(this);
		dayField.addFocusListener(this);
		
		addDay = new JButton("+");
		controls.add(addDay);		
		addDay.addActionListener(this);
		
		minusDay = new JButton("-");
		controls.add(minusDay);
		minusDay.addActionListener(this);
		
		showEarth2Mars = new JButton("Show Departure");
		controls.add(showEarth2Mars);
		showEarth2Mars.addActionListener(this);
		showEarth2Mars.setActionCommand("show");		

		showMars2Earth = new JButton("Show Return");
		controls.add(showMars2Earth);
		showMars2Earth.addActionListener(this);
		showMars2Earth.setActionCommand("show");	

		showEarth2MarsRetrograde = new JButton("Show Retrograde");
		controls.add(showEarth2MarsRetrograde);
		showEarth2MarsRetrograde.addActionListener(this);
		showEarth2MarsRetrograde.setActionCommand("show");		
		
		toMars = new JPanel();
		toMars.setBorder(UIManager.getBorder("MenuBar.border"));
		bottom.add(toMars);
		toMars.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JLabel toMarsLabel = new JLabel("Earth To Mars:");
		//toMarsLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		toMars.add(toMarsLabel);
		
		JLabel semimajorAxisLabel_toMars = new JLabel("a (AU):");
		toMars.add(semimajorAxisLabel_toMars);
		
		semimajorAxis_toMars = new JTextField();
		semimajorAxis_toMars.setColumns(10);
		toMars.add(semimajorAxis_toMars);
		semimajorAxis_toMars.addActionListener(this);
	
		semimajorAxisSlider_toMars = new JSlider(0, sliderIncrement, 0);
		semimajorAxisSlider_toMars.setPreferredSize(new Dimension(100, 23));
		toMars.add(semimajorAxisSlider_toMars);
		semimajorAxisSlider_toMars.addChangeListener(this);
		
		JLabel eccentricityLabeL_toMars = new JLabel("e:");
		toMars.add(eccentricityLabeL_toMars);
		
		eccentricity_toMars = new JTextField();
		eccentricity_toMars.setColumns(10);
		toMars.add(eccentricity_toMars);
		eccentricity_toMars.addActionListener(this);
		
		toEarth = new JPanel();
		toEarth.setBorder(UIManager.getBorder("MenuBar.border"));
		bottom.add(toEarth);
		toEarth.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JLabel toEarthLabel = new JLabel("Mars to Earth:");
		//toEarthLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		toEarth.add(toEarthLabel);
		
		JLabel semimajorAxisLabel_toEarth = new JLabel("a (AU):");
		toEarth.add(semimajorAxisLabel_toEarth);

		semimajorAxis_toEarth = new JTextField(Double.toString(Mission.aMax_toEarth));
		semimajorAxis_toEarth.setColumns(10);
		toEarth.add(semimajorAxis_toEarth);
		semimajorAxis_toEarth.addActionListener(this);		
		
		semimajorAxisSlider_toEarth = new JSlider(0, sliderIncrement, sliderIncrement);
		semimajorAxisSlider_toEarth.setPreferredSize(new Dimension(100, 23));
		toEarth.add(semimajorAxisSlider_toEarth);
		semimajorAxisSlider_toEarth.addChangeListener(this);		
		
		JLabel eccentricityLabel_toEarth = new JLabel("e:");
		toEarth.add(eccentricityLabel_toEarth);
		
		eccentricity_toEarth = new JTextField();
		eccentricity_toEarth.setColumns(10);
		toEarth.add(eccentricity_toEarth);
		eccentricity_toEarth.addActionListener(this);				

		JPanel playback = new JPanel();
		playback.setBorder(UIManager.getBorder("MenuBar.border"));
		flowLayout = (FlowLayout) controls.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		bottom.add(playback);
		playback.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JLabel departureDayLabel = new JLabel();
		departureDayLabel.setText("Departure Day: ");
		playback.add(departureDayLabel);
		
		departureDayField = new JTextField();
		departureDayField.setColumns(5);
		playback.add(departureDayField);
		departureDayField.addActionListener(this);
		departureDayField.addFocusListener(this);

		JLabel returnDayLabel = new JLabel();
		returnDayLabel.setText("Return Day: ");
		playback.add(returnDayLabel);
		
		returnDayField = new JTextField();
		returnDayField.setColumns(5);
		playback.add(returnDayField);
		returnDayField.addActionListener(this);
		returnDayField.addFocusListener(this);
		
		resetTrip = new JButton("Reset Trip");
		playback.add(resetTrip);
		resetTrip.addActionListener(this);
		
		JPanel data = new JPanel();
		data.setAlignmentX(Component.LEFT_ALIGNMENT);
		data.setPreferredSize(new Dimension(200, 10));
		data.setBackground(Color.BLACK);
		this.getContentPane().add(data, BorderLayout.WEST);
		data.setLayout(new BorderLayout(0, 0));
		
		dataText = new JTextPane();
		dataText.setFocusCycleRoot(false);
		dataText.setFocusTraversalKeysEnabled(false);
		dataText.setFocusable(false);
		dataText.setAlignmentX(Component.LEFT_ALIGNMENT);
		dataText.setForeground(Color.WHITE);
		dataText.setBackground(Color.BLACK);
		dataText.setFont(new Font("MS Sans Serif", Font.PLAIN, 11));
		dataText.setEditable(false);
		data.add(dataText);
		
		mission.setBackground(Color.BLACK);
		mission.setPreferredSize(new Dimension(300, 0));
		this.getContentPane().add(mission, BorderLayout.CENTER);
		
		JPanel panel = (JPanel) this.getContentPane();
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "addDay");
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "minusDay");
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, java.awt.event.InputEvent.CTRL_DOWN_MASK), "addTenDays");
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, java.awt.event.InputEvent.CTRL_DOWN_MASK), "minusTenDays");        
		panel.getActionMap().put("addDay", new ChangeDay(this, 1.0));
		panel.getActionMap().put("minusDay", new ChangeDay(this, -1.0));
		panel.getActionMap().put("addTenDays", new ChangeDay(this, 10.0));
		panel.getActionMap().put("minusTenDays", new ChangeDay(this, -10.0)); 
	}

}
