

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;

public class MovementController extends Thread{

	/*
	 * Private copies of parsed arguments
	 */
	private float wheelSize;
	private float wheelDistance;
	private RegulatedMotor left;
	private RegulatedMotor right;
	
	/*
	 * Calculated variables
	 */
	private float turnConversion;
	private float distancePrDegree;
	
	// Constructor. Creates motors and setup synchronization between the motors.
	public MovementController(Port left, Port right, float wheelSize, float wheelDistance) {
		
		this.wheelSize = wheelSize;
		this.wheelDistance = wheelDistance;
		
		this.left = new EV3LargeRegulatedMotor(left);
		this.right = new EV3LargeRegulatedMotor(right);
		
		this.left.synchronizeWith(new RegulatedMotor[] {this.right});
		
		setupVariables();
		
	}
	
	// Uses the sizes of the wheels to calculate the degrees turned
	// on the wheels to a degree turned for the whole robot
	private void setupVariables() {

		// Variables for the small wheel
		float wheelRadius = wheelSize/2;
		float wheelCircumference = (float) (wheelRadius * Math.PI * 2);
		
		// Variables for the robot as a whole
		float wheelDistanceRadius = wheelDistance / 2;
		float robotCircumference = (float) (wheelDistanceRadius * Math.PI * 2);
		
		// How far the robot turns when asked to turn 1 degree
		distancePrDegree = wheelCircumference/360;
		
		turnConversion = robotCircumference / wheelCircumference;
		
	}
	
	
	/*
	 * Takes the input argument (in millimeters) and moves that distance forward.
	 */
	public void moveForward(int Distance) {
		
		int degreesToTurn = (int) (distancePrDegree / Distance);
		
		left.startSynchronization();
		left.rotate(degreesToTurn);
		right.rotate(degreesToTurn);
		left.endSynchronization();
		
	}
	
	/*
	 * Takes the input argument (in millimeters) and moves that distance backward.
	 */
	public void moveBackward(int Distance) {
		
		int degreesToTurn = (int) -(distancePrDegree / Distance);
		
		left.startSynchronization();
		left.rotate(degreesToTurn);
		right.rotate(degreesToTurn);
		left.endSynchronization();
		
	}
	
	/*
	 * Takes the input argument (in degrees) and turn right that amount
	 */
	public void turnRight(int degrees) {
		
		int totalDegrees = (int) (degrees * turnConversion);
		
		left.startSynchronization();
		left.rotate(-totalDegrees);
		right.rotate(totalDegrees);
		left.endSynchronization();
		
	}
	
	/*
	 * Takes the input argument (in degrees) and turn left that amount
	 */
	public void turnLeft(int degrees) {
		
		int totalDegrees = (int) (degrees * turnConversion);
		
		left.startSynchronization();
		left.rotate(totalDegrees);
		right.rotate(-totalDegrees);
		left.endSynchronization();
		
	}
	
	/*
	 * Stops the robot
	 */
	public void stopMovement() {
		
		left.startSynchronization();
		left.rotate(0);
		right.rotate(0);
		left.endSynchronization();
		
	}
	
	/*
	 * Returns true or false if the robot is moving
	 */
	public boolean isMoving() {
		
		return (left.isMoving() || right.isMoving());
		
	}
	
	/*
	 * Returns true or false if the motors are stalled
	 */
	public boolean isStalled() {
		
		return (left.isStalled() || right.isStalled());
		
	}
	
	
	
	
	
	
	
}
