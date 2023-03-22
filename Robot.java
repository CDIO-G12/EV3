import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.lcd.LCD;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;

public class Robot {

	private static final Port leftPort = MotorPort.C;
	private static final Port rightPort = MotorPort.B;
	private static final Port harvester = MotorPort.D;
	private static final Port dump = MotorPort.A;
	private static final Port colorSensor = SensorPort.S3;
	
	private static final int port = 9999;
	private static final String ip = "192.168.0.102";
	private static final float wheelDiameter = 0;
	private static final float robotDiagonal = 0;
	
	private NetworkCommunication netComm = new NetworkCommunication(ip, port);
	private MovementController moveCon = new MovementController(leftPort, rightPort, wheelDiameter, robotDiagonal);
	private PeripheralDevices pd = new PeripheralDevices(harvester, dump);
	
	private boolean newCommand = true;
	private boolean stop = false;
	
	private Queue<String> commandQueue = new LinkedList<String>();
	
	public Robot() {
		
		
		
	}


	public void run() throws UnknownHostException, IOException {

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
		
		Thread tHandler = new Thread(new Runnable() {

			@Override
			public void run() {
				
				String[] commands = new String[2];
				String currentCommand = "";
				int arg = 0;
				
				while(newCommand) {
					
					currentCommand = commandQueue.poll();
					commands = currentCommand.split(" ");
					arg = Integer.parseInt(commands[1]);
					
					
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
							pd.dumpBalls(true);
						}
						break;
						
					case "Z":
						moveCon.stopMovement();
						pd.stopHarvest();
						commandQueue.clear();
					}
					
					newCommand = false;
				}
			}
			
		});
		
		
		
		
		tnetwork.start();
		
		
		
		
	}

	
	
}
