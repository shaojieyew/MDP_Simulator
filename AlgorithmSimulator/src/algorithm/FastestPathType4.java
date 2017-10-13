package algorithm;

/*follow left wall, with minimum rotation*/

import java.awt.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import Data.Map;
import Data.MapListener;
import Data.Position;
import Data.Robot;
import Data.RobotListener;
import Data.Vertex;
import RPiInterface.Message;

public class FastestPathType4 extends FastestPath {
	public static boolean testTurnedLeft =false;
	public static boolean testTurnedRight =false;
	@Override
	public Message computeAction() {
		// TODO Auto-generated method stub
		//Robot
		//ArrayList<String> movements = getNextWallHugLocation(.getPosX())
		return null;
	}
	

	private ArrayList<String> getNextWallHugLocation( int x, int y,int direction, ArrayList<String> movements){
		if(movements==null){
			movements = new ArrayList<String>();
		}
		int robotsNorth = (int) ((NORTH+direction)%360);
		int robotsEast = (int) ((EAST+direction)%360);
		int robotsWest = (int) ((WEST+direction)%360);
		int nMoveable = isDirectionMoveable(robotsNorth, x, y);
		int wMoveable = isDirectionMoveable(robotsWest, x, y);
		int previousBlocked = getSideBlocks(direction,x, y,true);
		int [] result = {x,y,direction};
		//end of recursive, termination point
		if(x==13&&y==18){
			return movements;
		}
		int steps = (nMoveable<previousBlocked)?nMoveable:previousBlocked;
		steps = 1;
		if(testTurnedLeft&&nMoveable!=0){
			testTurnedLeft=false;
			r.moveForward(10);
			result= computeForwardLocation(direction, x, y, steps);
			movements.add("F10");
			movements=getNextWallHugLocation(result[0],result[1],direction,movements);
		}else{
			if(wMoveable!=0){
				testTurnedLeft=true;
				float degreeToMove =rotateToDirection(direction,robotsWest);
				int intDegree = Math.round(degreeToMove);
				String rmovement= "R"+intDegree;
				if(intDegree<0){
					rmovement= "L"+(intDegree*-1);
				}
				movements.add(rmovement);
				movements=getNextWallHugLocation(result[0],result[1],robotsWest,movements);
			}else{
				if(steps!=0){
					if(testTurnedRight){
						testTurnedRight=false;
					}

					r.moveForward(10);
					movements.add("F10");
					result= computeForwardLocation(direction, x, y, steps);
					movements=getNextWallHugLocation(result[0],result[1],direction,movements);
				}else{
					testTurnedRight=true;

					float degreeToMove =rotateToDirection(direction,robotsEast);
					int intDegree = Math.round(degreeToMove);
					String rmovement= "R"+intDegree;
					if(intDegree<0){
						rmovement= "L"+(intDegree*-1);
					}
					movements.add(rmovement);
					movements=getNextWallHugLocation(result[0],result[1],robotsEast,movements);
				}
			}
		}
		return movements;
	}
	

	private float rotateToDirection(float currentDirection, float inDirection){
		//////System.out.println("rotate from "+currentDirection+" to "+inDirection);
		float degree = degreeToRotateToDirection(currentDirection,  inDirection);
		r.rotate(degree);
		return degree;
	}
	private float degreeToRotateToDirection(float currentDirection, float inDirection){
		float difference = inDirection-currentDirection;
		if(Math.abs(Math.round(difference))==180){
			return 180;
		}
		if(difference<180){
			if(Math.abs(difference)>180){
				return difference+360;
			}else{
				return difference;
			}
		}else{
			//return (-(currentDirection+360-difference));
			
			return (-(360-difference));
		}
	}
	
	public int getSideBlocks(float direction, int x, int y, boolean left){
		int[][]obstacles = m.getObstacles();
		int[][]explored = m.getExploredTiles();
		int dir = (int)direction;
		int text_x=1;
		int text_y=1;
		switch (dir){
		case 0: 
			text_x = left?x-2:x+2;
			if(text_x>=15||text_x<0){
				return 3;
			}
			if(explored[y+1][text_x]==0||(explored[y+1][text_x]==1&&obstacles[y+1][text_x]==1)){
				return 3;
			}
			if(explored[y][text_x]==0||(explored[y][text_x]==1&&obstacles[y][text_x]==1)){
				return 2;
			}
			if(explored[y-1][text_x]==0||(explored[y-1][text_x]==1&&obstacles[y-1][text_x]==1)){
				return 1;
			}
			break;
		case 90: 
			text_y = left?y+2:y-2;
			if(text_y>=20||text_y<0){
				return 3;
			}
			if(explored[text_y][x+1]==0||(explored[text_y][x+1]==1&&obstacles[text_y][x+1]==1)){
				return 3;
			}
			if(explored[text_y][x]==0||(explored[text_y][x]==1&&obstacles[text_y][x]==1)){
				return 2;
			}
			if(explored[text_y][x-1]==0||(explored[text_y][x-1]==1&&obstacles[text_y][x-1]==1)){
				return 1;
			}
			break;
		case 180: 
			text_x = left?x+2:x-2;
			if(text_x>=15||text_x<0){
				return 3;
			}
			if(explored[y-1][text_x]==0||(explored[y-1][text_x]==1&&obstacles[y-1][text_x]==1)){
				return 3;
			}
			if(explored[y][text_x]==0||(explored[y][text_x]==1&&obstacles[y][text_x]==1)){
				return 2;
			}
			if(explored[y+1][text_x]==0||(explored[y+1][text_x]==1&&obstacles[y+1][text_x]==1)){
				return 1;
			}
			break;
		case 270: 
			text_y = left?y-2:y+2;
			if(text_y>=20||text_y<0){
				return 3;
			}
			if(explored[text_y][x-1]==0||(explored[text_y][x-1]==1&&obstacles[text_y][x-1]==1)){
				return 3;
			}
			if(explored[text_y][x]==0||(explored[text_y][x]==1&&obstacles[text_y][x]==1)){
				return 2;
			}
			if(explored[text_y][x+1]==0||(explored[text_y][x+1]==1&&obstacles[text_y][x+1]==1)){
				return 1;
			}
			break;
		}
		return 3;
	}
	
	protected int[] computeForwardLocation(int direction, int x, int y, int steps){
		switch(direction){
		case 0:
			y=y+steps;
			break;
		case 90:
			x=x+steps;
			break;
		case 180:
			y=y-steps;
			break;
		case 270:
			x=x-steps;
			break;
		}
		int result[]={x,y};
		return result;
	}

	public int isDirectionMoveable(float direction, int x, int y){
		int[][]obstacles = m.getObstacles();
		int[][]explored = m.getExploredTiles();
		int maxMoveable = 3;
		int dir = (int)direction;
		switch (dir){
		case 0: 
			for(int i =0;i<maxMoveable;i++){
				if(y+2+i>=20){
					return i;
				}
				if(explored[y+2+i][x+1]==0||(explored[y+2+i][x+1]==1&&obstacles[y+2+i][x+1]==1)){
					return i;
				}
				if(explored[y+2+i][x]==0||(explored[y+2+i][x]==1&&obstacles[y+2+i][x]==1)){
					return i;
				}
				if(explored[y+2+i][x-1]==0||(explored[y+2+i][x-1]==1&&obstacles[y+2+i][x-1]==1)){
					return i;
				}
			}
			break;
		case 90: 
			for(int i =0;i<maxMoveable;i++){
				if(x+2+i>=15){
					return i;
				}
				if(explored[y-1][x+i+2]==0||(explored[y-1][x+i+2]==1&&obstacles[y-1][x+i+2]==1)){
					return i;
				}
				if(explored[y][x+i+2]==0||(explored[y][x+i+2]==1&&obstacles[y][x+i+2]==1)){
					return i;
				}
				if(explored[y+1][x+i+2]==0||(explored[y+1][x+i+2]==1&&obstacles[y+1][x+i+2]==1)){
					return i;
				}
			}
			break;
		case 180: 
			for(int i =0;i<maxMoveable;i++){
				if(y-2-i<0){
					return i;
				}
				if(explored[y-2-i][x-1]==0||(explored[y-2-i][x-1]==1&&obstacles[y-2-i][x-1]==1)){
					return i;
				}
				if(explored[y-2-i][x]==0||(explored[y-2-i][x]==1&&obstacles[y-2-i][x]==1)){
					return i;
				}
				if(explored[y-2-i][x+1]==0||(explored[y-2-i][x+1]==1&&obstacles[y-2-i][x+1]==1)){
					return i;
				}
			}
			break;
		case 270: 
			for(int i =0;i<maxMoveable;i++){
				if(x-2-i<0){
					return i;
				}
				if(explored[y+1][x-i-2]==0||(explored[y+1][x-i-2]==1&&obstacles[y+1][x-i-2]==1)){
					return i;
				}
				if(explored[y][x-i-2]==0||(explored[y][x-i-2]==1&&obstacles[y][x-i-2]==1)){
					return i;
				}
				if(explored[y-1][x-i-2]==0||(explored[y-1][x-i-2]==1&&obstacles[y-1][x-i-2]==1)){
					return i;
				}
			}
			break;
		}
		return maxMoveable;
	}

	@Override
	public String geType() {
		// TODO Auto-generated method stub
		return null;
	}
	
}