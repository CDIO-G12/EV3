import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;

public class MovementController {

	private float wheelSize;
	private float wheelDistance;
	private RegulatedMotor left;
	private RegulatedMotor right;
	
	private float wheelRadius;
	private float wheelDistanceRadius;

	private float turnConversion;
	private float distancePrDegree;
	
	public int defaultAcc = 500;
	public int defaultSpeed = 720; 
	
	
	public MovementController(Port left, Port right, float wheelSize, float wheelDistance) {
		
		this.wheelSize = wheelSize;
		this.wheelDistance = wheelDistance;
		
		this.left = new EV3LargeRegulatedMotor(left);
		this.right = new EV3LargeRegulatedMotor(right);
		
		this.left.setAcceleration(defaultAcc);
		this.right.setAcceleration(defaultAcc);
		this.right.setSpeed(defaultSpeed);
		this.left.setSpeed(defaultSpeed);
		
		this.left.synchronizeWith(new RegulatedMotor[] {this.right});
		
		setupVariables();
		
	}
	
	public void test() {
		
		left.startSynchronization();
		left.rotate(100);
		right.rotate(100);
		left.endSynchronization();
		
		
	}
	
	private void setupVariables() {

		// Variables for the small wheel
		wheelRadius = wheelSize/2;
		float wheelCircumference = (float) (wheelRadius * Math.PI * 2);
		
		// Variables for the robot as a whole
		wheelDistanceRadius = wheelDistance / 2;
		float robotCircumference = (float) (wheelDistanceRadius * Math.PI * 2);
		
		// How far the robot turns when asked to turn 1 degree
		distancePrDegree = wheelCircumference/360;
		
		turnConversion = robotCircumference / wheelCircumference;
	}
	
	/*
	 * Takes the input argument (in millimeters) and moves that distance.
	 */
	public void moveForwardFine(byte distance, boolean imediateReturn) {
		int dist = (distance & 0xFF);
		int degreesToTurn = (int) (dist / distancePrDegree);
		
		left.startSynchronization();
		left.rotate(degreesToTurn, imediateReturn);
		right.rotate(degreesToTurn, imediateReturn);
		left.endSynchronization();
		
	}
	
	/*
	 * Takes the input argument (in millimeters) and moves that distance.
	 */
	public void moveForward(byte distance, boolean imediateReturn) {
		int dist = (distance & 0xFF);
		int degreesToTurn = (int) (dist / distancePrDegree);
		
		left.startSynchronization();
		left.rotate(degreesToTurn*10, imediateReturn);
		right.rotate(degreesToTurn*10, imediateReturn);
		left.endSynchronization();
		
	}
	/*
	 * Takes the input argument (in millimeters) and moves that distance.
	 */
	public void moveBackward(byte distance, boolean imediateReturn) {
		int dist = (distance & 0xFF);
		int degreesToTurn = (int) (dist / distancePrDegree);
		
		left.startSynchronization();
		left.rotate(-degreesToTurn, imediateReturn);
		right.rotate(-degreesToTurn, imediateReturn);
		left.endSynchronization();
		
	}
	
	/*
	 * Takes input in degrees and turns to the desired side
	 */
	public void turnRight(byte degrees, boolean imediateReturn) {
		int deg = (degrees & 0xFF);
		int totalDegrees = (int) (deg * turnConversion);

		left.startSynchronization();
		left.rotate(totalDegrees, imediateReturn);
		right.rotate(-totalDegrees, imediateReturn);
		left.endSynchronization();
		
	}
	
	/*
	 * Takes input in degrees and turns to the desired side
	 */
	public void turnLeft(byte degrees, boolean imediateReturn) {
		setSpeed(360);
		resetAcc();
		int deg = (degrees & 0xFF);
		int totalDegrees = (int) (deg * turnConversion);
		
		left.startSynchronization();
		left.rotate(-totalDegrees, imediateReturn);
		right.rotate(totalDegrees, imediateReturn);
		left.endSynchronization();
		
	}
	
	/*
	 * Stops motors
	 */
	public void stop() {
		left.startSynchronization();
		left.stop();
		right.stop();
		left.endSynchronization();
	}
	
	//Emergency stop
	public void emStop() {
		left.setAcceleration(10000);
		right.setAcceleration(10000);
		
		left.startSynchronization();
		left.stop();
		right.stop();
		left.endSynchronization();
		
		this.left.setAcceleration(defaultAcc);
		this.right.setAcceleration(defaultAcc);
	}
	
	public void setSpeed(int speed) {
		left.setSpeed(speed);
		right.setSpeed(speed);
	}
	
	public void resetSpeed() {
		left.setSpeed(defaultSpeed);
		right.setSpeed(defaultSpeed);
	}
	
	public void setAcc(int acc) {
		left.setAcceleration(acc);
		right.setAcceleration(acc);
	}
	
	public void resetAcc() {
		left.setAcceleration(defaultSpeed);
		right.setAcceleration(defaultSpeed);
	}
	
	/*
	 * Returns true of the robot is moving
	 */
	public boolean isMoving() {
		
		return (left.isMoving() || right.isMoving());
		
	}
	
	/*
	 * Returns true if either of the motors are stalled
	 */
	public boolean isStalled() {
		
		return (left.isStalled() || right.isStalled());
		
	}
	
	/*
	 * Close motors
	 */
	public void close() {
		this.left.close();
		this.right.close();
	}
	
}
