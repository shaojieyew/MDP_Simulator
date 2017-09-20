package algorithm;

import Data.Map;
import Data.MapListener;
import Data.Robot;
import Data.RobotListener;
import Data.WayPoint;
import RPiInterface.Message;

public abstract class FastestPath {
	protected static Robot r = Robot.getInstance();
	protected static Map m = Map.getInstance();
	protected static WayPoint w = WayPoint.getInstance();
	protected static int startingX = 1;
	protected static int startingY = 1;
	
	public FastestPath(){
	}
	public FastestPath(int startAtX, int startAtY){
		startingX=startAtX;
		startingY=startAtY;
	}

	public Message start(){
		return computeAction();
	}
	
	public abstract Message computeAction();

	public int getStartingX() {
		return startingX;
	}
	public void setStartingX(int startingX) {
		this.startingX = startingX;
	}
	public int getStartingY() {
		return startingY;
	}
	public void setStartingY(int startingY) {
		this.startingY = startingY;
	}

	public abstract String geType();
	
	
}
