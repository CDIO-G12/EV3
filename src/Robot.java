import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.utility.Delay;

public class Robot {

	/*
	 * All ports used
	 */
	private static final Port leftPort = MotorPort.B;
	private static final Port rightPort = MotorPort.C;
	private static final Port openCloseGrapper = MotorPort.D;
	private static final Port upDownGrapper = MotorPort.A;
	// Unused sensor
	// private static final Port distanceSesnor = SensorPort.S1;
	private static final Port colorSensor = SensorPort.S3;
	private static final Port upDownSensor = SensorPort.S4;
	//private static final Port gyro = SensorPort.S1;

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
	// private NetworkCommunication netComm = new NetworkCommunication(ip, port);
	private final MovementController moveCon = new MovementController(leftPort, rightPort, wheelDiameter,
			robotDiagonal);
	private final PeripheralDevices pd = new PeripheralDevices(openCloseGrapper, upDownGrapper, upDownSensor);
	private final Sensors sen = new Sensors(colorSensor);

	/*
	 * Variables used for receiving and queuing movement commands
	 */
	private boolean newCommand = false;
	private boolean stop = false;
	private Queue<String> commandQueue = new LinkedList<>();
	private Queue<String> outputQueue = new LinkedList<>();
	private final char[] validCommands = { 'F', 'f', 'B', 'L', 'R', 'S', 'D', 'G', 'A', 'Z', 'T' };

	public void run() throws UnknownHostException, IOException {

		/*
		 * Checks if command(s) is present If it is we call the appropriate function
		 */
		Thread tHandler = new Thread(new Runnable() {

			@Override
			public void run() {

				String[] commands = new String[2];
				String currentCommand = "";
				byte arg = 0;

				boolean isMoving = moveCon.isMoving();
				boolean orange = true;

				while (!stop) {
					if (isMoving != moveCon.isMoving()) {
						isMoving = moveCon.isMoving();

						if (isMoving) {
							outputQueue.add("m");
						} else {
							outputQueue.add("fm");
						}
					}
					
					if (!newCommand) {
						continue;
					}

					currentCommand = commandQueue.poll();
					
					
					if(currentCommand == null) {
						continue;
					}
					commands = currentCommand.split(" ");
					arg = Byte.valueOf(commands[1]);

					switch (commands[0]) {

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
						if (arg == -1) {
							// Skal testes
							pd.downGrapper();
							moveCon.moveForwardFine((byte) 10);
							while (moveCon.isMoving())
								;
							pd.closeGrapper();
							pd.upGrapper();
						}
						break;

					case "G":
						if (arg == 1) {
							pd.closeGrapper();
						} else {
							pd.openGrapper();
						}
						break;

					case "A":
						if (arg == 1) {
							pd.upGrapper();
						} else {
							pd.downGrapper();
						}
						break;

					case "D":
						pd.poop(arg);
						outputQueue.add("fd");
						break;

					case "Z":
						if (arg == -1) {
							moveCon.emStop();
							pd.stopPeripherals();
							commandQueue.clear();
						}
						break;
					case "T":
						if (arg == 0 || arg == 1) {
							// Check if ball is supposed to be orange
							orange = (arg == 1);

							// (All pd calls are blocking)
							// Move arm down, close around ball, move back up.
							pd.downGrapper();
							moveCon.setSpeed(100);
							moveCon.moveForwardFine((byte) 80);
							pd.closeGrapper();
							moveCon.resetSpeed();
							pd.upGrapper();

							Delay.msDelay(200);

							// Check if color is supposed to be orange
							
							/*
							if (sen.readColors(orange)) {
								Sound.beepSequenceUp();
								pd.openGrapper();
								outputQueue.add("gbo");
							} else {
								Sound.beep();
								pd.openGrapper();
								outputQueue.add("gb");
							}
							*/
							
							// set got ball
							pd.openGrapper();
							outputQueue.add("gb");
							
						}
						break;
					}
					
					newCommand = false;

				}
			}
		});

		// tnetwork.start();
		tHandler.start();

		/*
		 * Create thread that read commands from TCP connection and stores it in the
		 * FIFO queue
		 */

		while (true) {

			// Try connecting to MM
			try (Socket socket = new Socket(ip, port)) {
				// Setup input and output stream
				DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

				// Set default values
				String comArg = "";
				byte argument = 0;
				char command = 0;
				boolean validCommand = false;

				// Send ready command to MM
				outputQueue.add("rd");
				LCD.clear();
				LCD.drawString("Running", 0, 0);

				while (!stop) {
					// Send contents of output queue.
					if (!outputQueue.isEmpty()) {
						String poll = outputQueue.poll();
						LCD.drawString(poll, 0, 5);
						output.writeBytes(poll);
						output.flush();
						continue;
					}

					// Check a command has been sent
					if (input == null || input.available() <= 0) {
						continue;
					}

					// Try reading command and argument from TCP
					command = (char) input.readByte();
					argument = input.readByte();

					// Checks if the read command is valid.
					for (int i = 0; i < validCommands.length; i++) {
						if (validCommands[i] == command) {
							validCommand = true;
							break;
						} else {
							validCommand = false;
						}
					}

					// If command is not valid check loop condition.
					if (!validCommand) {
						continue;
					}

					// Concatenate command and argument.
					comArg = command + " " + argument;

					// Write command to LCD display.
					LCD.drawString("comArg is: " + comArg, 0, 4);

					// Set flags and add command to command queue.
					newCommand = true;
					validCommand = false;
					commandQueue.add(comArg);
				}

			} catch (IOException e) {
				moveCon.emStop();
				pd.stopPeripherals();
				commandQueue.clear();

				// Keep trying to reconnect to MM
				LCD.clear();
				LCD.drawString("Socket Error", 0, 2);
				LCD.drawString("Waiting for MM", 0, 3);
				Delay.msDelay(500);
				continue;
			}
		}
	}

	public void test() {

		//pd.openGrapper();
		
		int i = 0;
		while(i < 10) {
		
		for(int j = 0; j < 2; j++) {
			pd.downGrapper();
			
			Delay.msDelay(100);
			
			pd.closeGrapper();
			
			Delay.msDelay(100);
			
			pd.upGrapper();
			
			Delay.msDelay(200);
			
			pd.openGrapper();
			
			i++;
		}
		
		pd.poop((byte) 2);
		
		}
		
	}
	
	public void test2() {

		boolean orange = true;
		
		pd.downGrapper();
		
		pd.closeGrapper();
		
		
		
		pd.upGrapper();

		if(sen.readColors(false)) {
			
			LCD.drawString("No ball", 0, 5);
			Sound.buzz();
			
		}
		
		pd.openGrapper();
		
		pd.poop((byte) 1);


		
		
	}
	
	public void test3() {
		
		boolean orange = true;
		
		while(true) {

			if(sen.readColors(orange)) {
				
				LCD.drawString("Orange", 0, 5);
				
			} else {
				
				LCD.drawString("White", 0, 5);
				
			}
			
			Delay.msDelay(1000);
			
		}
		
		
	}
	
	public void fullDump() {
		
		moveCon.setAcc(3000);

		moveCon.turnLeft((byte) 20);
		Delay.msDelay(250);
		moveCon.turnRight((byte) 20);
		
		moveCon.resetAcc();
		
		pd.poop((byte) 4);
		
	}
	
	public void sequenceTest() {
		
		pd.downGrapper();
		
		moveCon.setSpeed(100);
		moveCon.moveForwardFine((byte) 80);
		moveCon.resetSpeed();
		
		pd.closeGrapper();
		
		pd.upGrapper();
		
		if(sen.readColors(false)) {
			
			LCD.drawString("No ball", 0, 5);
			Sound.buzz();
			
		}
		
		pd.openGrapper();
		
		
	}
	
public void sequenceTestCorner() {
		
		pd.downGrapper();
		
		moveCon.setSpeed(100);
		moveCon.moveForwardFine((byte) 80);
		moveCon.resetSpeed();
		
		pd.closeGrapper();
		
		moveCon.moveBackward((byte) 100);
		
		
		
		pd.upGrapper();
		
		if(sen.readColors(false)) {
			
			LCD.drawString("No ball", 0, 5);
			Sound.buzz();
			
		}
		
		pd.openGrapper();
		
		
	}
	
	public void poop() {
		
		pd.poop((byte) 1);
		
	}
}
