package algorithm;

import Data.Map;
import Data.MapListener;
import Data.Robot;
import Data.RobotListener;

public abstract class Exploration implements  MapListener, RobotListener{
	protected static Robot r = Robot.getInstance();
	protected static Map m = Map.getInstance();
	private int startingX = 1;
	private int startingY = 1;
	private boolean okToTerminate=false;
	public boolean isOkToTerminate() {
		return okToTerminate;
	}
	public void terminate() {
		 okToTerminate = true;
	}
	public static final float NORTH = 0;
	public static final float EAST = 90;
	public static final float SOUTH = 180;
	public static final float WEST = 270;
	
	
	public Exploration(){
		init();
		r.setExploring(true);
		computeAction();
	}
	public Exploration(int startAtX, int startAtY){
		init();
		r.setExploring(true);
		startingX=startAtX;
		startingY=startAtY;
		computeAction();
	}
	@Override
	public void updateMap() {
		computeAction();
	}
	
	public abstract void init();
	public abstract void computeAction();

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
	//remove all listener and end the exploration
	public void destroy(){
		Robot.getInstance().setExploring(false);
		r.removeListener(this);
		m.removeListener(this);
	}
	
}
