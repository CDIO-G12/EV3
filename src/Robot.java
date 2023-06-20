import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Battery;
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
	private static final Port distanceSesnor = SensorPort.S2;
	private static final Port colorSensor = SensorPort.S3;

	/*
	 * TCP communication variables
	 */
	private static final int port = 9999;
	private static final String ip = "192.168.0.102";

	/*
	 * Physical sizes on the robot in millimeters
	 */
	private static final float wheelDiameter = 43.2f;	//Pulled from LEGO website, 43.2f originale dæk 
	private static final float robotDiagonal = 120f;	// Distance between the wheels, 112.5f originale dæk 

	/*
	 * Public objects
	 */
	// private NetworkCommunication netComm = new NetworkCommunication(ip, port);
	private final MovementController moveCon = new MovementController(leftPort, rightPort, wheelDiameter,
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
	private final char[] validCommands = { 'F', 'f', 'B', 'L', 'R', 'S', 'D', 'G', 'A', 'Z', 'T', 'C'};

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
					
					int i = 0;
					
					while(i < 500) {
						
						if(sen.readColors()) {
							tempSave = "gb";
							Sound.beepSequenceUp();
							break;	
						}
						
						if(i % 100 == 0) {
							pd.openGrapperVar(-50, false);
						}
						
						i++;
						Delay.msDelay(1);
						
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
				
		        LCD.drawString(String.format("Battery %d%%", getBatteryPercent()), 1, 3);
				
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
						
						moveCon.setSpeed(moveCon.defaultSpeed);
						moveCon.setAcc(moveCon.defaultAcc);
						moveCon.moveForward(arg, true);
						break;

					case "f":
						moveCon.setSpeed(360);
						moveCon.setAcc(moveCon.defaultAcc);
						moveCon.moveForwardFine(arg, true);
						break;

					case "B":
						moveCon.setSpeed(360);
						moveCon.setAcc(moveCon.defaultAcc);
						moveCon.moveBackward(arg, true);
						break;
						
					case "C":
						//Calibration case 
						calibrationScript();
						break; 

					case "L":
						moveCon.turnLeft(arg, true);
						break;
						
					case "R":
						moveCon.turnRight(arg, true);
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
							startPickup = true;
							outputQueue.add("pb");
							break;
						
						//Case for semi-difficult ball	
						} else if(arg == 1) {
							
							pickUp((byte) 1);
							startPickup = true;
							outputQueue.add("pb");
							break;
						
						//Case for borderball
						} else if(arg == 2) {

							float distanceMM = sen.readDistanceAve() * 1000;
							
							//If the robot is too close to the border
							if(distanceMM < 180) {
								
								moveCon.setSpeed(20);
								moveCon.moveBackward((byte) (180 - distanceMM), false);
								
							}
							
							// Pickup sets the speed = 150
							pickUp((byte) 0);
							
							moveCon.moveBackward((byte) 20, false);
							while(moveCon.isMoving());

							startPickup = true;
							Delay.msDelay(1000);
							outputQueue.add("pb");
							break;
							
						//Case for cornerBall	
						} else if(arg == 3) {
							
							cornerGrapper();

							startPickup = true;
							Delay.msDelay(1000);
							outputQueue.add("pb");
							break;
							
						//Case for grapping ball in middle 	
						} else if (arg == 4) {
							
							middleXoutPuller();
							outputQueue.add("pb");
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
						try {
						String poll = outputQueue.poll();
						LCD.drawString(poll, 0, 5);
						output.writeBytes(poll);
						output.flush();
						continue;
						} catch (NullPointerException e) {
							LCD.drawString(e.toString(), 0, 5);
						}
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
	
	//Our pickup sequence, whenever robot goes for pickup
	private void pickUp(byte distance) {
		
		pd.downGrapper();
		moveCon.setSpeed(150);
		moveCon.moveForwardFine((distance), true);
		pd.closeGrapper();
		moveCon.stop();
		while(moveCon.isMoving());
		moveCon.resetSpeed();
		
	}
	
	public void middleXoutPuller() {
		
		moveCon.setSpeed(150);
		moveCon.moveForwardFine((byte) 15, false);
		
		//SkrabeKlo sat i position til at scoope/hive bold ud
		pd.downGrapperVar(-310, false); 
		
		while(pd.upDownGrapperIsMoving());
	
		Delay.msDelay(1000);
		
		//Gerne kører ud med bolden, eller bare få den ud af middle
		moveCon.moveBackward((byte) 150, false);
	
		Delay.msDelay(1000); 
	
		moveCon.moveBackward((byte) 100, false);
		while(moveCon.isMoving());
		
		//Sætter grapper tilbage, så den er klar til næste opgave
		pd.downGrapperVar(310, false);
		
		moveCon.resetSpeed();
		
	}
	
	public void cornerGrapper() {

		pd.closeGrapper();
		
		//Åbner tilpas nok til at kunne fange bold i hjørnet
		pd.openGrapperVar(-270, false);
		
		pd.downGrapper();

		moveCon.setSpeed(150);
		//Kører en smule frem for at grib fat om bolden
		moveCon.moveForwardFine((byte) 100, false);
		while(moveCon.isMoving());
		
		Delay.msDelay(100);
		
		//Lukker så bolden forbliver forrest i kloen
		pd.openGrapperVar(200, false);

		//Kører tilbage, så kloen ikke længere kan sidde fast i banderne, hvis det nu skulle ske
		moveCon.moveBackward((byte) 30, true);
		
		pd.openGrapperVar(20, false);
		moveCon.resetSpeed();
		
		
	}
	
	//testScript, en del af tjeklisten
	public void calibrationScript() {
		
		pd.downGrapper();
		pd.closeGrapper();
		moveCon.stop();
		pd.upGrapper();
		pd.openGrapper();
		Delay.msDelay(1000);
		pd.poop((byte) 2);
		
	}
	
	
	//To test the wheels of the robot
	public void drejeTest() {
		
		moveCon.turnRight((byte) 90, false);
		
		while(moveCon.isMoving());

		moveCon.turnLeft((byte) 90, false);
		
		while(moveCon.isMoving());
	}
	
	//Makes the robot drive forward with 2 meters, and the max amount of speed 
	public void RAMBO() {

		moveCon.setSpeed(720);
		moveCon.moveForward((byte) 200, false);
		
		while(moveCon.isMoving());
	}
	
	 //Gets the battery in percentage on the EV3 brick
	 public static int getBatteryPercent() {
		 return (int) (Battery.getVoltage()/10*100);
		 
	 }
	
}
