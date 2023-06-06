import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.utility.Delay;


public class Sensors {
	
	/*
	 * Private copies of parsed arguments
	 */
	private EV3ColorSensor colorSensor;
	private EV3GyroSensor gyroSensor;
	private double cutoffValue = 0.015;
	
	public Sensors(Port colorSensor) {
	
		this.colorSensor = new EV3ColorSensor(colorSensor);
		
	}
	
	public boolean checkBall() {
		
		if(!readColors()) {
			Delay.msDelay(3000);
			
			if(!readColors()) {
				Sound.buzz();
				return false;
			}
		}
		
		return true;
	}
		
	/*
	 * Reads the different RGB values and returns true if a ball roles past.
	 */
	public boolean readColors() {
		
		//float sample, made for saving the rgb values 
		float[] sample = new float[3];
				
		//getRGBMode gives 3 values betweem 0-255, reads the intensity of red, green og blue light
		colorSensor.getRGBMode().fetchSample(sample, 0);
		
		LCD.clear();
		LCD.drawString("R: " + sample[0] * 100, 0, 2);
		LCD.drawString("G: " + sample[1] * 100, 0, 3);
		LCD.drawString("B: " + sample[2] * 100, 0, 4);
		
		//Returns if all rgb values are over 0,5, which means it should be a white ball
		return (sample[0] >= cutoffValue && sample[1] >= cutoffValue && sample[2] >= cutoffValue);
			
	}
	
	public void readGyro() {
		

		float[] sample = null;
		
		gyroSensor.getAngleMode().fetchSample(sample, 0);
		
		LCD.drawString("gyroAngle: " + sample[0]);
		
		
		
	}
		
}
