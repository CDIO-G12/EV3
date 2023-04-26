import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
	private static final Port colorSensor = SensorPort.S3;
	private static final Port UpDownSensor = SensorPort.S4;
	
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
	//private NetworkCommunication netComm = new NetworkCommunication(ip, port);
	private MovementController moveCon = new MovementController(leftPort, rightPort, wheelDiameter, robotDiagonal);
	private PeripheralDevices pd = new PeripheralDevices(openCloseGrapper, upDownGrapper, UpDownSensor);
	private Sensors sen = new Sensors(colorSensor);
	
	/*
	 * Variables used for receiving and queuing movement commands
	 */
	private boolean newCommand = false;
	private boolean stop = false;
	private Queue<String> commandQueue = new LinkedList<>();
	private Queue<String> outputQueue = new LinkedList<>();
	private char[] validCommands = {'F', 'f', 'B', 'L', 'R', 'S', 'D', 'G', 'A', 'Z'};
	
	public void run() throws UnknownHostException, IOException {
		
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
				
				boolean isMoving = false;
				
				while(!stop) {
					if(newCommand) {
						
						currentCommand = commandQueue.poll();
						commands = currentCommand.split(" ");
						arg = Byte.valueOf(commands[1]);
						
						
						switch(commands[0]) {
							
						case "F":
							moveCon.moveForward(arg);
							break;
						
						case "f":
							moveCon.moveForwardFine(arg);
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
							if(arg == -1) {
								// Skal testes 
								pd.downGrapper();
								moveCon.moveForwardFine((byte) 10);
								while(moveCon.isMoving());
								pd.closeGrapper();
								pd.upGrapper();
							}
							break;
						
						case "G":
							if(arg == 1) {
								pd.closeGrapper();
							} else {
								pd.openGrapper();
							}
							break;
							
						case "A":
							if(arg == 1) {
								pd.upGrapper();
							} else {
								pd.downGrapper();
							}
							break;
							
						case "D":
							if(arg == -1) {
								pd.poop();
							}
							break;
							
						case "Z":
							if(arg == -1) {
								moveCon.emStop();
								pd.stopPeripherals();
								commandQueue.clear();
							}
						}
						
						newCommand = false;
						
					}
					
					if(isMoving != moveCon.isMoving()) {
						isMoving = moveCon.isMoving();
						
						if(isMoving) {
							outputQueue.add("m");
						} else {
							outputQueue.add("fm");
						}
						
					}
				}
			}
		});
		
		//tnetwork.start();
		tHandler.start();
		
		LCD.drawString("Running", 0, 0);

		/*
		 * Create thread that read commands from TCP connection and stores it in the FIFO queue
		 */
		try(Socket socket = new Socket(ip, port)) {
			
			//Reads data from middleman, then decrypts it 
			DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			
			String comArg = "";
			byte argument = 0;
			boolean validCommand = false;
			
			outputQueue.add("rd");
			
			while(!stop) {
				if (input.available() > 0) {
					//Reads bytes from input 
					char command = (char) input.readByte();
					for(int i = 0; i < validCommands.length; i++) {
						if(command != validCommands[i]) {
							validCommand = false;
							break;
						} else {
							validCommand = true;
						}
					}
					
					if(!validCommand) {
						continue;
					}
					
					argument = input.readByte();
					
					//String which holds the command that are being written
					comArg = command + " " + argument;
							
					LCD.drawString("comArg is: " + comArg, 0, 4);
	
					commandQueue.add(comArg);
					newCommand = true;
				}
				
				if(!outputQueue.isEmpty()) {
					output.writeBytes(outputQueue.poll());
					output.flush();
				}
			}
			
		}
		catch(Exception e) {
		LCD.drawString("Socket Error", 0, 4);
		} 
		
	}
}
