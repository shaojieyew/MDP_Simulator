package RPiInterface;

import Data.Map;
import Data.Position;
import Data.Robot;
import javafx.application.Platform;

public abstract class RobotSensorSimulator implements Runnable {
	private static int sensorInfoFreq= 1;
	boolean stop = false;
	boolean moved = true;
	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(sensorInfoFreq);
				if(!Robot.getInstance().isMoving()){
					if(moved){
						sensorInfoUpdate();
						moved=false;
					}
				}else{
					moved=true;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(stop){
				break;
			}
		}
	}

	public abstract void sensorInfoUpdate();
	public abstract String getSensorType();
	public abstract Position[][] getLineOfSensor(int x, int y, float direction);
	
	public void stop() {
		stop=true;
	}

}
