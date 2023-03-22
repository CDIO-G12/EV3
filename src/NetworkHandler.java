/*import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

public class NetworkHandler {


	RegulatedMotor poop = new EV3MediumRegulatedMotor(MotorPort.A);
	
	EV3TouchSensor pooptouch = new EV3TouchSensor(SensorPort.S1);
	
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
	
	
	RegulatedMotor left = new EV3LargeRegulatedMotor(MotorPort.C);
	RegulatedMotor right = new EV3LargeRegulatedMotor(MotorPort.B);
	RegulatedMotor spin = new EV3MediumRegulatedMotor(MotorPort.D);
	
	spin.setSpeed(640);
	poop.setSpeed(360);

	left.synchronizeWith(new RegulatedMotor[] {right});
	left.setSpeed(740);
	right.setSpeed(740);
	
	Dump();
	
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

	left.close();
	right.close();
	spin.close();
	poop.close();
	pooptouch.close();
	
}
*/
