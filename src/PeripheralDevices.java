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
	
	private int openCloseDefaultSpeed = 150;
	private int upDownDefaultSpeed = 270;	
	private int openCloseDefaultAcc = 6000;
	private int upDownDefaultAcc = 500;
	

	public PeripheralDevices(Port openCloseGrapper, Port upDownGrapper, Port downSensor) {

		this.openCloseGrapper = new EV3LargeRegulatedMotor(openCloseGrapper);
		this.upDownGrapper = new EV3MediumRegulatedMotor(upDownGrapper);
		this.downSensor = new EV3TouchSensor(downSensor);

		this.openCloseGrapper.setSpeed(openCloseDefaultSpeed);
		this.upDownGrapper.setAcceleration(upDownDefaultAcc);
		this.upDownGrapper.setSpeed(upDownDefaultSpeed);

	}

	public void calibrateMotors() {

		upDownGrapper.backward();
		while (!grapperIsDown());
		upDownGrapper.stop();
		Delay.msDelay(500);
		upDownGrapper.rotate(400);

		Delay.msDelay(1000);

		this.openCloseGrapper.setStallThreshold(4, 13);
		openCloseGrapper.forward();
		while (!openCloseGrapper.isStalled())
			;
		openCloseGrapper.stop();

		Delay.msDelay(500);

		openGrapper();

		Delay.msDelay(1000);

		Sound.beepSequenceUp();

	}

	public void closeGrapper() {

		openCloseGrapper.setStallThreshold(5, 25);

		openCloseGrapper.forward();
		while (!openCloseGrapper.isStalled())
			;
		openCloseGrapper.stop();

		Sound.beepSequenceUp();

	}
	
	public void openGrapperLittle() {
		
		openCloseGrapper.setAcceleration(500);
		openCloseGrapper.setSpeed(720);
		
		openCloseGrapper.rotate(-300);
		
		resetOpenCloseAcc();
		resetOpenCloseSpeed();
		
	}

	public void openGrapper() {

		
		openCloseGrapper.setAcceleration(1000);
		openCloseGrapper.setSpeed(720);
		openCloseGrapper.setStallThreshold(5, 250);
		openCloseGrapper.rotate(-1000);
		
		resetOpenCloseAcc();
		resetOpenCloseSpeed();

	}

	public void upGrapper() {

		// Change speed to match rotation duration
		openCloseGrapper.setSpeed(90);
		upDownGrapper.setSpeed(360);
		
		openCloseGrapper.flt();
		Delay.msDelay(10);
		
		openCloseGrapper.rotate(-150, true);
		upDownGrapper.rotate(550);		
		while (upDownGrapper.isMoving());
		
		// Reset speed
		resetOpenCloseSpeed();
		resetUpDownSpeed();
		
		
	}

	public void downGrapper() {
		openCloseGrapper.flt();
		
		upDownGrapper.setSpeed(150);
		upDownGrapper.setStallThreshold(50, 250);
		
		upDownGrapper.backward();
		while (!grapperIsDown() && !upDownGrapper.isStalled());
		upDownGrapper.stop();
		
		upDownGrapper.setStallThreshold(70, 1000);
		upDownGrapper.rotate(-150);
		upDownGrapper.setStallThreshold(50, 1000);
		
		resetUpDownSpeed();

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

	public void poop(byte loop) {
		Sound.beepSequence();

		for (byte i = 0; i < loop; i++) {
			upDownGrapper.rotate(500);
			Delay.msDelay(1000);
			upDownGrapper.rotate(-500);
		}

	}
	
	private void resetUpDownSpeed() {
		
		upDownGrapper.setSpeed(upDownDefaultSpeed);
		
	}
	
	private void resetUpDownAcc() {
		
		upDownGrapper.setAcceleration(upDownDefaultAcc);
		
	}
	
	private void resetOpenCloseSpeed() {
		
		openCloseGrapper.setSpeed(openCloseDefaultSpeed);
		
	}
	
	private void resetOpenCloseAcc() {
		
		upDownGrapper.setAcceleration(upDownDefaultAcc);
		
	}

}
