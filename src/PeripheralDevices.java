import javax.management.openmbean.OpenDataException;

import lejos.hardware.Sound;
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
	private RegulatedMotor openCloseGrapper;
	private RegulatedMotor upDownGrapper;
	private EV3TouchSensor downSensor;
	private EV3UltrasonicSensor distanceSensor;
	
	public PeripheralDevices(Port openCloseGrapper, Port upDownGrapper, Port downSensor) {
		
		this.openCloseGrapper = new EV3LargeRegulatedMotor(openCloseGrapper);
		this.upDownGrapper = new EV3MediumRegulatedMotor(upDownGrapper);
		this.downSensor = new EV3TouchSensor(downSensor);
		
		this.openCloseGrapper.setSpeed(150);
		this.openCloseGrapper.setStallThreshold(4, 13); // Skal højst sandsynligt ændres
		this.upDownGrapper.setAcceleration(500);
		this.upDownGrapper.setSpeed(270);
		
	}
	
	public void calibrateMotors() {

		upDownGrapper.backward();
		while(!grapperIsDown());
		upDownGrapper.stop();
		Delay.msDelay(500);
		upDownGrapper.rotate(400);

		Delay.msDelay(1000);
		
		this.openCloseGrapper.setStallThreshold(4, 13);
		openCloseGrapper.forward();
		while(!openCloseGrapper.isStalled());
		openCloseGrapper.stop();
		
		Delay.msDelay(500);

		openGrapper();
		
		Delay.msDelay(1000);

		Sound.beepSequenceUp();
		
		
	}

	public void closeGrapper() {

		openCloseGrapper.setStallThreshold(8, 13);
		
		openCloseGrapper.forward();
		while(!openCloseGrapper.isStalled());
		openCloseGrapper.stop();
		
		Sound.beepSequenceUp();
		
	}
	
	public void openGrapper() {
		
		openCloseGrapper.setStallThreshold(5, 250);
		openCloseGrapper.rotate(-800);
		while(openCloseGrapper.isMoving() && !openCloseGrapper.isStalled());
		openCloseGrapper.stop();
		
	}
	
	public void upGrapper() {
		openCloseGrapper.flt();
		upDownGrapper.rotate(550);
		while(upDownGrapper.isMoving());
		
	}
	
	public void downGrapper() {
		openCloseGrapper.flt();
		upDownGrapper.backward();
		while(!grapperIsDown());
		upDownGrapper.stop();
		
	}

	private boolean grapperIsDown() {
		
		  float[] sample = new float[1];
		  downSensor.getTouchMode().fetchSample(sample, 0);
		  return sample[0] != 0.0f;
		
	}
	
	public void stopPeripherals() {
		
		upDownGrapper.stop();
		openCloseGrapper.stop();
		
	}
	
	public void poop() {

		Sound.beepSequence();
		upDownGrapper.rotate(500);
		Delay.msDelay(1000);
		upDownGrapper.rotate(-500);
		
	}
	
}
