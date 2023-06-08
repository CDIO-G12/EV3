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
	private static final Port distanceSesnor = SensorPort.S2;
	private static final Port colorSensor = SensorPort.S3;
	private static final Port gyroSensor = SensorPort.S1;

	/*
	 * TCP communication variables
	 */
	private static final int port = 9999;
	private static final String ip = "192.168.0.102";

	/*
	 * Physical sizes on the robot in millimeters
	 */
	private static final float wheelDiameter = 43.2f;	//Pulled from LEGO website
	private static final float robotDiagonal = 112.5f;	// Distance between the wheels

	/*
	 * Public objects
	 */
	// private NetworkCommunication netComm = new NetworkCommunication(ip, port);
	private final MovementController moveCon = new MovementController(leftPort, rightPort, gyroSensor, wheelDiameter,
			robotDiagonal);
	private final PeripheralDevices pd = new PeripheralDevices(openCloseGrapper, upDownGrapper);
	private final Sensors sen = new Sensors(colorSensor, distanceSesnor);

	/*
	 * Variables used for receiving and queuing movement commands
	 */
	private boolean newCommand = false;
	private boolean stop = false;
	private boolean pickupRunning = false;
	private boolean startPickup = false;
	private Queue<String> commandQueue = new LinkedList<>();
	private Queue<String> outputQueue = new LinkedList<>();
	private final char[] validCommands = { 'F', 'f', 'B', 'L', 'R', 'S', 'D', 'G', 'A', 'Z', 'T' };

	public void run() throws UnknownHostException, IOException {


		final Thread tPickup = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					while(!startPickup);
					startPickup = false;
					
					pickupRunning = true;
				
					pd.upGrapper();
	
					// Check for ball twice
					String tempSave = "nb";
					int openAmount = -900;
	
					pd.resetTachoOpenClose();
					
					for(int i = 0; i < 3; i++) {
	
						if(sen.readColors()) {
							tempSave = "gb";
							Sound.beep();
							break;	
						}
						
						pd.openGrapperVar(-50, false);
						
					}
					
					pd.openGrapperVarTo(openAmount, true);
	
					outputQueue.add(tempSave);
					pickupRunning = false;
				
				}
			}
		});


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
					
					if(isMoving) {
						moveCon.adjustAngle();
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
						moveCon.moveBackward(arg, false);
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
						while(pickupRunning);

						if(arg == 0) {
							
							pickUp((byte) 80, true);
							break;
							
						} else if(arg == 1) {
							
							pickUp((byte) 0, true);
							break;
							
						} else if(arg == 2) {

							float distanceMM = sen.readDistanceAve() * 1000;
							
							if(distanceMM < 200) {
								
								moveCon.setSpeed(20);
								moveCon.moveBackward((byte) (180 - distanceMM), false);
								
							}
							
							pickUp((byte) 0, false);
							
							moveCon.moveBackward((byte) 20, false);
							
							pd.upGrapper();
							
							// Check for ball twice
							String tempSave = "nb";
							int openAmount = -900;
			
							pd.resetTachoOpenClose();
							
							for(int i = 0; i < 3; i++) {
			
								if(sen.readColors()) {
									tempSave = "gb";
									Sound.beep();
									break;	
								}
								
								pd.openGrapperVar(-50, false);
								
							}
							
							pd.openGrapperVarTo(openAmount, true);
			
							outputQueue.add(tempSave);
							
							break;
							
						} else if(arg == 3) {
							
							
							break;
							
						} else {
							
							pd.upGrapper();
							break;
							
						}	
					}
					
					newCommand = false;

				}
			}
		});

		// tnetwork.start();
		tHandler.start();
		tPickup.start();

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
	
	private void pickUp(byte distance, boolean runPickupThread) {
		
		pd.downGrapper();
		moveCon.setSpeed(150);
		moveCon.moveForwardFine(distance);
		pd.closeGrapper();
		moveCon.stop();
		while(moveCon.isMoving());
		
		if(runPickupThread) {
			outputQueue.add("pb");
			startPickup = true;
		}
	}
	
	
	public void pickUpSequence() {
		
		
		float distanceMM = sen.readDistanceAve() * 1000;
		
		LCD.drawString("Distance bef: " + distanceMM, 0, 2);
		
		if(distanceMM < 200) {
			
			moveCon.setSpeed(20);
			moveCon.moveBackward((byte) (180 - distanceMM), false);
			
		}
		
		Delay.msDelay(5000);
		
		LCD.drawString("Distance aft" + (sen.readDistanceAve() * 1000), 0, 3);
		
		pickUp((byte) 0, false);
		
		moveCon.moveBackward((byte) 20, false);
		
		pd.upGrapper();
		
		// Check for ball twice
		int openAmount = -900;

		pd.resetTachoOpenClose();
		
		for(int i = 0; i < 3; i++) {

			if(sen.readColors()) {
				Sound.beep();
				break;	
			}
			
			pd.openGrapperVar(-50, false);
			
		}
		
		pd.openGrapperVarTo(openAmount, true);
		
		pd.openGrapper();
		
		Delay.msDelay(2000);
	}
	
	public void test() {
		
		float distanceMM = sen.readDistanceAve() * 1000;
		
		if(distanceMM < 200) {
			
			moveCon.setSpeed(20);
			moveCon.moveBackward((byte) (180 - distanceMM), false);
			
		}
		
		pickUp((byte) 0, false);
		
		moveCon.moveBackward((byte) 20, false);
		
		pd.upGrapper();
		
		// Check for ball twice
		String tempSave = "nb";
		int openAmount = -900;

		pd.resetTachoOpenClose();
		
		for(int i = 0; i < 3; i++) {

			if(sen.readColors()) {
				tempSave = "gb";
				Sound.beep();
				break;	
			}
			
			pd.openGrapperVar(-50, false);
			
		}
		
		pd.openGrapperVarTo(openAmount, true);

		outputQueue.add(tempSave);
		
		Delay.msDelay(2000);
		
	}
	
	public void runTest() {

		moveCon.moveForward((byte) 40);
		
		while(moveCon.isMoving());
		
		
	}

	public void testTurning() {	

		for(int i = 0; i < 8; i++) {
			
			if(i > 3) {
				
				moveCon.turnLeftGyro(45);
				
			} else {
				
				moveCon.turnRightGyro(45);
				
			}
			
			Delay.msDelay(1000);
			
		}


	}
	
	
	public void testCorner() {
		
		//pd.openGrapper();
		moveCon.setSpeed(40);
		moveCon.moveForwardFine((byte) 250);
		
		while(sen.readDistance() > 0.25);
		
		moveCon.stop();

		pd.cornerCalibrate();
		pd.downGrapper();
		
		Delay.msDelay(1000);
		
		pd.closeGrapper();
		
		moveCon.resetSpeed();
		pd.upGrapper();
		
		/*
		if(!sen.checkBall()) {
			Sound.buzz();
		}
		*/
		
		pd.openGrapper();
		
		
		/*
		pd.cornerCalibrate();
		pd.downGrapper();
		moveCon.setSpeed(80);
		moveCon.moveForwardFine((byte) 200);
		pd.closeGrapper();
		
		
		while(sen.readDistance() > 0.19);
		moveCon.stop();
		
		moveCon.moveBackward((byte) 100);
		
		while(moveCon.isMoving());
		moveCon.resetSpeed();
		pd.upGrapper();

		// Check for ball twice
		if(!sen.checkBall()) {
			Sound.buzz();
		}
		
		pd.openGrapper();
		*/
	}
	
	public void gyroTest() {
		
		moveCon.moveForward((byte) 100);
		
		while(moveCon.isMoving()) {
			
			moveCon.adjustAngle();
			
		}
	}
	
	public void distanceTest() {
		
		while(true) {
			
			LCD.drawString("Distance: " + sen.readDistance(), 0, 3);
			Delay.msDelay(200);
			
		}
		
	}
	
}
