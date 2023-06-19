import java.io.IOException;
import java.net.UnknownHostException;

import lejos.utility.Delay;

public class main {

	
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		// Create Robot object
		Robot robot = new Robot();
		robot.run();
		Delay.msDelay(5000);

	}

}
