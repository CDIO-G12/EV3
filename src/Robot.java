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
import lejos.hardware.Battery;

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
	private static final float wheelDiameter = 43.2f;	//Pulled from LEGO website, 43.2f originale dæk, samuel dæk: 36.8f, 
	private static final float robotDiagonal = 112.5f;	// Distance between the wheels, 112.5f originale dæk, samuel dæk:105f 

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
	private final char[] validCommands = { 'F', 'f', 'B', 'L', 'R', 'S', 'D', 'G', 'A', 'Z', 'T', 'C', 'r', 'l'};

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
					
					for(int i = 0; i < 5; i++) {
							
						if(sen.readColors()) {
							tempSave = "gb";
							Sound.beep();
							break;	
						}
						
						pd.openGrapperVar(-50, false);
						Delay.msDelay(100);
						
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
						
						moveCon.moveForward(arg, true);
						break;

					case "f":
						moveCon.moveForwardFine(arg, true);
						break;

					case "B":
						moveCon.moveBackward(arg, true);
						break;
						
					case "C":
						//Calibration case 
						calibrationScript();
						break; 

					case "L":
						moveCon.turnLeft(arg, true);
						break;

					case "l":
						moveCon.turnOnlyLeft(arg, true);
						break;
						
					case "R":
						moveCon.turnRight(arg, true);
						break;
					
					case "r":
						moveCon.turnOnlyRight(arg, true);
						break;

					case "S":
						if (arg == -1) {
							pd.downGrapper();
							moveCon.moveForwardFine((byte) 10, true);
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
						
						//Case for simple ball
						if(arg == 0) {
							
							pickUp((byte) 80);
							outputQueue.add("pb");
							startPickup = true;
							break;
						
						//Case for semi-difficult ball	
						} else if(arg == 1) {
							
							pickUp((byte) 0);
							outputQueue.add("pb");
							startPickup = true;
							break;
						
						//Case for borderball
						} else if(arg == 2) {

							float distanceMM = sen.readDistanceAve() * 1000;
							
							if(distanceMM < 180) {
								
								moveCon.setSpeed(20);
								moveCon.moveBackward((byte) (180 - distanceMM), false);
								
							}
							moveCon.resetSpeed();
							pickUp((byte) 0);
							
							moveCon.moveBackward((byte) 20, false);
							while(moveCon.isMoving());
							
							outputQueue.add("pb");
							startPickup = true;
							break;
							
						//Case for cornerBall	
						} else if(arg == 3) {
							
							cornerGrapper();

							startPickup = true;
							Delay.msDelay(1000);
							outputQueue.add("pb");
							break;
							
						} else if (arg == 4) {
							
							middleXGrapper();
							
							outputQueue.add("pb");
							startPickup = true;
							break;
							
						}
						else {
							
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
	
	private void pickUp(byte distance) {
		
		pd.downGrapper();
		moveCon.setSpeed(150);
		moveCon.moveForwardFine((distance), true);
		pd.closeGrapper();
		moveCon.stop();
		while(moveCon.isMoving());
		moveCon.resetSpeed();
		
	}
	
	public void middleXGrapper() {
		
		//Klo 2 bliver sat lodret
		//pd.downGrapperLittle();  
		
		moveCon.moveForwardFine((byte) 15, false);
		
		
		//Klo 2 sat i position til at scoope/hive bold ud
		pd.downGrapperVar(-310, false); 
		
		while(pd.upDownGrapperIsMoving());
	
		Delay.msDelay(1000); //1000
		
		moveCon.moveBackward((byte) 150, false);
	
		Delay.msDelay(1000); //1000
		pd.upGrapperLittle();
		
		while(moveCon.isMoving());
		
		moveCon.moveBackward((byte) 100, false);
		
	}
	
	//test funktion til os
	public void cornerGrapperTest() {
		
		pd.closeGrapper();
		
		pd.openGrapperVar(-270, false); 
		
		pd.downGrapper();
		
		moveCon.moveForwardFine((byte) 50, false);
		
		Delay.msDelay(500);
		
		pd.openGrapperVar(200, false); 
		
		moveCon.moveBackward((byte) 50, true);
		
		pd.openGrapperVar(20, false);
		
		pd.upGrapper();
		
		pd.openGrapper();
		
	}
	
	public void cornerGrapper() {

		pd.closeGrapper();
		
		//Åbner tilpas nok til at kunne fange bold i hjørnet
		pd.openGrapperVar(-270, false);
		
		pd.downGrapper();

		//Kører en smule frem for at grib fat om bolden
		moveCon.moveForwardFine((byte) 50, false);
		
		Delay.msDelay(500);
		
		//Lukker så bolden forbliver forrest i kloen
		pd.openGrapperVar(200, false);

		//Kører tilbage, så kloen ikke længere kan sidde fast i banderne, hvis det nu skulle ske
		moveCon.moveBackward((byte) 30, true);
		
		pd.openGrapperVar(20, false);
		
		
	}
	
	//testScript, en del af tjeklisten
	public void calibrationScript() {
		
		pd.downGrapper();
		moveCon.setSpeed(150);
		pd.closeGrapper();
		moveCon.stop();
		pd.upGrapper();
		pd.openGrapper();
		moveCon.resetSpeed();
		Delay.msDelay(1000);
		pd.poop((byte) 1);
		
	}
	
	public void testBorders() {
		
		float distanceMM = sen.readDistanceAve() * 1000;
		
		if(distanceMM < 180) {
			
			moveCon.setSpeed(20);
			moveCon.moveBackward((byte) (180 - distanceMM), false);
			
		}
		moveCon.resetSpeed();
		pickUp((byte) 0);
		
		moveCon.moveBackward((byte) 20, false);
		while(moveCon.isMoving());
		
		pd.upGrapper();
		
		pd.openGrapper();
		
	}
	
	public void gyroTest() {
		
		moveCon.moveForward((byte) 100, true);
		
		while(moveCon.isMoving()) {
			
			moveCon.adjustAngle();
			
		}
	}
	
	public void distanceTest() {
		
		while(true) {
			
			LCD.drawString("Distance: " + sen.readDistance() * 100, 0, 3);
			Delay.msDelay(200);
			
		}
		
	}
	
	public void RAMBO() {

		moveCon.setSpeed(720);
		moveCon.moveForward((byte) 200, false);
		
		while(moveCon.isMoving());
	}
	
}
