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
	private float wheelCircumference;
	private float robotCircumference;
	
	private float turnConversion;
	private float distancePrDegree;
	
	private int standardAcc = 500;
	private int defaultSpeed = 720;
	
	
	public MovementController(Port left, Port right, float wheelSize, float wheelDistance) {
		
		this.wheelSize = wheelSize;
		this.wheelDistance = wheelDistance;
		
		this.left = new EV3LargeRegulatedMotor(left);
		this.right = new EV3LargeRegulatedMotor(right);
		this.left.setAcceleration(standardAcc);
		this.right.setAcceleration(standardAcc);
		
		this.left.synchronizeWith(new RegulatedMotor[] {this.right});
		
		setupVariables();
		
	}
	
	private void setupVariables() {

		// Variables for the small wheel
		wheelRadius = wheelSize/2;
		wheelCircumference = (float) (wheelRadius * Math.PI * 2);
		
		// Variables for the robot as a whole
		wheelDistanceRadius = wheelDistance / 2;
		robotCircumference = (float) (wheelDistanceRadius * Math.PI * 2);
		
		// How far the robot turns when asked to turn 1 degree
		distancePrDegree = wheelCircumference/360;
		

		turnConversion = robotCircumference / wheelCircumference;
		
	}
	
	/*
	 * Takes the input argument (in millimeters) and moves that distance.
	 */
	public void moveForwardFine(byte distance) {
		int dist = (distance & 0xFF);
		int degreesToTurn = (int) (dist / distancePrDegree);
		
		left.startSynchronization();
		left.rotate(degreesToTurn);
		right.rotate(degreesToTurn);
		left.endSynchronization();
		
	}
	
	/*
	 * Takes the input argument (in millimeters) and moves that distance.
	 */
	public void moveForward(byte distance) {
		int dist = (distance & 0xFF);
		int degreesToTurn = (int) (dist / distancePrDegree);
		
		left.startSynchronization();
		left.rotate(degreesToTurn*255);
		right.rotate(degreesToTurn*255);
		left.endSynchronization();
		
	}
	
	public void moveBackward(byte distance) {
		int dist = (distance & 0xFF);
		int degreesToTurn = (int) (dist / distancePrDegree);
		
		left.startSynchronization();
		left.rotate(-degreesToTurn);
		right.rotate(-degreesToTurn);
		left.endSynchronization();
		
	}
	
	/*
	 * Takes input in degrees and turns to the desired side
	 */
	public void turnRight(byte degrees) {
		int deg = (degrees & 0xFF);
		int totalDegrees = (int) (deg * turnConversion);

		
		left.startSynchronization();
		left.rotate(totalDegrees);
		right.rotate(-totalDegrees);
		left.endSynchronization();
		
	}
	
	public void turnLeft(byte degrees) {
		int deg = (degrees & 0xFF);
		int totalDegrees = (int) (deg * turnConversion);
		
		left.startSynchronization();
		left.rotate(-totalDegrees);
		right.rotate(totalDegrees);
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
	
	public void emStop() {
		left.setAcceleration(10000);
		right.setAcceleration(10000);
		
		left.startSynchronization();
		left.stop();
		right.stop();
		left.endSynchronization();
		
		this.left.setAcceleration(standardAcc);
		this.right.setAcceleration(standardAcc);
	}
	
	public void setSpeed(int speed) {
		left.setSpeed(speed);
		right.setSpeed(speed);
	}
	
	public void resetSpeed() {
		left.setSpeed(defaultSpeed);
		right.setSpeed(defaultSpeed);
		
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
