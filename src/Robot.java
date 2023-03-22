import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;

public class Robot {

	private static final Port leftPort = MotorPort.C;
	private static final Port rightPort = MotorPort.B;
	private static final Port colorSensor = SensorPort.S3;
	
	private static final int port = 9999;
	private static final String ip = "192.168.0.102";
	private static final float wheelDiameter = 0;
	private static final float robotDiagonal = 0;
	
	private NetworkCommunication netComm = new NetworkCommunication(ip, port);
	
	private MovementController moveCon = new MovementController(leftPort, rightPort, wheelDiameter, robotDiagonal);
	private PeripheralDevices pd = new PeripheralDevices(colorSensor);
	
	public Robot() {
		
		
		
	}


	public void run() {
	
		while(true) { 
		pd.readColors();
		
		}
	}

	
	
}
