import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.RegulatedMotor;

public class MovementController {

	private float wheelSize;
	private float wheelDistance;
	private RegulatedMotor left;
	private RegulatedMotor right;
	private EV3GyroSensor gyroSensor;
	
	private float wheelRadius;
	private float wheelDistanceRadius;
	private float wheelCircumference;
	private float robotCircumference;
	
	private float turnConversion;
	private float distancePrDegree;
	
	private int standardAcc = 500;
	private int defaultSpeed = 360;
	
	private boolean useGyro = false;
	
	
	public MovementController(Port left, Port right, Port gyroSensor, float wheelSize, float wheelDistance) {
		
		this.wheelSize = wheelSize;
		this.wheelDistance = wheelDistance;
		
		this.left = new EV3LargeRegulatedMotor(left);
		this.right = new EV3LargeRegulatedMotor(right);
		this.gyroSensor = new EV3GyroSensor(gyroSensor);
		
		this.left.setAcceleration(standardAcc);
		this.right.setAcceleration(standardAcc);
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
		useGyro = false;
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
		resetGyro();
		int dist = (distance & 0xFF);
		int degreesToTurn = (int) (dist / distancePrDegree);
		
		left.startSynchronization();
		left.rotate(degreesToTurn*10);
		right.rotate(degreesToTurn*10);
		left.endSynchronization();
		
	}
	
	public void moveBackward(byte distance, boolean imediateReturn) {
		resetGyro();
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
	public void turnRight(byte degrees) {
		useGyro = false;
		int deg = (degrees & 0xFF);
		int totalDegrees = (int) (deg * turnConversion);

		left.startSynchronization();
		left.rotate(totalDegrees);
		right.rotate(-totalDegrees);
		left.endSynchronization();
		
	}
	
	public void turnLeft(byte degrees) {
		useGyro = false;
		int deg = (degrees & 0xFF);
		int totalDegrees = (int) (deg * turnConversion);
		
		left.startSynchronization();
		left.rotate(-totalDegrees);
		right.rotate(totalDegrees);
		left.endSynchronization();
		
	}
	
	public void turnRightGyro(int degrees) {
		
		resetGyro();
		setSpeed(100);

		left.startSynchronization();
		left.forward();
		right.backward();
		left.endSynchronization();
		
		while(Math.abs(getGyroAngle()) <= ((float) degrees * 0.95));
		
		stop();
		
		
	}
	
	public void turnLeftGyro(int degrees) {

		resetGyro();
		setSpeed(100);

		left.startSynchronization();
		left.backward();
		right.forward();
		left.endSynchronization();
		
		while(Math.abs(getGyroAngle()) <= ((float) degrees * 0.95));
		
		stop();
		
		
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
	
	public void adjustAngle() {
		
		if(!useGyro) return;
		
		// Read gyro and apply differnce to wheel speed
		
		float sample = getGyroAngle();
		float newSpeed = 0;
		
		if(sample == 0) {
			
			resetSpeed();
			
			return;
		}
		
		if(sample < 0) {
			
			newSpeed = defaultSpeed + (sample * 5);
			
			left.setSpeed((int) newSpeed);
			// Left side to turn slower
			
		} else {
			
			newSpeed = defaultSpeed - (sample * 5);
			
			left.setSpeed(defaultSpeed);
			// Right side to turn slower
			
		}
	
		LCD.drawString("newSpeed: " + newSpeed, 0, 3);
		LCD.drawString("Angle: " + sample, 0, 4);
		
	}
	
	public void resetGyro() {
		
		gyroSensor.reset();
		useGyro = true;
		resetSpeed();
		
	}
	
	public float getGyroAngle() {

		float[] sample = new float[1];

		gyroSensor.getAngleMode().fetchSample(sample, 0);

		return sample[0];
		
	}
	
	/*
	 * Close motors
	 */
	public void close() {
		this.left.close();
		this.right.close();
	}
	
}
