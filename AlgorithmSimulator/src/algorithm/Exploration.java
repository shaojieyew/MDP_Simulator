package algorithm;

import Data.Map;
import Data.MapListener;
import Data.Position;
import Data.Robot;
import Data.RobotListener;
import Data.WayPoint;
import RPiInterface.Message;
import RPiInterface.RobotSensorSimulatorFactory;

public abstract class Exploration implements  MapListener, RobotListener{
	private static long autoTerminate_time=270;
	private static float autoTerminate_explore_rate=1;

	
	public static final float NORTH = 0;
	public static final float EAST = 90;
	public static final float SOUTH = 180;
	public static final float WEST = 270;

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
	public Exploration(boolean isTerminate){
		if(isTerminate){
			terminate();
		}
	}
	
	public Exploration(){
	}
	public Exploration(int startAtX, int startAtY){
		startingX=startAtX;
		startingY=startAtY;
	}
	
	public Message start(){
		init();
		if(Robot.getInstance().isExploring()==false){
			r.setExploring(true);
		}
		
		return computeAction();
	}
	
	@Override
	public void updateMap() {
		computeAction();
	}
	
	public abstract void init();
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

	public abstract String geType();
	

	protected boolean canCalibrate(int currentX,int currentY, float inDirection){
		inDirection=inDirection%360;
		if(currentX<=1&&inDirection==WEST){
			return true;
		}
		if(currentX>=13&&inDirection==EAST){
			return true;
		}
		if(currentY<=1&&inDirection==SOUTH){
			return true;
		}
		if(currentY>=18&&inDirection==NORTH){
			return true;
		}
		int [][]obstacles= m.getObstacles();
		int [][]explored= m.getExploredTiles();
		if(inDirection ==NORTH ){
			if(explored[currentY+2][currentX-1]==1 && explored[currentY+2][currentX+1]==1){
				if(obstacles[currentY+2][currentX-1]==1 && obstacles[currentY+2][currentX+1]==1){
					return true;
				}
			}
		}
		if(inDirection ==SOUTH ){
			if(explored[currentY-2][currentX-1]==1 && explored[currentY-2][currentX+1]==1){
				if(obstacles[currentY-2][currentX-1]==1 && obstacles[currentY-2][currentX+1]==1){
					return true;
				}
			}
		}
		if(inDirection ==EAST ){
			if(explored[currentY+1][currentX+2]==1 && explored[currentY-1][currentX+2]==1){
				if(obstacles[currentY+1][currentX+2]==1 && obstacles[currentY-1][currentX+2]==1){
					return true;
				}
			}
		}
		if(inDirection ==WEST ){
			if(explored[currentY+1][currentX-2]==1 && explored[currentY-1][currentX-2]==1){
				if(obstacles[currentY+1][currentX-2]==1 && obstacles[currentY-1][currentX-2]==1){
					return true;
				}
			}
		}
		return false;
	}
	

	protected boolean isAnyUndiscovered(int currentX,int currentY, float inDirection){
		Position[][] lineOfSensors = r.getSensorSimulator().getLineOfSensor(currentX, currentY, inDirection);
		boolean thereExistUndiscovered = false;
		for(Position[] sensors:lineOfSensors){
			if(thereExistUndiscovered)
				break;
			for(Position sensor:sensors){
				if(sensor!=null&&sensor.getPosY()>=0&&(sensor.getPosY())<20&&(sensor.getPosX())>=0&&(sensor.getPosX())<15){
					if(m.getExploredTiles()[sensor.getPosY()][(sensor.getPosX())]==0){
						thereExistUndiscovered = true;
						break;
					}else{
						if(m.getObstacles()[sensor.getPosY()][(sensor.getPosX())]==1){
							break;
						}
					}
				}else{
					break;
				}
			}
		}
		return thereExistUndiscovered;
	}
	
	protected int howManyUndiscovered(int currentX,int currentY, float inDirection){
		int count=0;
		Position[][] lineOfSensors = r.getSensorSimulator().getLineOfSensor(currentX, currentY, inDirection);
		boolean thereExistUndiscovered = false;
		for(Position[] sensors:lineOfSensors){
			if(thereExistUndiscovered)
				break;
			for(Position sensor:sensors){
				if(sensor!=null&&sensor.getPosY()>=0&&(sensor.getPosY())<20&&(sensor.getPosX())>=0&&(sensor.getPosX())<15){
					if(m.getExploredTiles()[sensor.getPosY()][(sensor.getPosX())]==0){
						count++;
					}else{
						if(m.getObstacles()[sensor.getPosY()][(sensor.getPosX())]==1){
							break;
						}
					}
				}else{
					break;
				}
			}
		}
		return count;
	}

	public boolean outofWallhugging(int x, int y, int direction){
		//if left wall nothing, right wall got thing

		int obstacles[][] = m.getObstacles();
		switch(direction){
			case 0:
				if(x==1||obstacles[y+1][x-2]==1||obstacles[y-1][x-2]==1||obstacles[y][x-2]==1){
					return false;
				}
				break;
			case 90:
				if(y==18||obstacles[y+2][x-1]==1||obstacles[y+2][x+1]==1||obstacles[y+2][x]==1){
					return false;
				}
				break;
			case 180:
				if(x==13||obstacles[y+1][x+2]==1||obstacles[y][x+2]==1||obstacles[y-1][x+2]==1){
					return false;
				}
				break;
			case 270:
				if(y==1||obstacles[y-2][x+1]==1||obstacles[y-2][x]==1||obstacles[y-2][x-1]==1){
					return false;
				}
				break;
			}
		return true;
	}
	

	public boolean startPointFound() {
		for(int x =0;x<3;x++){
			for(int y =0;y<3;y++){
				if(m.getExploredTiles()[y][x]!=1){
					return false;
				}
			}
		}
		return true;
	}

	public boolean endPointFound() {
		for(int x =12;x<15;x++){
			for(int y =17;y<20;y++){
				if(m.getExploredTiles()[y][x]!=1){
					return false;
				}
			}
		}
		return true;
	}
	public boolean waypointFound() {
		Position wp = WayPoint.getInstance().getPosition();
		if(wp==null)
			return true;
		for(int x =wp.getPosX()-1;x<=wp.getPosX()-1;x++){
			for(int y =wp.getPosY()-1;y<=wp.getPosY()-1;y++){
				if(m.getExploredTiles()[y][x]!=1){
					return false;
				}
			}
		}
		return true;
	}

}
