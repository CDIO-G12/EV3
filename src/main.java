import java.io.IOException;
import java.net.UnknownHostException;

import lejos.hardware.ev3.EV3;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.internal.ev3.EV3Port;
import lejos.utility.Delay;

public class main {

	
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		// Create Robot object
		Robot robot = new Robot();
		robot.run();
		
		

	}

}
