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
	private int startingX = 1;
	private int startingY = 1;
	private float direction = 0;
	public static final float NORTH = 0;
	public static final float EAST = 90;
	public static final float SOUTH = 180;
	public static final float WEST = 270;

	
	int printCount=1;
	public Test(){
		Robot.getInstance().setExploring(true);
		computeAction();
	}
	public Test(int startAtX, int startAtY){
		startingX=startAtX;
		startingY=startAtY;
		computeAction();
	}

	@Override
	public void updateMap() {
		computeAction();
	}

	private float checkEnvironementOf[]={NORTH,SOUTH,EAST,WEST};
	protected void computeAction(){

		System.out.println("\n===Action " +printCount+"=== Explration rate :"+m.getInstance().getExploredRate());
		printCount++;
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
		checkedVisited =new int[20][15];
		int[] location  = getBestNextStop(currentX,currentY,1000);
		System.out.println("Best Location :"+location[0]+","+location[1]+","+location[2]+","+location[3]);
		
		//move to best location
		moveToLocation(currentX, currentY, direction, location[0],location[1]);
		if(location[0]==1&&location[1]==1&&okToTerminate){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Robot.getInstance().setExploring(false);
			r.removeListener(this);
			m.removeListener(this);
		}
	}
	
	boolean okToTerminate=false;
	
	//get Instructions To Location
	private void moveToLocation(int x1, int y1,float facing, int x2, int y2) {
		ArrayList<String> instructions = new ArrayList<String>();
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
				instructions.add("ROTATE_FROM_"+direction+"_TO"+degree);
				direction = degree;
			}
			instructions.add("MOVE_FORWARD_"+10);
			r.moveForward(10);
		}

	//	System.out.println("=============");
	//	for(String instruction: instructions){
	//		System.out.println(instruction);
	//	}
	//	System.out.println("=============");
	}


	private int[] getBestNextStop(int inX, int inY, int maxHop) {
		int canExplore= howManyTileCanBeDiscovered(1,1,false);
		float degree = getDegreeBetweenTwoPoint(currentX,currentY,currentX,currentY);
		int degreeBetween =0;
		//if(currentX!=1 || currentY!=1){
			 degreeBetween = (int) degreeToRotateToDirection(direction,degree);
		//}
		int result[] = {currentX,currentY,canExplore,degreeBetween,0};
		return getBestNextStopRecursive(inX,  inY,  maxHop, result);
	}

	//BFS, check all nodes
	private  int checkedVisited[][]= new int[20][15];
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
			if(checkedVisited[y][x]==0){
				checkedVisited[y][x]=1;
					int canExplore= howManyTileCanBeDiscovered(x,y,false);
					if(canExplore>0&&canExplore<=3){
						canExplore =howManyTileCanBeDiscovered(x,y,true);
					}
					float degree = 0;
					int degreeBetween = 0;
					if(currentX!=x || currentY!=y){
						degree=getDegreeBetweenTwoPoint(currentX,currentY,x,y);
						degreeBetween=(int) degreeToRotateToDirection(direction,degree);
					}
					int squareAway =  getDistanceAway(currentX,currentY,x,y);
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

	 //greedy heuristic 
	private int[] calculateScoreOfVertexAndCompare(int[] place1, int[] place2){
		float mapDiscoveredRate = m.getExploredRate();
		float exploreMoreWeightage = 1; //explore_score:1-36
		int nearByWeightage = 0;	     //score: 1-28
		float endLocationWeightage = 0;  //score: 1-28
		float startWeightage = 0;        //score: 1-28
		int distanceWeightage = 0;
		int[] endLocation = {13,18};
		int[] startLocation = {1,1};
		//if end point found
		if(m.getExploredTiles()[18][13]==1){
			endLocationWeightage=0;
			if(mapDiscoveredRate>0.9){
				startWeightage = 0;
				exploreMoreWeightage=1;
				nearByWeightage=1;
			}
			if(mapDiscoveredRate==1){
				startWeightage = 1000000;
				exploreMoreWeightage=0;
				nearByWeightage=0;
				okToTerminate=true;
			}
		}
		long currentTimeStamp = System.currentTimeMillis();
    	long minutes = ((currentTimeStamp-Robot.getInstance().getExploringStartTime())/1000)/60;
		if(minutes>=5){
			endLocationWeightage=0;
			startWeightage = 1000000;
			exploreMoreWeightage=0;
			nearByWeightage=0;
			okToTerminate=true;
		}

		int x1 = place1[0];
		int y1 = place1[1];
		int x2 = place2[0];
		int y2 = place2[1];
		if(((place1[2]==1||place2[2]==1)&&( Math.abs(x1-x2)+Math.abs(y1-y2))>10)
			||
			(Math.abs(place1[2]-place2[2])<=1)
			){
			 distanceWeightage=10;
		}
		
		float score1=0;
		float score2=0;
		score1=calculateScore( place1,  distanceWeightage, startWeightage,  nearByWeightage,  endLocationWeightage,  exploreMoreWeightage);
		score2=calculateScore( place2,  distanceWeightage, startWeightage,  nearByWeightage,  endLocationWeightage,  exploreMoreWeightage);
		//if visited then score ==0
		//if start weightage is more than 0 ,we can go to vistied location.
		
		
		if(score1!=0)
		System.out.println("\t ("+(place1[0])+","+place1[1]+")"+"- score:" +score1 +" \ttotal explorable:"+ place1[2]+" \t distance:"+ place1[4]);

		if(score2!=0)
			System.out.println("\t ("+(place2[0])+","+place2[1]+")"+"- score:" +score2+" \ttotal explorable:"+ place2[2]+" \t distance:"+ place1[4]);
			
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
	private float calculateScore(int place2[], int distanceWeightage,float startWeightage, float nearByWeightage, float endLocationWeightage, float exploreMoreWeightage){
		//int x = place[0];
		//int y = place[0];

		float leanXDirection = 1;
		float leanYDirection=1;
		int[] endLocation = {13,18};
		int[] startLocation = {1,1};
		float score=0;
		if(visited[place2[1]][place2[0]]==0 || ((visited[place2[1]][place2[0]]==1)&&(place2[0]==1&&place2[1]==1)&&startWeightage>0)){
			int exploreSquareCount2 = place2[2];
			int distanceAway2 = 300-(place2[4]);
			int squareAway2 = 29-(Math.abs(place2[0]-endLocation[0])+Math.abs(place2[1]-endLocation[1]));
			int nearBySquareAway2 = (int) (29-(Math.abs(place2[0]-r.getPosY())+Math.abs(place2[1]-r.getPosY())));
			int startSquareAway2 = (int) (29-(Math.abs(place2[0]-startLocation[1])+Math.abs(place2[1]-startLocation[1])));
	
			score =(distanceAway2*distanceWeightage)+ (startSquareAway2)*startWeightage +(nearBySquareAway2)*nearByWeightage + (squareAway2)*endLocationWeightage 	+ 	exploreSquareCount2*exploreMoreWeightage;
			
		}
		return score;
	}
	
	private int getDistanceAway(int x1, int y1, int x2, int y2) {
		Vertex v1 = m.getVertices()[y1][x1];
		Vertex v2 = m.getVertices()[y2][x2];
		java.util.List<Vertex> path = null;
		if(v1!=null&&v2!=null){
			path=Dijkstra.computePaths(v1, v2);
		}
		if(path==null&&path.size()>0){
			return path.size()-1;
		}
		return  Math.abs(x1-x2)+Math.abs(y1-y2);
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
	private int howManyTileCanBeDiscovered(int x, int y, boolean firstLayer){
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
					if(firstLayer){
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
	@Override
	public void onRobotStartExploring() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onRobotStopExploring() {
		// TODO Auto-generated method stub
		
	}
}
