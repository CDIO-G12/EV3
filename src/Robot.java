import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;

public class Robot {

	/*
	 * All ports used
	 */
	private static final Port leftPort = MotorPort.B;
	private static final Port rightPort = MotorPort.C;
	private static final Port openCloseGrapper = MotorPort.D;
	private static final Port upDownGrapper = MotorPort.A;
	private static final Port distanceSesnor = SensorPort.S1;
	private static final Port colorSensor = SensorPort.S2;
	private static final Port dumpSensor = SensorPort.S4;
	
	/*
	 * TCP communication variables
	 */
	private static final int port = 9999;
	private static final String ip = "192.168.0.102";
	
	/*
	 * Physical sizes on the robot in millimeters
	 */
	private static final float wheelDiameter = 43.2f;
	private static final float robotDiagonal = 100f;
	
	/*
	 * Public objects
	 */
	private NetworkCommunication netComm = new NetworkCommunication(ip, port);
	private MovementController moveCon = new MovementController(leftPort, rightPort, wheelDiameter, robotDiagonal);
	private PeripheralDevices pd = new PeripheralDevices(openCloseGrapper, upDownGrapper, dumpSensor);
	private Sensors sen = new Sensors(colorSensor);
	
	/*
	 * Variables used for receiving and queuing movement commands
	 */
	private boolean newCommand = false;
	private boolean stop = false;
	private Queue<String> commandQueue = new LinkedList<>();

	public void run() throws UnknownHostException, IOException {
		
		/*
		 * Create thread that read commands from TCP connection and stores it in the FIFO queue
		 */
		Thread tnetwork = new Thread(new Runnable() {

			@Override
			public void run() {
				String recivedCommand = "";

				try {	
					while(!stop) {
						recivedCommand = netComm.readCommand();
						
						if(!recivedCommand.equals("")) {
							commandQueue.add(recivedCommand);
							newCommand = true;
						}
					}
				} catch (IOException e) {
					LCD.drawString("Network Error", 0, 4);
					e.printStackTrace();
				}
			}
			
		});
		
		/*
		 * Checks if command(s) is present
		 * If it is we call the appropriate function 
		 */
		Thread tHandler = new Thread(new Runnable() {

			@Override
			public void run() {
				
				String[] commands = new String[2];
				String currentCommand = "";
				byte arg = 0;
				
				while(newCommand || !stop || !moveCon.isMoving()) {
					
					currentCommand = commandQueue.poll();
					commands = currentCommand.split(" ");
					arg = Byte.valueOf(commands[1]);
					
					
					switch(commands[0]) {
						
					case "F":
						moveCon.moveForward(arg);
						break;
					
					case "B":
						moveCon.moveBackward(arg);
						break;
					
					case "L":
						moveCon.turnLeft(arg);
						break;
						
					case "R":
						moveCon.turnRight(arg);
						break;
						
					case "S":
						if(arg == 0) {
							pd.stopHarvest();
						} else {
							if(arg == 1) {
								pd.harvest(true);
							} else {
								pd.harvest(false);
							}
						}
						break;
						
					case "D":
						if(arg == 1) {
							pd.dumpBalls();
						}
						break;
						
					case "Z":
						moveCon.stop();
						pd.stopHarvest();
						commandQueue.clear();
					}
					
					newCommand = false;
				}
			}
			
		});
		
		tnetwork.start();
		tHandler.start();
		
	}
}
