package algorithm;

import Data.Map;
import Data.MapListener;
import Data.Position;
import Data.Robot;
import Data.RobotListener;

public class Test implements  MapListener, RobotListener{
	public static Robot r = Robot.getInstance();
	public static Map m = Map.getInstance();
	private int [][] visited = new int[20][15];
	private int currentX = 1;
	private int currentY = 1;
	private float direction = 0;
	public static final float NORTH = 0;
	public static final float EAST = 90;
	public static final float SOUTH = 180;
	public static final float WEST = 270;
	
	int count=1;
	public Test(){
		computeAction();
	}

	@Override
	public void updateMap() {
		computeAction();
	}

	private float checkEnvironementOf[]={NORTH,SOUTH,EAST,WEST};
	protected void computeAction(){
		System.out.println("\n===Action " +count+"===");
		count++;

		currentX = Math.round(r.getPosX());
		currentY= Math.round(r.getPosY());
		direction = r.getDirection();
		System.out.println("Hello i am at ("+r.getPosX()+","+r.getPosX()+") facing at "+direction);
	
		//check if i can explore more here, if yes. rotate~~~
		//check right side
		for(float checkDirection: checkEnvironementOf){
			if(isAnyUndiscovered(checkDirection)){
				//rotate to east
				rotateToDirection(direction,checkDirection);
				break;
			}
		}	
		
		//set visited
		visited[currentX][currentY]=1;
		
		
		int rx = currentY+1;
		int ry1 = currentX-1;
		int ry2 = currentX;
		int ry3 = currentX+1;
		
		
		//1st front row
		int fy = currentY+1;
		int fx1 = currentX-1;
		int fx2 = currentX;
		int fx3 = currentX+1;

		
		if(m.getObstacles()[fy][fx1]==1){
			if(m.getObstacles()[fy][fx2]==1){
				if(m.getObstacles()[fy][fx3]==1){
					//cannot go forward
				}
			}
		}
	}
	
	private boolean isAnyUndiscovered(float inDirection){
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
		if(thereExistUndiscovered){
			System.out.println(inDirection+" direction have undiscovered tiles");
		}
		return thereExistUndiscovered;
	}
	
	private void rotateToDirection(float currentDirection, float inDirection){
		System.out.println("rotate from "+currentDirection+" to "+inDirection);
		float difference = inDirection-currentDirection;
		if(Math.abs(Math.round(difference))==180){
			r.rotate(180);
			return;
		}
		if(difference<180){
			if(Math.abs(difference)>180){
				r.rotate(difference+360);
			}else{
				r.rotate(difference);
			}
		}else{
			r.rotate(-(currentDirection+360-difference));
			
		}
	}
	
	private int[][] getPossibleDirection(int x, int y){
		//int [][]relations = m.getRelations()[y][x];
		return null;
	}
	
	@Override
	public void updateRobot() {
		
	}
	
	@Override
	public void onRobotStop() {
		
	}
	
}
