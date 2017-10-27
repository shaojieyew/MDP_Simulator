package algorithm;

/*follow left wall, with minimum rotation*/

import java.util.List;
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

public class FastestPathType3 extends FastestPath {
	public static boolean testTurnedLeft =false;
	public static boolean testTurnedRight =false;
	@Override
	public Message computeAction() {
		//ArrayList<String> instructions = new  ArrayList<String>();
		List<Vertex> positions = null;
		positions = getNextWallHugLocation(1,1,0,positions);
		
		
		List<Vertex> path =positions;
		
			//System.out.println("Path: "+path);
			float direction = 0;
			int forwardCount = 0;
			ArrayList<String> instructions = new ArrayList<String>();
			direction = r.getDirection();
			if(direction !=0){
				float degreeToMove =rotateToDirection(direction,0);
				int intDegree = Math.round(degreeToMove);
				if(intDegree!=0){
					String rmovement= "R"+intDegree;
					if(intDegree<0){
						rmovement= "L"+(intDegree*-1);
					}
					instructions.add(rmovement);
					//Exploration.lastMovedBeforeCalibrate = Exploration.lastMovedBeforeCalibrate+ Exploration.rotationCost;
				}
				direction=0;
			}
			
			for(int i =0;i<path.size()-1;i++){
				Vertex v1 =path.get(i);
				Vertex v2 =path.get(i+1);
				float degree = getDegreeBetweenTwoPoint(v1.x,v1.y,v2.x,v2.y);
				if(degree!= direction)
				{
					if(forwardCount!=0){
						instructions.add("F"+forwardCount);
						r.moveForward(forwardCount);
						Exploration.lastMovedBeforeCalibrate = Exploration.lastMovedBeforeCalibrate+ forwardCount;
						//if(lastMovedBeforeCalibrate>=Exploration.intervalForCalibrate){
						
						Vertex v3 =path.get(i+1);
						Vertex v4 =null;
						if(i+2<path.size()-1){
							 v4 =path.get(i+2);
						}
						if(calibration){
							instructions = Exploration.addCalibrationCommand((int)v2.x,(int)v2.y,(int) direction,instructions,null);
						}
						//}
						forwardCount=0;
					}
					
					float degreeToMove = rotateToDirection(direction,degree);
					int intDegree = Math.round(degreeToMove);
					String movement= "R"+intDegree;
					if(intDegree<0){
						movement= "L"+(intDegree*-1);
					}
					instructions.add(movement);
					Exploration.lastMovedBeforeCalibrate = Exploration.lastMovedBeforeCalibrate+ Exploration.rotationCost;
					direction = degree;
				}
				
				forwardCount=forwardCount+10;
				if(forwardCount>=Exploration.intervalForCalibrate){
				//if(forwardCount>=Exploration.intervalForCalibrate){
					instructions.add("F"+forwardCount);
					r.moveForward(forwardCount);
					Exploration.lastMovedBeforeCalibrate = Exploration.lastMovedBeforeCalibrate+ forwardCount;
					//if(lastMovedBeforeCalibrate>=Exploration.intervalForCalibrate){
					Vertex v3 =path.get(i+1);
					Vertex v4 =null;
					if(i+2<path.size()-1){
						 v4 =path.get(i+2);
					}
					if(calibration){
						instructions = Exploration.addCalibrationCommand((int)v2.x,(int)v2.y,(int) direction,instructions,getNextInstruction(direction,v3,v4));
					}
					forwardCount=0;
				}else{
					if(i==path.size()-2){
						if(forwardCount>0){
							instructions.add("F"+forwardCount);
							r.moveForward(forwardCount);
							Exploration.lastMovedBeforeCalibrate = Exploration.lastMovedBeforeCalibrate+ forwardCount;
							//if(lastMovedBeforeCalibrate>=Exploration.intervalForCalibrate){
							Vertex v3 =path.get(i+1);
							Vertex v4 =null;
							if(i+2<path.size()-1){
								 v4 =path.get(i+2);
							}
							if(calibration){
								instructions = Exploration.addCalibrationCommand((int)v2.x,(int)v2.y,(int) direction,instructions,getNextInstruction(direction,v3,v4));
								}
						//}
							forwardCount=0;
						}
					}
				}
			}

			String []movements = new String[instructions.size()];
			int index=0;
			for(String instruction: instructions){
					System.out.println(instruction);
					movements[index] = instruction;
					index++;
			}
			Message message  = new Message();
			message.setMovements(movements);
			Vertex lastLocation = path.get(path.size()-1);
			int [] location = {(int) lastLocation.x,(int) lastLocation.y};
			message.setRobotLocation(location);
			message.setEndOfExploration(false);
			message.setDirection(direction);
			return message;
	}
	

	private List<Vertex> getNextWallHugLocation( int x, int y,int direction, List<Vertex> positions){
		Vertex[][] vertices = m.getVertices();
		if(positions==null){
			positions = new ArrayList<Vertex>();
			positions.add(vertices[y][x]);
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
			//positions.add(vertices[result[1]][result[0]]);
			return positions;
		}
		int steps = (nMoveable<previousBlocked)?nMoveable:previousBlocked;
		if(steps>0){
			steps = 1;
		}
		if(testTurnedLeft&&nMoveable!=0){
			testTurnedLeft=false;
			//r.moveForward(10);
			result= computeForwardLocation(direction, x, y, steps);
			//movements.add("F10");
			for(int i =0;i<positions.size();i++){
				if(positions.get(i).x==result[0]&&positions.get(i).y==result[1]){
					for(int j =positions.size()-1;j>=i;j--){
						positions.remove(j);
					}
					break;
				}
			}
			positions.add(vertices[result[1]][result[0]]);
			positions=getNextWallHugLocation(result[0],result[1],direction,positions);
		}else{
			if(wMoveable!=0){
				testTurnedLeft=true;
				/*
				float degreeToMove =rotateToDirection(direction,robotsWest);
				int intDegree = Math.round(degreeToMove);
				String rmovement= "R"+intDegree;
				if(intDegree<0){
					rmovement= "L"+(intDegree*-1);
				}
				movements.add(rmovement);*/
				positions=getNextWallHugLocation(result[0],result[1],robotsWest,positions);
				//movements=getNextWallHugLocation(result[0],result[1],robotsWest,movements);
			}else{
				if(steps!=0){
					if(testTurnedRight){
						testTurnedRight=false;
					}

					//r.moveForward(10);
					//movements.add("F10");
					result= computeForwardLocation(direction, x, y, steps);
					for(int i =0;i<positions.size();i++){
						if(positions.get(i).x==result[0]&&positions.get(i).y==result[1]){
							for(int j =positions.size()-1;j>=i;j--){
								positions.remove(j);
							}
							break;
						}
					}
					positions.add(vertices[result[1]][result[0]]);
					//movements=getNextWallHugLocation(result[0],result[1],direction,movements);
					positions=getNextWallHugLocation(result[0],result[1],direction,positions);
				}else{
					testTurnedRight=true;
					/*
					float degreeToMove =rotateToDirection(direction,robotsEast);
					int intDegree = Math.round(degreeToMove);
					String rmovement= "R"+intDegree;
					if(intDegree<0){
						rmovement= "L"+(intDegree*-1);
					}
					movements.add(rmovement);*/
					positions=getNextWallHugLocation(result[0],result[1],robotsEast,positions);
					//movements=getNextWallHugLocation(result[0],result[1],robotsEast,movements);
				}
			}
		}
		return positions;
	}
	

	public String getNextInstruction(float direction,Vertex v1,Vertex v2 ){
		if(v2==null){
			return null;
		}
		float degree = getDegreeBetweenTwoPoint(v1.x,v1.y,v2.x,v2.y);
		if(degree!= direction)
		{
			float degreeToMove = degreeToRotateToDirection(direction,degree);
			int intDegree = Math.round(degreeToMove);
			String movement= "R"+intDegree;
			if(intDegree<0){
				movement= "L"+(intDegree*-1);
			}
			return movement;
		}
		return null;
	}
	
	
	//get degree between 2 point
	private static float getDegreeBetweenTwoPoint(float x,float y,float x2, float y2){
		if(x==x2 && y==y2)
			return 0;
		   float angle = (float) Math.toDegrees(Math.atan2(y2 - y, x2 - x));
		    if(angle < 0){
		        angle += 360;
		    }
		    angle=(angle-90)%360;
		    angle = (360-angle)%360;
		    return angle;
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