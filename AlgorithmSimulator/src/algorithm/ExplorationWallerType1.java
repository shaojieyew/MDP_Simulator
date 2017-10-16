package algorithm;
/*follow left wall*/

import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.Stack;

import Data.Map;
import Data.MapListener;
import Data.Position;
import Data.Robot;
import Data.RobotListener;
import Data.Vertex;
import RPiInterface.Message;
import RPiInterface.RobotSensorSimulatorType2;

public class ExplorationWallerType1 extends Exploration {

	public static boolean turnedLeft =false;
	public static boolean turnedRight =false;
	public static boolean testTurnedLeft =false;
	public static boolean testTurnedRight =false;
	public static boolean newVisit =false;
	public static float newVisitDirection =0;
	public static boolean finishHuggingWall =false;
	public  int [][] visited = new int[20][15];
	public  float checkEnvironementOf[];
	int printCount=1;

	public static int movedCount = 0;
	
	//from rpi
	public  ExplorationWallerType1(boolean isTerminate){
		super(isTerminate);
	}
	
	//from simulator
	public ExplorationWallerType1(){
		super();
	}
	public ExplorationWallerType1(int startAtX, int startAtY){
		super(startAtX,startAtY);
	}

	@Override
	public void init() {
		visited = new int[20][15];
		float checkEnvironementOf[]={NORTH,SOUTH,EAST,WEST};
		this.checkEnvironementOf =checkEnvironementOf;
	}


	
	public Message computeAction(){
		float mapDiscoveredRate = m.getExploredRate();
		long currentTimeStamp = System.currentTimeMillis();
    	long seconds = ((currentTimeStamp-Robot.getInstance().getExploringStartTime())/1000);
		if(waypointFound()&&endPointFound()&&(seconds>=getAutoTerminate_time()||isOkToTerminate()||mapDiscoveredRate>=getAutoTerminate_explore_rate())){
			finishHuggingWall=true;
			terminate();
		}
		
		Message message = null;
		int currentX = Math.round(r.getPosX());
		int currentY =Math.round(r.getPosY());
		float direction = r.getDirection();

		
		if(m.getExploredTiles()[currentY][currentX]==0){
			return null;
		}
		int result[] ;
		updateVisitedList();
		if(allPossibleNodeVisited()){
			finishHuggingWall=true;
		}
		
		if(finishHuggingWall&&(!isOkToTerminate()||!startPointFound())){
			int currentDirectionIndex = 0;
			if(visited[currentY][currentX]==0){
				if(!newVisit){
					newVisit = true;
					newVisitDirection = direction;
				}
				int count = 0;
				int checkDirection = 0;
				int toRotate = 360;
				for(int i =currentDirectionIndex ; i<currentDirectionIndex+4 ; i++){
					float directionToCheck = checkEnvironementOf[i%4];
					int temp = howManyUndiscovered(currentX, currentY,directionToCheck);
					//System.out.println(direction+" facing =  "+directionToCheck+" , "+ temp);
					float degree = degreeToRotateToDirection(direction,  directionToCheck);
					if(temp>count){
						count = temp;
						checkDirection = (int) directionToCheck;
						toRotate = (int) degree;
					}else{
						if(temp==count){
							if(Math.abs(degree)<toRotate){
								//System.out.println(toRotate+" vs "+ degree);
								checkDirection = (int) directionToCheck;
								toRotate = (int) degree;
							}
						}
					}
				}
				//System.out.println(checkDirection);
					if(isAnyUndiscovered(currentX, currentY,checkDirection)){
						float degree = rotateToDirection(direction,checkDirection);
						int intDegree = Math.round(degree);
						String movement= "R"+intDegree;
						if(intDegree<0){
							movement= "L"+(intDegree*-1);
						}
						message  = new Message();
						String []movments = {movement};
						message.setMovements(movments);
						int [] location = {currentX,currentY};
						message.setRobotLocation(location);
						message.setEndOfExploration(false);
						message.setDirection(checkDirection);
						//return message;
						return message;
					}
				}
			}
		
		
		if(!finishHuggingWall){
			ArrayList<String> instructions = new ArrayList<String>();
			if(newVisit){
				newVisit = false;

				float degreeToMove =rotateToDirection(direction,newVisitDirection);
				direction= newVisitDirection;
				int intDegree = Math.round(degreeToMove);
				String rmovement= "R"+intDegree;
				if(intDegree<0){
					rmovement= "L"+(intDegree*-1);
				}
				instructions.add(rmovement);
			}
			if(testTurnedRight==true&&outofWallhugging(currentX,currentY,(int)direction)){
				finishHuggingWall=true;
			}else{
				message = getNextWallHugLocation(currentX,currentY,(int)direction, instructions);
				if(message.getRobotLocation()[0]==1&&message.getRobotLocation()[1]==1){
					finishHuggingWall = true;
				}else{
					return message;
				}
			}
		}
		if(finishHuggingWall){
			checkedVisited =new int[20][15];
			result  = getBestNextStop(currentX,currentY,10000);
			if(currentX==result[0]&&currentY==result[1]&&(currentX!=1||currentY!=1)){
				result[0]=1;
				result[1]=1;
			}
			
			//move to best location
			
			if(result[0]==1&&result[1]==1&&isOkToTerminate()){
				message = moveToLocation(currentX, currentY, r.getDirection(), result[0],result[1],0);
				message.setEndOfExploration(true);
				cleanUpVar();
				destroy();
			}else{
				message = moveToLocation(currentX, currentY, r.getDirection(), result[0],result[1]);
			}
		}
		return message;
	}

	public void cleanUpVar(){
		 testTurnedLeft =false;
		 testTurnedRight =false;
		 newVisit =false;
		 newVisitDirection =0;
		 finishHuggingWall =false;
	}
	/*
	 if (turnedleft previously and forward no wall)
		  go forwards 1 cell
	 elseif (no wall at left)
		  turn 90deg left
		elseif (no wall forwards)
		  go forwards 1 cell
		else
		  turn 90deg right
	 * */

	
	private int[] getNextWallHugCheckLocation( int x, int y,int direction,boolean turnedLeft, boolean turnedRight){
		int robotsNorth = (int) ((NORTH+direction)%360);
		int robotsEast = (int) ((EAST+direction)%360);
		int robotsWest = (int) ((WEST+direction)%360);
		int nMoveable = isDirectionMoveable(robotsNorth, x, y);
		int wMoveable = isDirectionMoveable(robotsWest, x, y);
		int previousBlocked = getLeftBlocks(direction,x, y);
		int [] result = {x,y,direction};
		int steps = (nMoveable<previousBlocked)?nMoveable:previousBlocked;
		if(steps>0){
			//steps = 1;
			int explored[][] = m.getExploredTiles();
			int obstacles[][] = m.getObstacles();
			boolean stopstep = false;
			int i = 1;
			
			for( i =1;i<=steps;i++){
				for(int j=0;j<3;j++){

					int checkX1  =0;
					int checkY1 = 0;
					switch(direction){
						case 0: {
							checkX1= x+2+j;
							checkY1= y+i-1;
							break;
						}
						case 90: {
							checkX1= x+i-1;
							checkY1= y-2-j;
							break;
						}
						case 180: {
							checkX1= x-2-j;
							checkY1=y-i+1;
							break;
						}
						case 270: {
							checkX1= x-i+1;
							checkY1= y+2+j;
							break;
						}
					}
					if(checkX1>=0&&checkX1<=14&&checkY1>=0&&checkY1<=19){
						if(explored[checkY1][checkX1]==0){
							stopstep = true;
							break;
						}else{
							if(obstacles[checkY1][checkX1]==1){
								break;
							}
						}
					}
				}
				if(stopstep){
					break;
				}
			}
			int tempsteps= i<steps?i:steps;
			stopstep= false;
			for( i =1;i<=steps;i++){
				for(int j=0;j<3;j++){

					int checkX2  =0;
					int checkY2 = 0;
					switch(direction){
						case 0: {
							checkX2= x-2-j;
							checkY2= y+i+1;
							break;
						}
						case 90: {
							checkX2= x+i+1;
							checkY2= y+2+j;
							break;
						}
						case 180: {
							checkX2= x+2+j;
							checkY2=y-i+1;
							break;
						}
						case 270: {
							checkX2= x-i-1;
							checkY2= y-2-j;
							break;
						}
					}
					if(checkX2>=0&&checkX2<=14&&checkY2>=0&&checkY2<=19){
						if(explored[checkY2][checkX2]==0){
							stopstep = true;
							break;
						}else{
							if(obstacles[checkY2][checkX2]==1){
								break;
							}
						}
					}
				}
				if(stopstep){
					break;
				}
			}
			steps= i<tempsteps?i:tempsteps;
		}else{
			return result;
		}
		if(result[0]==1&&result[1]==1){
			return result;
		}
		
		if(turnedLeft&&nMoveable!=0){
			int next_result[]= computeForwardLocation(direction, x, y, steps);
			result=getNextWallHugCheckLocation(next_result[0],next_result[1],direction,false,turnedRight);
		}else{
			if(wMoveable!=0){
				result=getNextWallHugCheckLocation(result[0],result[1],robotsWest,true,turnedRight);
			}else{
				if(steps!=0){
					int next_result[]= computeForwardLocation(direction, x, y, steps);
					result=getNextWallHugCheckLocation(next_result[0],next_result[1],direction,turnedLeft,false);

				}else{
					result=getNextWallHugCheckLocation(result[0],result[1],robotsEast,turnedLeft,true);
				}
			}
		}
		return result;
	}
	
	private Message getNextWallHugLocation( int x, int y,int direction,ArrayList<String> instructions){
	
		int robotsNorth = (int) ((NORTH+direction)%360);
		int robotsEast = (int) ((EAST+direction)%360);
		int robotsWest = (int) ((WEST+direction)%360);
		int nMoveable = isDirectionMoveable(robotsNorth, x, y);
		int wMoveable = isDirectionMoveable(robotsWest, x, y);
		int previousBlocked = getLeftBlocks(direction,x, y);
		Message message;
		//int [] endResult = {x,y,direction};
		int []endResult = getNextWallHugCheckLocation(x,y,direction,testTurnedLeft,testTurnedRight);
		if(endResult[0]!=1||endResult[1]!=1||!endPointFound()){
			int [] result = {x,y,direction};
			int steps = (nMoveable<previousBlocked)?nMoveable:previousBlocked;
			int tempsteps=1000;
			if(steps>0){
				//steps = 1;
				int explored[][] = m.getExploredTiles();
				int obstacles[][] = m.getObstacles();
				boolean stopstep = false;
				int i = 1;
				
				for( i =1;i<=steps;i++){
					for(int j=0;j<3;j++){
	
						int checkX1  =0;
						int checkY1 = 0;
						switch(direction){
							case 0: {
								checkX1= x+2+j;
								checkY1= y+i-1;
								break;
							}
							case 90: {
								checkX1= x+i-1;
								checkY1= y-2-j;
								break;
							}
							case 180: {
								checkX1= x-2-j;
								checkY1=y-i+1;
								break;
							}
							case 270: {
								checkX1= x-i+1;
								checkY1= y+2+j;
								break;
							}
						}
						if(checkX1>=0&&checkX1<=14&&checkY1>=0&&checkY1<=19){
							if(explored[checkY1][checkX1]==0){
								stopstep = true;
								break;
							}else{
								if(obstacles[checkY1][checkX1]==1){
									break;
								}
							}
						}
					}
					if(stopstep){
						tempsteps = i;
						break;
					}
				}
				steps= steps<tempsteps?steps:tempsteps;
				stopstep= false;
				tempsteps =1000;
				for( i =1;i<=steps;i++){
					for(int j=0;j<3;j++){
	
						int checkX2  =0;
						int checkY2 = 0;
						switch(direction){
							case 0: {
								checkX2= x-2-j;
								checkY2= y+i+1;
								break;
							}
							case 90: {
								checkX2= x+i+1;
								checkY2= y+2+j;
								break;
							}
							case 180: {
								checkX2= x+2+j;
								checkY2=y-i-1;
								break;
							}
							case 270: {
								checkX2= x-i-1;
								checkY2= y-2-j;
								break;
							}
						}
						if(checkX2>=0&&checkX2<=14&&checkY2>=0&&checkY2<=19){
							if(explored[checkY2][checkX2]==0){
								stopstep = true;
								break;
							}else{
								if(obstacles[checkY2][checkX2]==1){
									break;
								}
							}
						}
					}
					if(stopstep){
						tempsteps = i;
						break;
					}
				}
				steps= steps<tempsteps?steps:tempsteps;
			}
			
			if(testTurnedLeft&&nMoveable!=0){	
				/***/
				/* Calibration**/
				if(movedCount>=intervalForCalibrate){
					instructions = addCalibrationCommand(x,y,(int) direction,instructions);
					movedCount = 0;
				}
				/***/
				testTurnedLeft=false;
				r.moveForward(10*steps);
				instructions.add("F"+10*steps);
				result= computeForwardLocation(direction, x, y, steps);
				movedCount = movedCount+10*steps;
				//message=getNextWallHugLocation(result[0],result[1],direction,instructions);
				
			}else{
				if(wMoveable!=0){
					testTurnedLeft=true;
					float degreeToMove =rotateToDirection(direction,robotsWest);

					direction = robotsWest;
					int intDegree = Math.round(degreeToMove);
					String rmovement= "R"+intDegree;
					if(intDegree<0){
						rmovement= "L"+(intDegree*-1);
					}
					instructions.add(rmovement);
					//message=getNextWallHugLocation(result[0],result[1],robotsWest,instructions);
				}else{
					if(steps!=0){
						/***/
						/* Calibration**/
						if(movedCount>=intervalForCalibrate){
							instructions = addCalibrationCommand(x,y,(int) direction,instructions);
							movedCount = 0;
						}
						/***/
						if(testTurnedRight){
							testTurnedRight=false;
						}
						r.moveForward(10*steps);
						instructions.add("F"+10*steps);
						result= computeForwardLocation(direction, x, y, steps);
						movedCount = movedCount+10*steps;
						//message=getNextWallHugLocation(result[0],result[1],direction,instructions);
	
					}else{
						testTurnedRight=true;
						float degreeToMove =rotateToDirection(direction,robotsEast);
						direction = robotsEast;
						int intDegree = Math.round(degreeToMove);
						String rmovement= "R"+intDegree;
						if(intDegree<0){
							rmovement= "L"+(intDegree*-1);
						}
						instructions.add(rmovement);
						//message=getNextWallHugLocation(result[0],result[1],robotsEast,instructions);
					}
				}
			}
			String []movements = new String[instructions.size()];
			int index=0;
			for(String instruction: instructions){
					movements[index] = instruction;
					index++;
			}
			message  = new Message();
			message.setMovements(movements);
			message.setRobotLocation(result);
			message.setEndOfExploration(false);
			message.setDirection(direction);
			return message;
		}else{
			message  = new Message();
			message.setMovements(null);
			message.setRobotLocation(endResult);
			message.setEndOfExploration(false);
			message.setDirection(direction);
			return message;
		}
	}
	
	/*
	private Message moveToLocation1(int x1, int y1,float facing, int x2, int y2, int endDirection) {
	
		
		Vertex s = m.getVertices()[y1][x1];
		if(s==null){
			////System.out.println("Error:"+x1+","+y1);
			////System.out.println("Error:"+x1+","+y1);
			return null;
		}
		Vertex[][] vertices =  m.getVertices();
		Vertex e =vertices[y2][x2];
		DijkstraMinimunRotation d = new DijkstraMinimunRotation();
		java.util.List<Vertex> path = d.computePaths(s, e,vertices);

		ArrayList<String> instructions = new ArrayList<String>();
		if(newVisit){
			newVisit = false;
			float degree = getDegreeBetweenTwoPoint(x1,y1,path.get(1).x,path.get(1).y);
			////System.out.println(x1+","+y1+"=>"+","+path.get(1)+" - "+degree+" deg  <-->"+newVisitDirection);
			if(degree!=facing){
				float degreeToMove = rotateToDirection(facing,newVisitDirection);
				facing= newVisitDirection;
				int intDegree = Math.round(degreeToMove);
				String rmovement= "R"+intDegree;
				if(intDegree<0){
					rmovement= "L"+(intDegree*-1);
				}
				instructions.add(rmovement);
			}
		}
		float direction = facing;
		
		int forwardCount = 0;
		for(int i =0;i<path.size()-1;i++){
			Vertex v1 =path.get(i);
			Vertex v2 =path.get(i+1);
			float degree = getDegreeBetweenTwoPoint(v1.x,v1.y,v2.x,v2.y);
			if(degree!= direction)
			{
				if(forwardCount!=0){
					instructions.add("F"+forwardCount);
					//for calibration
					//distanceTraveledAfterCalibrate=distanceTraveledAfterCalibrate+forwardCount;
					r.moveForward(forwardCount);
				}
				forwardCount=0;
				//float degreeBetween= degreeToRotateToDirection(direction,degree);
				float degreeToMove =rotateToDirection(direction,degree);
				int intDegree = Math.round(degreeToMove);
				String rmovement= "R"+intDegree;
				if(intDegree<0){
					rmovement= "L"+(intDegree*-1);
				}
				instructions.add(rmovement);
				direction = degree;
			}
			forwardCount=forwardCount+10;
			if(i==path.size()-2){
				instructions.add("F"+forwardCount);
				//for calibration
				//distanceTraveledAfterCalibrate=distanceTraveledAfterCalibrate+forwardCount;
				r.moveForward(forwardCount);
			}
		}
		if(direction!=endDirection){
			float degreeToMove =rotateToDirection(direction,endDirection);
			int intDegree = Math.round(degreeToMove);
			String rmovement= "R"+intDegree;
			if(intDegree<0){
				rmovement= "L"+(intDegree*-1);
			}
			instructions.add(rmovement);
			direction = endDirection;
		}

		
		String []movements = new String[instructions.size()];
		int index=0;
		for(String instruction: instructions){
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
	//get Instructions To Location
	private Message moveToLocation2(int x1, int y1,float facing, int x2, int y2) {
		System.out.println(x1+","+y1+" facing: "+facing);
		
		ArrayList<String> instructions = new ArrayList<String>();
		Vertex s = m.getVertices()[y1][x1];
		if(s==null){
			////System.out.println("Error:"+x1+","+y1);
			////System.out.println("Error:"+x1+","+y1);
			return null;
		}
		Vertex[][] vertices =  m.getVertices();
		Vertex e =vertices[y2][x2];
		float direction = facing;
		DijkstraMinimunRotation d = new DijkstraMinimunRotation();
		java.util.List<Vertex> path = d.computePaths(s, e,vertices);
		////System.out.println("Path to travel: "+path);
		////System.out.println("I am facing "+direction);
		int forwardCount = 0;
		for(int i =0;i<path.size()-1;i++){
			Vertex v1 =path.get(i);
			Vertex v2 =path.get(i+1);
			float degree = getDegreeBetweenTwoPoint(v1.x,v1.y,v2.x,v2.y);
			if(degree!= direction)
			{
				if(forwardCount!=0){
					instructions.add("F"+forwardCount);
					//for calibration
					//distanceTraveledAfterCalibrate=distanceTraveledAfterCalibrate+forwardCount;
					r.moveForward(forwardCount);
				}
				forwardCount=0;
				//float degreeBetween= degreeToRotateToDirection(direction,degree);
				float degreeToMove =rotateToDirection(direction,degree);
				int intDegree = Math.round(degreeToMove);
				String rmovement= "R"+intDegree;
				if(intDegree<0){
					rmovement= "L"+(intDegree*-1);
				}
				instructions.add(rmovement);
				direction = degree;
			}
			forwardCount=forwardCount+10;
			if(i==path.size()-2){
				instructions.add("F"+forwardCount);
				//for calibration
				//distanceTraveledAfterCalibrate=distanceTraveledAfterCalibrate+forwardCount;
				r.moveForward(forwardCount);
			}
		}
		
		String []movements = new String[instructions.size()];
		int index=0;
		for(String instruction: instructions){
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
	*/


	private int[] getBestNextStop(int inX, int inY, int maxHop) {

		int currentX = Math.round(r.getPosX());
		int currentY =Math.round(r.getPosY());
		float direction = r.getDirection();
		ArrayList<Position> canExplore= whatTileCanBeDiscovered(currentX,currentY,false);
		float degree = getDegreeBetweenTwoPoint(currentX,currentY,currentX,currentY);
		int degreeBetween =0;
		//if(currentX!=1 || currentY!=1){
			 degreeBetween = (int) degreeToRotateToDirection(direction,degree);
		//}
		int result[] = {currentX,currentY,canExplore.size(),0};
		return getBestNextStopRecursive(inX,  inY,  maxHop, result);
	}

	//BFS, check all nodes
	private  int checkedVisited[][]= new int[20][15];
	private int[] getBestNextStopRecursive(int inX, int inY, int maxHop, int[] inDefault) {
		int currentX = (int) r.getPosX();
		int currentY = (int) r.getPosY();
		Vertex v = m.getVertices()[inY][inX];
		if(v==null){
			return inDefault;
		}
		ArrayList<Vertex> neighbours = v.adjacencies;
		int []optimised=inDefault;
		for(Vertex nextNode: neighbours){
			int x = (int) nextNode.x;
			int y = (int) nextNode.y;
			if(checkedVisited[y][x]==0){
				checkedVisited[y][x]=1;
					ArrayList<Position> canExplore= whatTileCanBeDiscovered(x,y,false);
					if(canExplore.size()>0&&canExplore.size()<=3){
						canExplore =whatTileCanBeDiscovered(x,y,true);
					}
					int squareAway =  getDistanceAway(currentX,currentY,x,y);
					if(squareAway<0){
						//System.out.println("alert");
					}
					int result[] = {x,y,canExplore.size(),squareAway};
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
			float endLocationWeightage = 1;  //score: 1-28
			float startWeightage = 0;        //score: 1-28
			int distanceWeightage = 1;
			
			boolean isAllPossibleNodeVisited = allPossibleNodeVisited();
			//if end point found
			if(m.getExploredTiles()[18][13]==1||isAllPossibleNodeVisited){
				endLocationWeightage=0;
				//if map discovered rate 90% or more
				if(mapDiscoveredRate>0.7){
					startWeightage = 0;
					exploreMoreWeightage=1;
					distanceWeightage=1;
				}
				//if map discovered rate is 100%
				if(mapDiscoveredRate>=getAutoTerminate_explore_rate()||isAllPossibleNodeVisited){
					startWeightage = 1000000;
					exploreMoreWeightage=0;
					distanceWeightage=0;
					terminate();
				}
			}
			//check termiation or timeout
			long currentTimeStamp = System.currentTimeMillis();
	    	long seconds = ((currentTimeStamp-Robot.getInstance().getExploringStartTime())/1000);
			if(seconds>=getAutoTerminate_time()||isOkToTerminate()||mapDiscoveredRate>=getAutoTerminate_explore_rate()){
				startWeightage = 1000000;
				exploreMoreWeightage=0;
				distanceWeightage=0;
				terminate();
			}
			
			/*
			int x1 = place1[0];
			int y1 = place1[1];
			int x2 = place2[0];
			int y2 = place2[1];
			
			//compare to find mutual undiscovered tiles
			ArrayList<Position> canExplore1 =whatTileCanBeDiscovered(x1,y1,false);
			ArrayList<Position> canExplore2 =whatTileCanBeDiscovered(x2,y2,false);
			float totalCount = (canExplore1.size()<canExplore2.size())?canExplore1.size():canExplore2.size();
			float similarCount = 0;
			if(totalCount>0){
				boolean breakAll=false;
				for(Position s1: canExplore1){
					for(Position s2: canExplore2){
						if(s1.equals(s2)){
							similarCount++;
							if((similarCount/totalCount)>0.5){
								 distanceWeightage=0;
								 breakAll=true;
								 break;
							}
						}
					}
					if(breakAll){
						 break;
					}
				}
			}
			//if mutually exclusive then visit the nearer one
			if((totalCount!=0&&(similarCount==0))){
				distanceWeightage=10;
				exploreMoreWeightage=0;
			}
			*/
			//get score of 2 location and compare
			float score1=0;
			float score2=0;
			score1=calculateScore( place1,  distanceWeightage, startWeightage,  nearByWeightage,  endLocationWeightage,  exploreMoreWeightage);
			score2=calculateScore( place2,  distanceWeightage, startWeightage,  nearByWeightage,  endLocationWeightage,  exploreMoreWeightage);
			
			if(startWeightage>0){
				if(place1[0]==1&&place1[1]==1){
					score2=0;
					score1=1;
				}
				if(place2[0]==1&&place2[1]==1){
					score1=0;
					score2=1;
				}
			}else{
				if(score1==score2){
					if(getStartingX()<=6){
						if(place1[0]>place2[0]){
							score1=0;
							score2=1;
						}else{
							score1=1;
							score2=0;
						}
					}else{
						if(place1[0]<place2[0]){
							score1=0;
							score2=1;
						}else{
							score1=1;
							score2=0;
						}
					}
				}
			}
			
			
			//if(place1[2]!=0)
				//////System.out.println("\t ("+(place1[0])+","+place1[1]+")"+"- score:" +score1 +" \ttotal explorable:"+ place1[2]+" \t distance:"+ place1[3]);
			//if(place2[2]!=0)
				//////System.out.println("\t ("+(place2[0])+","+place2[1]+")"+"- score:" +score2+" \ttotal explorable:"+ place2[2]+" \t distance:"+ place1[3]);
				
			int []result;
			if(score1>score2){
				result = place1;
			}else{
				if(score1==score2){
					result = place1;
				}else{
					result = place2;
				}
			}
			
			return result;
		}
	
	int visitedTemp [][] = new int[20][15];
	private int getTotalUnexploredTileConnected(Position pos){
		visitedTemp = new int[20][15];
		visitedTemp[pos.getPosY()][pos.getPosX()]=1;
		int x = pos.getPosX();
		int y = pos.getPosY();
		return getTotalUnexploredTileConnected(x,y, 1);
	}
	private int getTotalUnexploredTileConnected(int x, int y, int count){
		int xTop = x;
		int yTop = y+1;
		int xBottom = x;
		int yBottom = y-1;
		int xLeft = x-1;
		int yLeft = y;
		int xRight = x+1;
		int yRight = y;

		if(xTop>=0&&xTop<15&&yTop>=0&&yTop<20&&m.getExploredTiles()[yTop][xTop]==0&&visitedTemp[yTop][xTop]!=1){
			visitedTemp[yTop][xTop]=1;
			count = getTotalUnexploredTileConnected(xTop, yTop ,count+1);
		}
		if(xBottom>=0&&xBottom<15&&yBottom>=0&&yBottom<20&&m.getExploredTiles()[yBottom][xBottom]==0&&visitedTemp[yBottom][xBottom]!=1){
			visitedTemp[yBottom][xBottom]=1;
			count = getTotalUnexploredTileConnected(xBottom, yBottom ,count+1);
		}
		if(xLeft>=0&&xLeft<15&&yLeft>=0&&yLeft<20&&m.getExploredTiles()[yLeft][xLeft]==0&&visitedTemp[yLeft][xLeft]!=1){
			visitedTemp[yLeft][xLeft]=1;
			count = getTotalUnexploredTileConnected(xLeft, yLeft ,count+1);
		}
		if(xRight>=0&&xRight<15&&yRight>=0&&yRight<20&&m.getExploredTiles()[yRight][xRight]==0&&visitedTemp[yRight][xRight]!=1){
			visitedTemp[yRight][xRight]=1;
			count = getTotalUnexploredTileConnected(xRight, yRight ,count+1);
		}
		return count;
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
			int distanceAway2 = 300-(place2[3]);
			int squareAway2 = 29-(Math.abs(place2[0]-endLocation[0])+Math.abs(place2[1]-endLocation[1]));
			int nearBySquareAway2 = (int) (29-(Math.abs(place2[0]-r.getPosX())+Math.abs(place2[1]-r.getPosY())));
			int startSquareAway2 = (int) (29-(Math.abs(place2[0]-startLocation[1])+Math.abs(place2[1]-startLocation[1])));
			
			if(exploreSquareCount2==0){
				score=0;
			}else{
				score =(distanceAway2*distanceWeightage)+ (startSquareAway2)*startWeightage +(nearBySquareAway2)*nearByWeightage + (squareAway2)*endLocationWeightage 	+ 	exploreSquareCount2*exploreMoreWeightage;
			}
		}
		return score;
	}
	
	
	
	private int getDistanceAway(int x1, int y1, int x2, int y2) {
		Vertex[][]vertices = m.getVertices();
		Vertex v1 = vertices[y1][x1];
		Vertex v2 = vertices[y2][x2];
		java.util.List<Vertex> path = null;
		if(v1!=null&&v2!=null){
			DijkstraMinimunRotation d = new DijkstraMinimunRotation();
			path=d.computePaths(v1, v2,vertices);
		}
		if(path!=null&&path.size()>0){
			return path.size()-1;
		}
		return  Math.abs(x1-x2)+Math.abs(y1-y2);
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

	//update the visited list
	private boolean allPossibleNodeVisited(){
		for(int x1=1;x1<14;x1++){
			for(int y1=1;y1<19;y1++){
				if(visited[y1][x1]==0){
					Vertex v = m.getVertices()[y1][x1];
					if(v!=null){
						return false;
					}
				}
			}
		}
		return true;
	}
	
	//get number of tiles that are unexplored in a location
	private ArrayList<Position> whatTileCanBeDiscovered(int x, int y, boolean firstLayer){
		ArrayList<Position> arrays=new ArrayList<Position>();
		float[] allDirection = {NORTH,SOUTH,EAST,WEST};
		for(float dir: allDirection){
			RobotSensorSimulatorType2 r = new RobotSensorSimulatorType2();
			Position[][] lineOfSensors = r.getLineOfSensor(x, y, dir);
			for(int i =0;i<3;i++){
				Position[] sensors = lineOfSensors[i];
				for(Position sensor:sensors){
					int count = 0;
					if(sensor!=null&&sensor.getPosY()>=0&&(sensor.getPosY())<20&&(sensor.getPosX())>=0&&(sensor.getPosX())<15){
						if(m.getExploredTiles()[sensor.getPosY()][(sensor.getPosX())]==0){
							arrays.add(new Position(sensor.getPosX(),sensor.getPosY()));
							count++;
						}else{
							if(m.getObstacles()[sensor.getPosY()][(sensor.getPosX())]==1){
								break;
							}
						}
					}else{
						break;
					}
					if(firstLayer&&count==1){
						break;
					}
				}
			}
		}
		return arrays;
	}
	

	public int isDirectionMoveable(float direction, int x, int y){
		int[][]obstacles = m.getObstacles();
		int[][]explored = m.getExploredTiles();
		int maxMoveable = 10;
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
	public int getLeftBlocks(float direction, int x, int y){
		int[][]obstacles = m.getObstacles();
		int[][]explored = m.getExploredTiles();
		int dir = (int)direction;
		switch (dir){
		case 0: 
			if(x-2<0){
				return 3;
			}
			if(explored[y+1][x-2]==0||(explored[y+1][x-2]==1&&obstacles[y+1][x-2]==1)){
				return 3;
			}
			if(explored[y][x-2]==0||(explored[y][x-2]==1&&obstacles[y][x-2]==1)){
				return 2;
			}
			if(explored[y-1][x-2]==0||(explored[y-1][x-2]==1&&obstacles[y-1][x-2]==1)){
				return 1;
			}
			break;
		case 90: 
			if(y+2>=20){
				return 3;
			}
			if(explored[y+2][x+1]==0||(explored[y+2][x+1]==1&&obstacles[y+2][x+1]==1)){
				return 3;
			}
			if(explored[y+2][x]==0||(explored[y+2][x]==1&&obstacles[y+2][x]==1)){
				return 2;
			}
			if(explored[y+2][x-1]==0||(explored[y+2][x-1]==1&&obstacles[y+2][x-1]==1)){
				return 1;
			}
			break;
		case 180: 
			if(x+2>=15){
				return 3;
			}
			if(explored[y-1][x+2]==0||(explored[y-1][x+2]==1&&obstacles[y-1][x+2]==1)){
				return 3;
			}
			if(explored[y][x+2]==0||(explored[y][x+2]==1&&obstacles[y][x+2]==1)){
				return 2;
			}
			if(explored[y+1][x+2]==0||(explored[y+1][x+2]==1&&obstacles[y+1][x+2]==1)){
				return 1;
			}
			break;
		case 270: 
			if(y-2<0){
				return 3;
			}
			if(explored[y-2][x-1]==0||(explored[y-2][x-1]==1&&obstacles[y-2][x-1]==1)){
				return 3;
			}
			if(explored[y-2][x]==0||(explored[y-2][x]==1&&obstacles[y-2][x]==1)){
				return 2;
			}
			if(explored[y-2][x+1]==0||(explored[y-2][x+1]==1&&obstacles[y-2][x+1]==1)){
				return 1;
			}
			break;
		}
		return 3;
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


	@Override
	public String geType() {
		return ExplorationFactory.EX_WALL1;
	}
}
