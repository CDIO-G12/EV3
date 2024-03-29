import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;


public class Sensors {
	
	/*
	 * Private copies of parsed arguments
	 */
	private EV3ColorSensor colorSensor;
	private EV3UltrasonicSensor distanceSensor;
	private double cutoffValue = 0.015;
	
	public Sensors(Port colorSensor, Port distanceSensor) {
	
		this.colorSensor = new EV3ColorSensor(colorSensor);
		this.distanceSensor = new EV3UltrasonicSensor(distanceSensor);
		
	}
	//Gets average of distance
	public float readDistanceAve() {

		float[] samples = new float[5];
		float sum = 0;
		
		for(int i = 0;i < samples.length; i++) {
			
			distanceSensor.getDistanceMode().fetchSample(samples, i);
			
		}
		
		for (int i = 0; i < samples.length; i++) {
            sum += samples[i];
        }
 
        return sum / samples.length;
		
	}
	//Gets distance
	public float readDistance() {
		
		float[] sample = new float[1];
		
		distanceSensor.getDistanceMode().fetchSample(sample, 0);
		
		return sample[0];
		
	}
		
	/*
	 * Reads the different RGB values and returns true if a ball rolls past.
	 */
	public boolean readColors() {
		
		//float sample, made for saving the rgb values 
		float[] sample = new float[3];
				
		//getRGBMode gives 3 values betweem 0-255, reads the intensity of red, green og blue light
		colorSensor.getRGBMode().fetchSample(sample, 0);
		
		
		//Returns if all rgb values are over 0,5, which means it should be a white ball
		return (sample[0] >= cutoffValue && sample[1] >= cutoffValue && sample[2] >= cutoffValue);
			
	}
	
}
