import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;

public class NetworkComm {

	private String ip;
	private int port;
	private Socket socket; 

	RegulatedMotor poop = new EV3MediumRegulatedMotor(MotorPort.A);
	
	EV3TouchSensor pooptouch = new EV3TouchSensor(SensorPort.S1);
	
	public NetworkComm(String ip, int port) {
		
		this.ip = ip;
		this.port = port;

	}
	
	public void Dump() {
		
		poop.backward();
		
		while(!isPoopPressed()) {
		
		}
		
		poop.stop();
		
		Delay.msDelay(1000);
		
		poop.rotate(330);
		
	}
	
	public boolean isPoopPressed() {
		  float[] sample = new float[1];
		  pooptouch.getTouchMode().fetchSample(sample, 0);
		  return sample[0] != 0.0f;
		  
	}
	
	public void NetworkInit() throws UnknownHostException, IOException {

		RegulatedMotor left = new EV3LargeRegulatedMotor(MotorPort.C);
		RegulatedMotor right = new EV3LargeRegulatedMotor(MotorPort.B);
		RegulatedMotor spin = new EV3MediumRegulatedMotor(MotorPort.D);
		
		spin.setSpeed(640);
		poop.setSpeed(360);

		left.synchronizeWith(new RegulatedMotor[] {right});
		left.setSpeed(740);
		right.setSpeed(740);
		
		Dump();
		
		try(Socket createSocket = new Socket(ip, port)) {
			socket = createSocket;

			DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			
			LCD.drawString("Welcome", 0, 4);
			
			while(true) {
				
				char command = (char) input.readByte();
				byte argument = input.readByte();

				LCD.drawChar((char) command, 1, 5);
				LCD.drawChar((char) argument, 1, 6);
				
				switch(command) {
				
				case 'L':
					
					left.startSynchronization();
					left.backward();
					right.forward();
					left.endSynchronization();
					
				break;
				
				case 'R':
					
					left.startSynchronization();
					left.forward();
					right.backward();
					left.endSynchronization();
				
				break;
				
				case 'F': 

					left.startSynchronization();
					left.backward();
					right.backward();
					left.endSynchronization();
					
				break;
				
				case 'B': 

					left.startSynchronization();
					left.forward();
					right.forward();
					left.endSynchronization();
				
				break;
				
				case 'S':
					if(argument == 0) {
						spin.stop();
					}
					else {
						spin.setSpeed(argument*2);
						spin.forward();
					}
				
				break;
				
				case 'D':
					Dump();
					
				}
			
			}
		
		} catch(Exception e) {
			LCD.drawString("Error Network", 0, 4);
		}
		
		left.close();
		right.close();
		spin.close();
		poop.close();
		pooptouch.close();
	}
	
}
