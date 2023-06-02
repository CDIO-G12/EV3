import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
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
	public boolean readColors(boolean orange) {
		
		//float sample, made for saving the rgb values 
		float[] sample = new float[3];
				
		//getRGBMode gives 3 values betweem 0-255, reads the intensity of red, green og blue light
		colorSensor.getRGBMode().fetchSample(sample, 0);
		
		LCD.clear();
		LCD.drawString("R: " + sample[0] * 100, 0, 2);
		LCD.drawString("G: " + sample[1] * 100, 0, 3);
		LCD.drawString("B: " + sample[2] * 100, 0, 4);
		
		if(orange) {
			return (sample[0] <= 2.4 && sample[1] <= 2.4 && sample[2] <= 10);
		}
		
		//Returns if all rgb values are over 0,5, which means it should be a white ball
		return (sample[0] <= 0.015 && sample[1] <= 0.015 && sample[2] <= 0.015);
			
	}

	@Override
	public void run() {

		while(true) {
			readColors(false);
		}
		
	}	
		
}
