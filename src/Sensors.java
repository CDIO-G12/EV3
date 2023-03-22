import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;


public class Sensors implements Runnable {
	
	private EV3ColorSensor colorSensor;
	
	public Sensors(Port colorSensor) {
	
	this.colorSensor = new EV3ColorSensor(colorSensor);
		
	}
		
	private boolean readColors() {
			
		float[] sample = new float[3];
		colorSensor.getRGBMode().fetchSample(sample, 0);
		
		LCD.drawInt(((int) (sample[0]*100)), 0, 0);
		LCD.drawInt(((int) (sample[1]*100)), 0, 1);
		LCD.drawInt(((int) (sample[2]*100)), 0, 2);
		
		
		Delay.msDelay(500);
		
		return (sample[0] >= 0.5 && sample[1] >= 0.5 && sample[2] >= 0.5);
			
	}

	@Override
	public void run() {

		while(true) {
			readColors();
		}
		
	}	
		
}
