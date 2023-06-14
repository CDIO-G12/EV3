import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

public class PeripheralDevices {

	/*
	 * Private copies of parsed arguments
	 */
	private RegulatedMotor openCloseGrapper;
	private RegulatedMotor upDownGrapper;
	private EV3TouchSensor downSensor;
	
	private int openCloseDefaultSpeed = 750;
	private int upDownDefaultSpeed = 270;	
	private int openCloseDefaultAcc = 6000;
	private int upDownDefaultAcc = 500;
	

	public PeripheralDevices(Port openCloseGrapper, Port upDownGrapper) {

		this.openCloseGrapper = new EV3LargeRegulatedMotor(openCloseGrapper);
		this.upDownGrapper = new EV3MediumRegulatedMotor(upDownGrapper);

		this.openCloseGrapper.setSpeed(openCloseDefaultSpeed);
		this.upDownGrapper.setAcceleration(upDownDefaultAcc);
		this.upDownGrapper.setSpeed(upDownDefaultSpeed);

	}
	
	public void calibrateMotors() {

		upDownGrapper.backward();
		while (!upDownGrapper.isStalled());
		upDownGrapper.stop();
		Delay.msDelay(500);
		upDownGrapper.rotate(400);

		Delay.msDelay(1000);

		this.openCloseGrapper.setStallThreshold(4, 13);
		openCloseGrapper.forward();
		while (!openCloseGrapper.isStalled());
		openCloseGrapper.stop();

		Delay.msDelay(500);

		openGrapper();

		Delay.msDelay(1000);

		Sound.beepSequenceUp();

	}

	public void closeGrapper() {
		
		int i = 0;

		openCloseGrapper.setStallThreshold(10, 100); 

		openCloseGrapper.forward();
		while (!openCloseGrapper.isStalled() || i <= 1500) {
			if(i == 500) {
			    openCloseGrapper.setStallThreshold(7, 100);
			}

			if(i == 2500) break;
			i++;
			Delay.msDelay(1);
		}
		openCloseGrapper.stop();

		Sound.beepSequenceUp();

	}
	
	public void openGrapperVar(int size, boolean imediateReturn) {
		
		openCloseGrapper.setAcceleration(2000);
		openCloseGrapper.setSpeed(720);
		
		openCloseGrapper.rotate(size, imediateReturn);
		
		resetOpenCloseAcc();
		resetOpenCloseSpeed();
		
	}
	
	public void downGrapperVar(int size, boolean imediateReturn) {
		
		upDownGrapper.setAcceleration(2000);
		upDownGrapper.setSpeed(720);
		
		upDownGrapper.rotate(size, imediateReturn);
		
		resetUpDownAcc();
		resetUpDownSpeed();
		
	}

	public void openGrapper() {

		openCloseGrapper.setAcceleration(2000);
		openCloseGrapper.setSpeed(720);
		openCloseGrapper.setStallThreshold(5, 250); 

		openCloseGrapper.rotate(-900, true);
		
		resetOpenCloseAcc();
		resetOpenCloseSpeed();

	}

	public void upGrapper() {

		// Change speed to match rotation duration
		openCloseGrapper.setSpeed(175); 
		upDownGrapper.setSpeed(360);

		openCloseGrapper.setStallThreshold(10, 250);
		
		openCloseGrapper.rotate(-180, true);
		upDownGrapper.rotate(550);		
		while (upDownGrapper.isMoving());
		
		// Reset speed
		resetOpenCloseSpeed();
		resetUpDownSpeed();
			
	}
	
	public void upGrapperOnly() {

		openCloseGrapper.setSpeed(175); 
		upDownGrapper.setSpeed(360);

		upDownGrapper.rotate(550);		
		while (upDownGrapper.isMoving());
		resetOpenCloseSpeed();
		resetUpDownSpeed();
		
	}
	
	
	public void upGrapperLittle() {

		// Change speed to match rotation duration
		upDownGrapper.setSpeed(360);
		upDownGrapper.rotate(290);		
		while (upDownGrapper.isMoving());
		
		// Reset speed
		resetOpenCloseSpeed();
		resetUpDownSpeed();
			
	}

	public void downGrapper() {
		upDownGrapper.setSpeed(150);
		upDownGrapper.setStallThreshold(80, 100);
		
		upDownGrapper.backward();
		while (!upDownGrapper.isStalled());
		upDownGrapper.stop();
		
		resetUpDownSpeed();

	}
	
	public void downGrapperLittle() {
		upDownGrapper.setSpeed(150);
		upDownGrapper.setStallThreshold(80, 100);
		
		upDownGrapper.rotate(-290);
		while(upDownGrapper.isMoving());
		
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

	public void cornerCalibrate() {

		openCloseGrapper.setStallThreshold(10, 200);
		openCloseGrapper.rotate(-325);
		
		
	}
	
	public void resetTachoOpenClose() {
		
		openCloseGrapper.resetTachoCount();
		
	}
	
	public boolean openCloseGrapperIsMoving() {
		
		return openCloseGrapper.isMoving();
		
	}
	
	public void openGrapperVarTo (int size, boolean imediateReturn) {
		
		openCloseGrapper.setAcceleration(2000);
		openCloseGrapper.setSpeed(720);
		
		openCloseGrapper.rotateTo(size, imediateReturn);
		
		resetOpenCloseAcc();
		resetOpenCloseSpeed();
		
	}

}
