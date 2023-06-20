import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

public class PeripheralDevices {

	/*
	 * Private copies of parsed arguments
	 */
	private RegulatedMotor openCloseGrapper;
	private RegulatedMotor upDownGrapper;
	
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

	public void closeGrapper() {
		
		int i = 0;

		openCloseGrapper.setStallThreshold(10, 100); 

		openCloseGrapper.forward();
		while (!openCloseGrapper.isStalled() || i <= 1500) {
			//Timeout
			if(i == 500) {
			    openCloseGrapper.setStallThreshold(7, 100);
			}

			if(i == 2500) break;
			i++;
			Delay.msDelay(1);
		}
		openCloseGrapper.stop();

		//Sound.beepSequenceUp();

	}
	
	//Not a fixed value, can be varied for different type of use
	public void openGrapperVar(int size, boolean imediateReturn) {

		openCloseGrapper.setStallThreshold(10, 250);
		openCloseGrapper.setAcceleration(2000);
		openCloseGrapper.setSpeed(720);
		
		openCloseGrapper.rotate(size, imediateReturn);
		
		resetOpenCloseAcc();
		resetOpenCloseSpeed();
		
	}
	
	//Not a fixed value, can be varied for different type of use
	public void downGrapperVar(int size, boolean imediateReturn) {
		
		upDownGrapper.setStallThreshold(80, 100);
		upDownGrapper.setAcceleration(360);
		upDownGrapper.setSpeed(150);
		
		upDownGrapper.rotate(size, imediateReturn);
		
		resetUpDownAcc();
		resetUpDownSpeed();
		
	}
	
	//openGrapper sequence
	public void openGrapper() {

		openCloseGrapper.setSpeed(720);
		openCloseGrapper.setAcceleration(2000);
		openCloseGrapper.setStallThreshold(5, 250); 

		openCloseGrapper.rotate(-900, true);
		
		resetOpenCloseAcc();
		resetOpenCloseSpeed();

	}
	
	//upGrapper sequence
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
	
	//Only rotate the upGrapper, and not the openCloseGrapper
	public void upGrapperOnly() {
		
		openCloseGrapper.setSpeed(175); 
		upDownGrapper.setSpeed(360);

		upDownGrapper.rotate(30);		
		while (upDownGrapper.isMoving());
		resetOpenCloseSpeed();
		resetUpDownSpeed();
		
	}
	
	public void upGrapperLittle() {

		// Change speed to match rotation duration, but only to the upGrapper
		upDownGrapper.setSpeed(360);
		upDownGrapper.rotate(310);		
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

	public void stopPeripherals() {

		upDownGrapper.stop();
		openCloseGrapper.stop();

	}

	//Making the robot dump 
	public void poop(byte loop) {
		for (byte i = 0; i < loop; i++) {
			upDownGrapper.rotate(500); 
			while(upDownGrapper.isMoving());
			Delay.msDelay(1000);
			upDownGrapper.rotate(-500); 
		}
		//For at sikre at robotten er calibrated ordentligt, efter et dump 
		upDownGrapper.rotate(-10);

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
	
	public boolean upDownGrapperIsMoving() {
		
		return upDownGrapper.isMoving();
		
	}
	
	public void openGrapperVarTo (int size, boolean imediateReturn) {
		
		openCloseGrapper.setAcceleration(2000);
		openCloseGrapper.setSpeed(720);
		
		openCloseGrapper.rotateTo(size, imediateReturn);
		
		resetOpenCloseAcc();
		resetOpenCloseSpeed();
		
	}

}
