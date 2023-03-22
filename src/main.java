import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;

public class main {

	
	private static final Port leftPort = MotorPort.C;
	private static final Port rightPort = MotorPort.B;
	
	public static void main(String[] args) {
		
		Robot rB = new Robot();
		
		
		rB.run();

	}

}
