import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import lejos.hardware.lcd.LCD;

public class NetworkCommunication {

	private String ip;
	private int port;
	private Socket socket; 
	
	public NetworkCommunication(String ip, int port) {
		
		this.ip = ip;
		this.port = port;
		initNetwork();

	}
	
	public String readCommand() throws UnknownHostException, IOException {

		String comArg = "";
			
		//Reads data from middleman, then decrypts it 
		DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		
		//Reads bytes from input 
		char command = (char) input.readByte();
		byte argument = input.readByte();
				
		//String which holds the command that are being written
		comArg = command + " " + argument;
				
		LCD.drawString("comArg is: " + comArg, 0, 4);
				
		return comArg;
	}
	
	private void initNetwork() {
		
		try(Socket createSocket = new Socket(ip, port)) {
			
			socket = createSocket;
			
		}
		
		catch(Exception e) {
		LCD.drawString("Socket Error", 0, 4);
		} 
		
	}
		
}
