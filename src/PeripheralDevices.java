import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
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
	
	
	public PeripheralDevices(Port harvester, Port dump) {
		
		this.harvester = new EV3LargeRegulatedMotor(harvester);
		this.dumpMotor = new EV3MediumRegulatedMotor(dump);
		
	}
	
	public void harvest(boolean direction) {
		
		//hastighed skal sættes
		harvester.forward();
		
		
	}
	
	public void stopHarvest() {
		
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
