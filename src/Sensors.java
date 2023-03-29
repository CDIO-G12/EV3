import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
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
			
		//float sample, made for saving the rgb values 
		float[] sample = new float[3];
				
		//getRGBMode gives 3 values betweem 0-255, reads the intensity of red, green og blue light
		colorSensor.getRGBMode().fetchSample(sample, 0);
				
		Delay.msDelay(500);
				
		//Returns if all rgb values are over 50 
		return (sample[0] >= 0.5 && sample[1] >= 0.5 && sample[2] >= 0.5);
			
	}

	@Override
	public void run() {

		while(true) {
			readColors();
		}
		
	}	
		
}
