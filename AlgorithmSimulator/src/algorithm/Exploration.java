package algorithm;

import Data.Map;
import Data.MapListener;
import Data.Robot;
import Data.RobotListener;

public abstract class Exploration implements  MapListener, RobotListener{
	private static long autoTerminate_time=600;
	private static float autoTerminate_explore_rate=1;
	
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
		//wait until robot stop moving
		while(Robot.getInstance().isMoving()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.print("aaa");
		Robot.getInstance().setExploring(false);
		r.removeListener(this);
		m.removeListener(this);
	}

	public static long getAutoTerminate_time() {
		return autoTerminate_time;
	}
	public static void setAutoTerminate_time(long autoTerminate_time) {
		Exploration.autoTerminate_time = autoTerminate_time;
	}
	public static float getAutoTerminate_explore_rate() {
		return autoTerminate_explore_rate;
	}
	public static void setAutoTerminate_explore_rate(float autoTerminate_explore_rate) {
		Exploration.autoTerminate_explore_rate = autoTerminate_explore_rate;
	}
}
