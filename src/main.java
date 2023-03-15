import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;

public class main {

	
	private static final Port leftPort = MotorPort.C;
	private static final Port rightPort = MotorPort.B;
	
	public static void main(String[] args) {

		
		MovementController MC = new MovementController(leftPort, rightPort, 30.0f, 197.6f);
		
		
		MC.moveForward(100);
		

	}

}
