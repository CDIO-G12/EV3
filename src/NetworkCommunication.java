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

public class NetworkCommunication {

	private String ip;
	private int port;
	private Socket socket; 
	
	public NetworkCommunication(String ip, int port) {
		
		this.ip = ip;
		this.port = port;

	}
	
	
	public void NetworkInit() throws UnknownHostException, IOException {
		
		try(Socket createSocket = new Socket(ip, port)) {
			socket = createSocket;

			DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			
			LCD.drawString("Welcome", 0, 4);
			
		
		} catch(Exception e) {
			LCD.drawString("Error Network", 0, 4);
		}
		
	}
	
}
