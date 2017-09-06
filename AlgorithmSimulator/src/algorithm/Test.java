package algorithm;

import java.awt.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Stack;

import Data.Map;
import Data.MapListener;
import Data.Position;
import Data.Robot;
import Data.RobotListener;
import Data.Vertex;

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

		System.out.println("\n===Action " +count+"=== Explration rate :"+m.getInstance().getExploredRate());
		count++;
		currentX = Math.round(r.getPosX());
		currentY= Math.round(r.getPosY());
		direction = r.getDirection();
		System.out.println("Hello i am at ("+r.getPosX()+","+r.getPosX()+") facing at "+direction);
	
		//check if i can explore more here, if yes. rotate~~~
		//check right side
		updateVisitedList();
		if(visited[currentY][currentX]==0){
			for(float checkDirection: checkEnvironementOf){
				if(isAnyUndiscovered(currentX, currentY,checkDirection)){
					System.out.println(checkDirection+" direction have undiscovered tiles");
					//rotate to east
					rotateToDirection(direction,checkDirection);
					return;
				}
			}	
		}

		//set visited
		System.out.println("I have finish exploring ("+r.getPosX()+","+r.getPosX()+"), lets move on");
		visited[currentY][currentX]=1;

		System.out.println("I could move to :");
		checked =new int[20][15];
		int[] location  = getBestNextStop(currentX,currentY,100);
		System.out.println("Best Location :"+location[0]+","+location[1]+","+location[2]+","+location[3]);
		
		//move to best location
		moveToLocation(currentX, currentY, direction, location[0],location[1]);
	}
	
	private void moveToLocation(int x1, int y1,float facing, int x2, int y2) {
		ArrayList<String> arr = new ArrayList<String>();
		Vertex s = m.getVertices()[y1][x1];
		if(s==null){
			System.out.println("Error:"+x1+","+y1);
			System.out.println("Error:"+x1+","+y1);
			return;
		}
		Vertex e = m.getVertices()[y2][x2];
		float direction = facing;
		java.util.List<Vertex> path = Dijkstra.computePaths(s, e);
		System.out.println("Path to travel: "+path);
		System.out.println("I am facing "+direction);
		for(int i =0;i<path.size()-1;i++){
			Vertex v1 =path.get(i);
			Vertex v2 =path.get(i+1);
			float degree = getDegreeBetweenTwoPoint(v1.x,v1.y,v2.x,v2.y);
			if(degree!= direction)
			{
				//float degreeBetween= degreeToRotateToDirection(direction,degree);
				rotateToDirection(direction,degree);
				arr.add("ROTATE_FROM_"+direction+"_TO"+degree);
				direction = degree;
			}
			arr.add("MOVE_FORWARD_"+10);
			r.moveForward(10);
		}

	//	System.out.println("=============");
		for(String instruction: arr){
	//		System.out.println(instruction);
		}
	//	System.out.println("=============");
	}



	
	/*
	private void getTotalCost(Stack <int[]>stack, float facingDirection) {
		for(int i =0;i<stack.size();i++){
			int[]v1 = stack.get(i);
			System.out.print(v1[0]+","+v1[1]+"("+0+" degree)=>");
		}
		System.out.println("end");
	}*/

	private  int checked[][]= new int[20][15];

	private int[] getBestNextStop(int inX, int inY, int maxHop) {
		int canExplore= howManyTileCanBeDiscovered(1,1);
		float degree = getDegreeBetweenTwoPoint(currentX,currentY,1,1);
		int degreeBetween =0;
		if(currentX!=1 || currentY!=1){
			 degreeBetween = (int) degreeToRotateToDirection(direction,degree);
		}
		int result[] = {1,1,canExplore,degreeBetween,0};
		return getBestNextStopRecursive(inX,  inY,  maxHop, result);
	}
	
	private int[] getBestNextStopRecursive(int inX, int inY, int maxHop, int[] inDefault) {
		Vertex v = m.getVertices()[inY][inX];
		if(v==null){
			return inDefault;
		}
		ArrayList<Vertex> neighbours = v.adjacencies;
		int []optimised=inDefault;
		for(Vertex nextNode: neighbours){
			int x = nextNode.x;
			int y = nextNode.y;
			if(checked[y][x]==0){
					checked[y][x]=1;
					int canExplore= howManyTileCanBeDiscovered(x,y);
					float degree = 0;
					int degreeBetween = 0;
					if(currentX!=x || currentY!=y){
						degree=getDegreeBetweenTwoPoint(currentX,currentY,x,y);
						degreeBetween=(int) degreeToRotateToDirection(direction,degree);
					}
					int squareAway =  getSquareAway(currentX,currentY,x,y);
					int result[] = {x,y,canExplore,degreeBetween,squareAway};
					if(maxHop==1){
						return result;
					}else{
						int result1[] =getBestNextStopRecursive(x,y,maxHop-1,inDefault);
						result = calculateScoreOfVertexAndCompare(result1,result);
					}
				optimised = calculateScoreOfVertexAndCompare(result,optimised);
			}
		}
		return optimised;
	}

	 
	private int[] calculateScoreOfVertexAndCompare(int[] place1, int[] place2){
		float exploreMoreWeightage = 5;
		int nearByWeightage = 0;
		float endLocationWeightage = 10;
		int[] endLocation = {13,18};
		float startWeightage = 0;
		int[] startLocation = {1,1};
		float mapDiscoveredRate = m.getExploredRate();
		
		//if end point found
		if(m.getExploredTiles()[18][13]==1){
			
			endLocationWeightage=0;
			if(mapDiscoveredRate>90){
				startWeightage = 4;
				exploreMoreWeightage = 1;
			}
		}
		
		float score1=0;
		float score2=0;
		
		//if visited then score ==0
		//if start weightage is more than 0 ,we can go to vistied location.
		if(visited[place1[1]][place1[0]]==0 || startWeightage >0){
			int exploreSquareCount1 = place1[2];	
			int squareAway1 = Math.abs(place1[0]-endLocation[0])+Math.abs(place1[1]-endLocation[1]);
			int nearBySquareAway1 = (int) (Math.abs(place1[0]-r.getPosX())+Math.abs(place1[1]-r.getPosY()));
			int startSquareAway1 = (int) (Math.abs(place1[0]-startLocation[0])+Math.abs(place1[1]-startLocation[0]));
			score1 = (45-startSquareAway1)*startWeightage +(45-nearBySquareAway1)*nearByWeightage + (45-squareAway1)*endLocationWeightage 	+ 	exploreSquareCount1*exploreMoreWeightage;
				
		}
		if(visited[place2[1]][place2[0]]==0 || startWeightage >0){
			int exploreSquareCount2 = place2[2];
			int squareAway2 = Math.abs(place2[0]-endLocation[0])+Math.abs(place2[1]-endLocation[1]);
			int nearBySquareAway2 = (int) (Math.abs(place2[0]-r.getPosY())+Math.abs(place2[1]-r.getPosY()));
			int startSquareAway2 = (int) (Math.abs(place2[0]-startLocation[1])+Math.abs(place2[1]-startLocation[1]));
			score2 = (45-startSquareAway2)*startWeightage +(45-nearBySquareAway2)*nearByWeightage + (45-squareAway2)*endLocationWeightage 	+ 	exploreSquareCount2*exploreMoreWeightage;
		}
		
		System.out.println("\t ("+(place1[0])+","+place1[1]+")"+"- score:" +score1);
		System.out.println("\t ("+(place2[0])+","+place2[1]+")"+"- score:" +score2);
			
		int []result;
		if(score1>score2){
			result = place1;
		}else{
			if(score1==score2){
				result = place2;
			}else{
				result = place2;
			}
		}
		
		return result;
	}
	
	private int getSquareAway(int x1, int y1, int x2, int y2) {
		Vertex v1 = m.getVertices()[y1][x1];
		Vertex v2 = m.getVertices()[y2][x2];
		return 0;
		//return Math.abs(x1-x2)+Math.abs(y1-y2);
	}

	//check if the position and direction have any undiscovered tiles
	private boolean isAnyUndiscovered(int currentX,int currentY, float inDirection){
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
	
	private void rotateToDirection(float currentDirection, float inDirection){
		//System.out.println("rotate from "+currentDirection+" to "+inDirection);
		r.rotate(degreeToRotateToDirection(currentDirection,  inDirection));
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
			return (-(currentDirection+360-difference));
		}
	}
	
	//get degree between 2 point
	private float getDegreeBetweenTwoPoint(int x1,int y1,int x2, int y2){
		if(x1==x2 && y1==y2)
			return 0;
		   float angle = (float) Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
		    if(angle < 0){
		        angle += 360;
		    }
		    angle=(angle-90)%360;
		    angle = (360-angle)%360;
		    return angle;
	}
	

	//update the visited list
	private void updateVisitedList(){
		for(int x1=1;x1<14;x1++){
			for(int y1=1;y1<19;y1++){
				if(visited[y1][x1]==0){
					if(!isAnyUndiscovered(x1,y1,NORTH)&&!isAnyUndiscovered(x1,y1,SOUTH)&&!isAnyUndiscovered(x1,y1,EAST)&&!isAnyUndiscovered(x1,y1,WEST)){
						visited[y1][x1]=1;
					}
				}
			}
		}
	}
	
	//get number of tiles that are unexplored in a location
	private int howManyTileCanBeDiscovered(int x, int y){
		int count=0;
		float[] allDirection = {NORTH,SOUTH,EAST,WEST};
		for(float dir: allDirection){
			Position[][] lineOfSensors = r.getSensorSimulator().getLineOfSensor(x, y, dir);
			for(int i =0;i<3;i++){
				Position[] sensors = lineOfSensors[i];
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
		}
		return count;
	}
	
	@Override
	public void updateRobot() {
		
	}
	
	@Override
	public void onRobotStop() {
		
	}
}
