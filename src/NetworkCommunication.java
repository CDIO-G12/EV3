import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lejos.hardware.lcd.LCD;

public class NetworkCommunication {

	private String ip;
	private int port;
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	
	public NetworkCommunication(String ip, int port) {
		
		this.ip = ip;
		this.port = port;
		initNetwork();

	}
	
	private void initNetwork() {
		
		try(Socket createSocket = new Socket(ip, port)) {
			
			socket = createSocket;
			
			//Reads data from middleman, then decrypts it 
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			
		}
		catch(Exception e) {
		LCD.drawString("Socket Error", 0, 4);
		} 
		
	}
	
	public String readCommand() throws IOException {

		String comArg = "";
		
		//Reads bytes from input 
		char command = (char) input.readByte();
		byte argument = input.readByte();

		//String which holds the command that are being written
		comArg = command + " " + argument;
				
		LCD.drawString("comArg is: " + comArg, 0, 4);
				
		return comArg;
	}
	
	
		
}
