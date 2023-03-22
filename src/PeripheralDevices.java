import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

public class PeripheralDevices {
	
	/*
	 * Private copies of parsed arguments
	 */
	private RegulatedMotor harvester;
	private RegulatedMotor dumpMotor;
	private EV3TouchSensor dumpSensor;
	private EV3UltrasonicSensor distanceSensor;
	
	
	public PeripheralDevices(Port harvester, Port dump, Port dumpSensor) {
		
		this.harvester = new EV3LargeRegulatedMotor(harvester);
		this.dumpMotor = new EV3MediumRegulatedMotor(dump);
		this.dumpSensor = new EV3TouchSensor(dumpSensor);
		
	}
	
	/*
	 * Turn on harvester. 1 for forward
	 */
	public void harvest(boolean direction) {
		if(direction) {
			harvester.forward();
		} else {
			harvester.backward();
		}
	}
	
	/*
	 * Stop harvester
	 */
	public void stopHarvest() {
		
		harvester.stop();

		
	}
	
	/*
	 * Opens the rear hatch until the dumpTouch sensor is pressed and then closes again.
	 */
	public void dumpBalls() {
	
		dumpMotor.backward();
		
		while(!readDumpSensor()) {
		
		}
		
		dumpMotor.stop();
		
		Delay.msDelay(1000);
		
		dumpMotor.rotate(330);

	}
	
	/*
	 * Checks if the dumpSensor is pressed
	 */
	public boolean readDumpSensor() {
		  float[] sample = new float[1];
		  dumpSensor.getTouchMode().fetchSample(sample, 0);
		  return sample[0] != 0.0f;
		  
	}
	
}
