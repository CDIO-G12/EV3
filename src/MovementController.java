

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;

public class MovementController {

	private float wheelSize;
	private float wheelDistance;
	private RegulatedMotor left;
	private RegulatedMotor right;
	private RegulatedMotor harvester;
	
	private float wheelRadius;
	private float wheelDistanceRadius;
	private float wheelCircumference;
	private float robotCircumference;
	
	private float turnConversion;
	private float distancePrDegree;
	
	
	
	public MovementController(Port left, Port right, float wheelSize, float wheelDistance) {
		
		this.wheelSize = wheelSize;
		this.wheelDistance = wheelDistance;
		
		this.left = new EV3LargeRegulatedMotor(left);
		this.right = new EV3LargeRegulatedMotor(right);
		
		this.left.synchronizeWith(new RegulatedMotor[] {this.right});
		
		setupVariables();
		
	}
	
	public MovementController(float wheelSize, float wheelDistance, RegulatedMotor left, RegulatedMotor right,
			RegulatedMotor harvester) {
		
		this.wheelSize = wheelSize;
		this.wheelDistance = wheelDistance;
		this.left = left;
		this.right = right;
		this.harvester = harvester;
		
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
		

		// TODO: Currently set, needs to be changed to a calculation. 
		// Means 6.444 turns the robot 1 degree
		// turnConversion = 6.444f;
		turnConversion = robotCircumference / wheelCircumference;
		
		
	}
	
	
	/*
	 * Takes the input argument (in millimeters) and moves that distance.
	 */
	public void moveForward(int Distance) {
		
		int degreesToTurn = (int) (distancePrDegree / Distance);
		
		left.startSynchronization();
		left.rotate(degreesToTurn);
		right.rotate(degreesToTurn);
		left.endSynchronization();
		
	}
	
	
	public void moveBackward(int Distance) {
		
		int degreesToTurn = (int) -(distancePrDegree / Distance);
		
		left.startSynchronization();
		left.rotate(degreesToTurn);
		right.rotate(degreesToTurn);
		left.endSynchronization();
		
	}
	
	public void turnRight(int degrees) {
		
		int totalDegrees = (int) (degrees * turnConversion);
		
		left.startSynchronization();
		left.rotate(-totalDegrees);
		right.rotate(totalDegrees);
		left.endSynchronization();
		
	}
	
	public void turnLeft(int degrees) {
		
		int totalDegrees = (int) (degrees * turnConversion);
		
		left.startSynchronization();
		left.rotate(totalDegrees);
		right.rotate(-totalDegrees);
		left.endSynchronization();
		
	}
	
	public boolean isMoving() {
		
		return (left.isMoving() || right.isMoving());
		
	}
	
	public boolean isStalled() {
		
		return (left.isStalled() || right.isStalled());
		
	}
	
	
	
	
	
	
	
}
