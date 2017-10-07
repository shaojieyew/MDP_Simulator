package algorithm;


import java.awt.List;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
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
import Data.WayPoint;
import GUI.MapGUI;
import RPiInterface.Message;

public class ExplorationWallerType4 extends Exploration {
	public  int [][] visited = new int[20][15];
	public  float checkEnvironementOf[];
	int printCount=1;
	public static float bufferArea = 1.2f;

	public static boolean testTurnedLeft =false;
	public static boolean testTurnedRight =false;
	public static boolean newVisit =false;
	public static float newVisitDirection =0;
	public static boolean finishHuggingWall =false;
	
	public void cleanUpVar(){
		 testTurnedLeft =false;
		 testTurnedRight =false;
		 newVisit =false;
		 newVisitDirection =0;
		 finishHuggingWall =false;
	}
	
	//from rpi
	public  ExplorationWallerType4(boolean isTerminate){
		super(isTerminate);
	}
	
	//from simulator
	public ExplorationWallerType4(){
		super();
	}
	public ExplorationWallerType4(int startAtX, int startAtY){
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
		//set goal to termination/start point
    	if(seconds>=getAutoTerminate_time()||isOkToTerminate()||mapDiscoveredRate>=getAutoTerminate_explore_rate()){
			finishHuggingWall=true;
			terminate();
		}
		
		Message message = null;
		int currentX = Math.round(r.getPosX());
		int currentY =Math.round(r.getPosY());
		float direction = r.getDirection();
		int result[] ;
		updateVisitedList();
		//if startpoint is not explored or goal is not startpoint/termination, continue to explore nearby blocks 
		if(!isOkToTerminate()||!startPointFound()){
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
			float tempDirection = direction;
			if(newVisit){
				 tempDirection= (int) newVisitDirection;
			}
			result = getNextWallHugLocation(currentX,currentY,(int)tempDirection);
			if(result[0]==1&&result[1]==1){
				finishHuggingWall = true;
			}

			if(!finishHuggingWall){
				if(result[0]==1&&result[1]==1&&isOkToTerminate()){
					message = moveToLocation(currentX, currentY, direction, result[0],result[1],0);
					message.setEndOfExploration(true);
					destroy();
				}else{
					message = moveToLocation(currentX, currentY, direction, result[0],result[1],result[2]);
					
				}
				return message;
			}
		}

		if(finishHuggingWall){
			checkedVisited =new int[20][15];
			result  = getBestNextStop(currentX,currentY,10000);
			if(currentX==result[0]&&currentY==result[1]&&(currentX!=1||currentY!=1)){
				result[0]=1;
				result[1]=1;
			}
			if(result[0]==1&&result[1]==1&&isOkToTerminate()){
				message = moveToLocation(currentX, currentY, direction, result[0],result[1],0);
				message.setEndOfExploration(true);
				cleanUpVar();
				destroy();
			}else{

				message = moveToLocation(currentX, currentY, direction, result[0],result[1]);
			}
			return message ;
		}

		return message;
	}
	public boolean allPossiblePathExplored(int x,int y){
		int visitedCheck [][] = new int[20][15];
		Vertex v = m.getVertices()[y][x];
		// BFS uses Queue data structure
				Queue queue = new LinkedList();
				queue.add(v);
				visitedCheck[y][x] = 1;
				while(!queue.isEmpty()) {
					Vertex node = (Vertex)queue.remove();
					if(visited[(int) node.getY()][(int) node.getX()]==0){
						return false;
					}
					Vertex child=null;
					if(node.adjacencies.size()>0)
					for(int i =0;i<node.adjacencies.size();i++){
						child = node.adjacencies.get(i);
						if(visitedCheck[(int) child.getY()][(int) child.getX()]==0){
							visitedCheck[(int) child.getY()][(int) child.getX()]=1;
							queue.add(child);
						}
					}
				}
		return true;
	}
	@Override
	public void updateMap(){

		computeAction();
		
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

	
	private int[] getNextWallHugLocation( int x, int y,int direction){
		direction=direction%360;
		if(direction<=92&&direction>=88){
			direction = 90;
		}
		if(direction<=2||direction>=358){
			direction = 0;
		}
		if(direction<=182&&direction>=178){
			direction = 180;
		}
		if(direction<=272&&direction>=268){
			direction = 270;
		}
		int robotsNorth = (int) ((NORTH+direction)%360);
		int robotsEast = (int) ((EAST+direction)%360);
		int robotsWest = (int) ((WEST+direction)%360);
		int nMoveable = isDirectionMoveable(robotsNorth, x, y);
		int wMoveable = isDirectionMoveable(robotsWest, x, y);
		int previousBlocked = getLeftBlocks(direction,x, y);
		int [] result = {x,y,direction};
		if(visited[y][x]==0||(x==1 && y==1&&endPointFound())){
			return result;
		}
		if(allPossiblePathExplored(x,y)){
			int [] result1 = {1,1,0};
			return result1;
		}
		int steps = (nMoveable<previousBlocked)?nMoveable:previousBlocked;
		if(testTurnedLeft&&nMoveable!=0){
			testTurnedLeft=false;
			result= computeForwardLocation(direction, x, y, steps);
			result=getNextWallHugLocation(result[0],result[1],direction);
		}else{
			if(wMoveable!=0){
				testTurnedLeft=true;
				result=getNextWallHugLocation(result[0],result[1],robotsWest);
			}else{
				if(steps!=0){
					if(testTurnedRight){
						testTurnedRight=false;
					}
					result= computeForwardLocation(direction, x, y, steps);
					result=getNextWallHugLocation(result[0],result[1],direction);
				}else{
					testTurnedRight=true;
					result=getNextWallHugLocation(result[0],result[1],robotsEast);
				}
			}
		}
		return result;
	}
	
	private int[] computeForwardLocation(int direction, int x, int y, int steps){
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
	
	private boolean startPointFound() {
		for(int x =0;x<3;x++){
			for(int y =0;y<3;y++){
				if(m.getExploredTiles()[y][x]!=1){
					return false;
				}
			}
		}
		return true;
	}

	private boolean endPointFound() {
		for(int x =12;x<15;x++){
			for(int y =17;y<20;y++){
				if(m.getExploredTiles()[y][x]!=1){
					return false;
				}
			}
		}
		return true;
	}

	private Message moveToLocation(int x1, int y1,float facing, int x2, int y2, int endDirection) {

		Vertex s = m.getVertices()[y1][x1];
		if(s==null){
			//System.out.println("Error:"+x1+","+y1);
			//System.out.println("Error:"+x1+","+y1);
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
			System.out.println(x1+","+y1+"=>"+","+path.get(1)+" - "+degree+" deg  <-->"+newVisitDirection);
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
		
		return fastestPath(x1,y1,x2,y2,direction, endDirection);
	}
	//get Instructions To Location
	private Message moveToLocation(int x1, int y1,float facing, int x2, int y2) {
		ArrayList<String> instructions = new ArrayList<String>();
		Vertex s = m.getVertices()[y1][x1];
		if(s==null){
			//System.out.println("Error:"+x1+","+y1);
			//System.out.println("Error:"+x1+","+y1);
			return null;
		}
		Vertex[][] vertices =  m.getVertices();
		Vertex e =vertices[y2][x2];
		float direction = facing;
		return fastestPath(x1,y1,x2,y2,direction,0);
	}

	private Message fastestPath(float startX, float startY, float endX, float endtY, float startDirection, float endDirection){

		ArrayList<ArrayList<Vertex>> outOfBound= updateOutofBound();
		MapGUI map = MapGUI.getInstance();
		//check if start and end path is cleared
		Vertex start = new Vertex(startX+0.5f,startY+0.5f);
		Vertex end = new Vertex(endX+0.5f,endtY+0.5f);
		float direction = startDirection;
		
		Position wp = WayPoint.getInstance().getPosition();
		Vertex waypoint;
		if(wp!=null){
			waypoint = new Vertex(wp.getPosX()+0.5f,wp.getPosY()+0.5f);
		}else{
			waypoint = end;
		}
		if(FastestPathUtil.isClearPath(start,waypoint,outOfBound)&&FastestPathUtil.isClearPath(waypoint,end,outOfBound)){
			start.adjacencies.add(waypoint);
			end.adjacencies.add(waypoint);
			waypoint.adjacencies.add(start);
			waypoint.adjacencies.add(end);
		}else{
			//setup links
			for(ArrayList<Vertex> boundary1 : outOfBound){
				for(Vertex v1 : boundary1){
					for(ArrayList<Vertex> boundary2 : outOfBound){
						for(Vertex v2 : boundary2){
							if(v1.x!=v2.x||v1.y!=v2.y){
								if(FastestPathUtil.isClearPath(v1,v2,outOfBound)){
									v1.adjacencies.add(v2);
								}
							}
						}
					}
				}
			}  
			
			for(ArrayList<Vertex> boundary2 : outOfBound){
				for(Vertex v2 : boundary2){
					if(start.x!=v2.x||start.y!=v2.y){
						if(FastestPathUtil.isClearPath(start,v2,outOfBound)){
							start.adjacencies.add(v2);
							v2.adjacencies.add(start);
						}
					}
					if(waypoint.x!=v2.x||waypoint.y!=v2.y){
						if(FastestPathUtil.isClearPath(waypoint,v2,outOfBound)){
							waypoint.adjacencies.add(v2);
							v2.adjacencies.add(waypoint);
						}
					}
					if(end.x!=v2.x||end.y!=v2.y){
						if(FastestPathUtil.isClearPath(end,v2,outOfBound)){
							end.adjacencies.add(v2);
							v2.adjacencies.add(end);
						}
					}
				}
			}
		}
		if(FastestPathUtil.isClearPath(start,waypoint,outOfBound)){
			start.adjacencies.add(waypoint);
			waypoint.adjacencies.add(start);
		}
		if(FastestPathUtil.isClearPath(waypoint,end,outOfBound)){
			end.adjacencies.add(waypoint);
			waypoint.adjacencies.add(end);
		}
		
		
		
		ArrayList<Vertex> temp = new ArrayList<Vertex>();
		temp.add(start);
		temp.add(waypoint);
		temp.add(end);

		ArrayList<ArrayList<Vertex>> allVertices = new ArrayList<ArrayList<Vertex>>();
		allVertices.addAll(outOfBound);
		
		DijkstraDistanceWeighted d = new DijkstraDistanceWeighted();
		java.util.List<Vertex> path = d.computePaths(start, waypoint,allVertices);
		if(end.x!=waypoint.x||end.y!=waypoint.y){
			java.util.List<Vertex> path2 = d.computePaths(waypoint, end,allVertices);
			path.remove(path.size()-1);
			path.addAll(path2);
		}
		System.out.println("Path : "+path);

		float forwardCount = 0;
		ArrayList<String> instructions = new ArrayList<String>();
		for(int i =0;i<path.size()-1;i++){
			Vertex v1 =path.get(i);
			Vertex v2 =path.get(i+1);
			float degree = getDegreeBetweenTwoPoint(v1.x,v1.y,v2.x,v2.y);
			if(degree!= direction)
			{
				int forwardDist = Math.round(forwardCount);
				if(forwardDist!=0){
					instructions.add("F"+forwardDist);
					r.moveForward(forwardDist);
				}
				forwardCount=0;
				
				float degreeToMove = rotateToDirection(direction,degree);
				int intDegree = Math.round(degreeToMove);
				String movement= "R"+intDegree;
				if(intDegree<0){
					movement= "L"+(intDegree*-1);
				}
				if(intDegree!=0){
					instructions.add(movement);
					direction = degree;
				}
			}

			float distance = (float) Math.hypot(v1.x-v2.x, v1.y-v2.y);
			forwardCount=forwardCount+(10f*distance);
			if(i==path.size()-2){
				int forwardDist = Math.round(forwardCount);
				instructions.add("F"+forwardDist);
				r.moveForward(forwardDist);
			}
		}
		
		if(direction!=endDirection){
			float degreeToMove = rotateToDirection(direction,endDirection);
			int intDegree = Math.round(degreeToMove);
			String movement= "R"+intDegree;
			if(intDegree<0){
				movement= "L"+(intDegree*-1);
			}
			if(intDegree!=0){
				instructions.add(movement);
				direction = endDirection;
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

	//check if the position and direction have any undiscovered tiles

	private float rotateToDirection(float currentDirection, float inDirection){
		////System.out.println("rotate from "+currentDirection+" to "+inDirection);
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
	
	//get degree between 2 point
	private float getDegreeBetweenTwoPoint(float x,float y,float x2, float y2){
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
			Position[][] lineOfSensors = r.getSensorSimulator().getLineOfSensor(x, y, dir);
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
	public ArrayList<ArrayList<Vertex>> updateOutofBound(){
		int obstacles[][] = m.getObstacles();
		int exploredTiles[][] = m.getExploredTiles();
	    Area areas = new Area();
		
		//create obstacles
		for(int x =0;x<15;x++){
			for(int y =0;y<20;y++){
				if(exploredTiles[y][x]==0||obstacles[y][x]==1){
					//ArrayList<Polygon> temp = new ArrayList<Polygon>();
					Polygon poly = getPolygonOfAnObstacle(x,y);
				    Area a1 = new Area(poly);
					areas.add(a1);
				}
			}
		}
		Polygon walls = new Polygon();
		walls.addPoint((int)((0+bufferArea)*100),(int) ((0.01+bufferArea)*100)); //1,1
		walls.addPoint((int)((0-bufferArea)*100),(int) ((0.01+bufferArea)*100)); //-1,1
		walls.addPoint((int)((0-(bufferArea))*100),(int) ((20+bufferArea)*100));//-1,21
		walls.addPoint((int)((15+(bufferArea))*100),(int) ((20+bufferArea)*100)); //16,21
		walls.addPoint((int)((15+(bufferArea))*100),(int) ((0-bufferArea)*100)); //16,-1
		walls.addPoint((int)((0-(bufferArea))*100),(int) ((0-bufferArea)*100)); //-1,-1
		walls.addPoint((int)((0-bufferArea)*100),(int) ((0+bufferArea)*100)); //-1,1
		walls.addPoint((int)((15-(bufferArea))*100),(int) ((0+bufferArea)*100)); //14,1
		walls.addPoint((int)((15-(bufferArea))*100),(int) ((20-bufferArea)*100)); //14,19
		walls.addPoint((int)((0+(bufferArea))*100),(int) ((20-bufferArea)*100)); //1,19
		areas.add(new Area(walls));
		return  FastestPathUtil.areaToPolygons(areas);
	}
	
	private  Polygon getPolygonOfAnObstacle(int x, int y){
		Polygon r1 = new Polygon();
		r1.addPoint((int)((x-bufferArea)*100),(int) ((y-bufferArea)*100));
		r1.addPoint((int)((x+1f+bufferArea)*100), (int)((y-bufferArea)*100));
		r1.addPoint((int)((x+1f+bufferArea)*100), (int)((y+1f+bufferArea)*100));
		r1.addPoint((int)((x-bufferArea)*100), (int)((y+1f+bufferArea)*100));
		return r1;
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
		return ExplorationFactory.EX_WALL4;
	}
}
