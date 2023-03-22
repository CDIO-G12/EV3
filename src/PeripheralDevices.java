import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

public class PeripheralDevices {
	
	private RegulatedMotor harvester;
	private RegulatedMotor dumpMotor;
	private EV3TouchSensor dumpSensor;
	private EV3UltrasonicSensor distanceSensor;
	private EV3ColorSensor colorSensor;
	
	private boolean activeHarvest; 
	private boolean direction; 
	private boolean activeDump; 
	/*
	public init() {
		
	}
	*/
	
	public PeripheralDevices(Port colorSensor) {
		
		this.colorSensor = new EV3ColorSensor(colorSensor);
		
	}
	
	
	
	
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
	
	
	public boolean readColors() {
		
		float[] sample = new float[3];
		colorSensor.getRGBMode().fetchSample(sample, 0);
		
		LCD.drawInt(((int) (sample[0]*100)), 0, 0);
		LCD.drawInt(((int) (sample[1]*100)), 0, 1);
		LCD.drawInt(((int) (sample[2]*100)), 0, 2);
		
		
		Delay.msDelay(500);
		
		return (sample[0] >= 0.5 && sample[1] >= 0.5 && sample[2] >= 0.5);
		
		}
	
	public void getDistance() {
		
	}
	
	
}
