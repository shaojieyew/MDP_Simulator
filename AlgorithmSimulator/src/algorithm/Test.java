package algorithm;

import Data.Map;
import Data.MapListener;
import Data.Robot;
import Data.RobotListener;

public class Test implements Runnable, MapListener, RobotListener{
	public static Robot r = Robot.getInstance();
	boolean mapUpdated = false;
	@Override
	public void run() {
		mapUpdated = false;
		r.moveForward(10);
	}

	@Override
	public void updateMap() {
		mapUpdated=true;
		try {
			if(r.getPosX()<14){
					Thread.sleep(500);
					r.moveForward(40);
			}else{
				r.rotate(90);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateRobot() {
		
	}
	
	@Override
	public void onRobotStop() {
		
	}
	
}
