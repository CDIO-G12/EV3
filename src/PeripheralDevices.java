import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

public class PeripheralDevices {
	
	private RegulatedMotor harvester;
	private RegulatedMotor dumpMotor;
	private EV3TouchSensor dumpSensor;
	private EV3ColorSensor colorSensor;
	private EV3UltrasonicSensor distanceSensor;
	
	private boolean activeHarvest; 
	private boolean direction; 
	private boolean activeDump; 
	/*
	public init() {
		
	}
	*/
	
	public void harvest(boolean direction) {
		
		//hastighed skal sættes
		harvester.forward();
		
		
	}
	
	public void stopHarvest(boolean activeHarvest) {
		
		harvester.stop();

		
	}
	
	public void dumpBalls(boolean activeDump) {
	
		dumpMotor.backward();
		
		while(!readDumpSensor()) {
		
		}
		
		dumpMotor.stop();
		
		Delay.msDelay(1000);
		
		dumpMotor.rotate(330);

	}
	
	public boolean readDumpSensor() {
		  float[] sample = new float[1];
		  dumpSensor.getTouchMode().fetchSample(sample, 0);
		  return sample[0] != 0.0f;
		  
	}
	
	
	public void readColors() {
		
		colorSensor.getColorID();
		
		
	}
	
	
	
	
	public void getDistance() {
		
	}
	
	
	
	
	
}
