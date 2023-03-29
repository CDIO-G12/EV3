import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;


public class Sensors implements Runnable {
	
	/*
	 * Private copies of parsed arguments
	 */
	private EV3ColorSensor colorSensor;
	
	
	public Sensors(Port colorSensor) {
	
		this.colorSensor = new EV3ColorSensor(colorSensor);
		
	}
		
	/*
	 * Reads the different RGB values and returns true if a ball roles past.
	 */
	private boolean readColors() {
			
		//float sample, bliver lavet til at gemme rgb værdierne
		
		float[] sample = new float[3];
				
		//getRGBMode giver 3 values mellem 0-255, læser intensiteten af red, green og blue light
		colorSensor.getRGBMode().fetchSample(sample, 0);
				
		Delay.msDelay(500);
				
		//Returner hvis alle rgb værdierne er over 50 
		return (sample[0] >= 0.5 && sample[1] >= 0.5 && sample[2] >= 0.5);
			
	}

	@Override
	public void run() {

		while(true) {
			readColors();
		}
		
	}	
		
}
