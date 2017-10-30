package algorithm;

import java.util.ArrayList;
import java.util.Arrays;

import Data.Map;
import Data.MapListener;
import Data.Position;
import Data.Robot;
import Data.RobotListener;
import Data.Vertex;
import Data.WayPoint;
import RPiInterface.Message;
import RPiInterface.RobotSensorSimulatorFactory;

public abstract class Exploration implements  MapListener, RobotListener{
	private static long autoTerminate_time=270;
	public static int autoTerminate_explore_rate=300;

	public static boolean  arduinoAutoCalibrate = true;

	public static int lastMovedBeforeCalibrate = 0;
	public static int intervalForCalibrate = 40;
	public static int rotationCost = 0;
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
	public void updateMap(){
		Message m = computeAction();
		System.out.println(Arrays.toString(m.getMovements()));
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
	public static void setAutoTerminate_explore_rate(int autoTerminate_explore_rate) {
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
			int index=0;
			for(Position sensor:sensors){
				index++;
				if(index==4){
					break;
				}
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
	protected int howManyUndiscovered(int currentX,int currentY){
		int count=0;
		count = count+howManyUndiscovered(currentX, currentY, 0);
		count = count+howManyUndiscovered(currentX, currentY, 90);
		count = count+howManyUndiscovered(currentX, currentY, 180);
		count = count+howManyUndiscovered(currentX, currentY, 270);
		return count;
	}
	protected int howManyUndiscovered(int currentX,int currentY, float inDirection){
		int count=0;
		Position[][] lineOfSensors = r.getSensorSimulator().getLineOfSensor(currentX, currentY, inDirection);
		boolean thereExistUndiscovered = false;
		for(Position[] sensors:lineOfSensors){
			if(thereExistUndiscovered)
				break;

			int index = 0;
			for(Position sensor:sensors){
				index++;
				if(index>3){
					break;
				}
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

	public static boolean outofWallhugging(int x, int y, int direction){
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
	
	
	public static ArrayList<String>  addCalibrationCommand(int x, int y, int direction, ArrayList<String> instruction, String nextInstruction){
		int bestDirection = direction;
		int bestCount = getTotalSideForCalibration(x,y,direction);

		int countW = getTotalSideForCalibration(x,y,(direction+270)%360);
	//	if(countW>bestCount&&((x==1&&y==1)||(bestCount==0))){
		if(countW>bestCount){
			bestDirection = (direction+270)%360;
			bestCount = countW;
		}
		int countE = getTotalSideForCalibration(x,y,(direction+90)%360);
		if(countE>bestCount){
			bestDirection = (direction+90)%360;
			bestCount = countE;
		}
		
		int countS = getTotalSideForCalibration(x,y,(direction+180)%360);
		if(countS>bestCount&&bestCount==0){
			bestDirection = (direction+180)%360;
			bestCount = countS;
		}
		String prev = "";
		if(bestCount>0){
			float degree = degreeToRotateToDirection(direction,  bestDirection);
			int intDegree = Math.round(degree);
			if(intDegree!=0){
				String rmovement= "R"+intDegree;
				if(intDegree<0){
					rmovement= "L"+(intDegree*-1);
				}
				prev= rmovement;
			}
			
			if(nextInstruction==null||(nextInstruction!=null&&(nextInstruction.charAt(0)=='F'))||(nextInstruction!=null&&(nextInstruction.charAt(0)=='R'||nextInstruction.charAt(0)=='L'))){
				float degreeToMove =rotateToDirection(direction,bestDirection);
				intDegree = Math.round(degreeToMove);
				if(intDegree!=0){
					String rmovement= "R"+intDegree;
					if(intDegree<0){
						rmovement= "L"+(intDegree*-1);
					}
					instruction.add(rmovement);
					prev= rmovement;
				}
				
				if(!arduinoAutoCalibrate){
					instruction.add("C");
					r.calibrate();
				}
				Exploration.lastMovedBeforeCalibrate=0;
				
				degreeToMove =rotateToDirection(bestDirection,direction);
				intDegree = Math.round(degreeToMove);
				if(intDegree!=0){
					String rmovement= "R"+intDegree;
					if(intDegree<0){
						rmovement= "L"+(intDegree*-1);
					}
					instruction.add(rmovement);
				}
			}
			
		}
		
		return instruction;
	}
	
	
	public static int getTotalSideForCalibration(int x, int y, int direction){
		int count=0;
		int totalBlocks = 0;
		int obstacles[][] = m.getObstacles();
		int layer = 1;
		int explored[][] = m.getExploredTiles();
		switch (direction){
		case 0:
			/*front sensorcheck*/
			 totalBlocks = 0;
			for(int i=0;i<3;i++){
				if(i==1)
					continue;
				for(int j=0;j<layer;j++){
					int checkX = x-1+i;
					int checkY = y+2+j;
					if(checkY>19||(obstacles[checkY][checkX]==1&&explored[checkY][checkX]==1)){
						totalBlocks++;
						break;
					}
				}
			}
			if(totalBlocks>=2){
				count++;
			}

			/*side sensorcheck*/
			totalBlocks = 0;
			for(int i=0;i<3;i++){
				if(i==1)
					continue;
				for(int j=0;j<layer;j++){
					int checkX = x-2-j;
					int checkY = y-1+i;
					if(checkX<0||(obstacles[checkY][checkX]==1&&explored[checkY][checkX]==1)){
						totalBlocks++;
						break;
					}
				}
			}
			if(totalBlocks>=2){
				count++;
			}
			break;
			
		case 90:
			/*front sensorcheck*/
			totalBlocks = 0;
			for(int i=0;i<3;i++){
				if(i==1)
					continue;
				for(int j=0;j<layer;j++){
					int checkX = x+2+j;
					int checkY = y+1-i;
					if(checkX>14||(obstacles[checkY][checkX]==1&&explored[checkY][checkX]==1)){
						totalBlocks++;
						break;
					}
				}
			}
			if(totalBlocks>=2){
				count++;
			}

			/*side sensorcheck*/
			totalBlocks = 0;
			for(int i=0;i<3;i++){
				if(i==1)
					continue;
				for(int j=0;j<layer;j++){
					int checkX = x-1+i;
					int checkY = y+2+j;
					if(checkY>19||(obstacles[checkY][checkX]==1&&explored[checkY][checkX]==1)){
						totalBlocks++;
						break;
					}
				}
			}
			if(totalBlocks>=2){
				count++;
			}
			break;
			
			
			case 180:
				/*front sensorcheck*/
				totalBlocks = 0;
				for(int i=0;i<3;i++){
					if(i==1)
						continue;
					for(int j=0;j<layer;j++){
						int checkX = x+1-i;
						int checkY = y-2-j;
						if(checkY<0||(obstacles[checkY][checkX]==1&&explored[checkY][checkX]==1)){
							totalBlocks++;
							break;
						}
					}
				}
				if(totalBlocks>=2){
					count++;
				}

				/*side sensorcheck*/
				totalBlocks = 0;
				for(int i=0;i<3;i++){
					if(i==1)
						continue;
					for(int j=0;j<layer;j++){
						int checkX = x+2+j;
						int checkY = y+1-i;
						if(checkX>14||(obstacles[checkY][checkX]==1&&explored[checkY][checkX]==1)){
							totalBlocks++;
							break;
						}
					}
				}
				if(totalBlocks>=2){
					count++;
				}
				break;

				
				
			case 270:
				/*front sensorcheck*/
				totalBlocks = 0;
				for(int i=0;i<3;i++){
					if(i==1)
						continue;
					for(int j=0;j<layer;j++){
						int checkX = x-2-j;
						int checkY = y-1+i;
						if(checkX<0||(obstacles[checkY][checkX]==1&&explored[checkY][checkX]==1)){
							totalBlocks++;
							break;
						}
					}
				}
				if(totalBlocks>=2){
					count++;
				}

				/*side sensorcheck*/
				totalBlocks = 0;
				for(int i=0;i<3;i++){
					if(i==1)
						continue;
					for(int j=0;j<layer;j++){
						int checkX = x+1-i;
						int checkY = y-2-j;
						if(checkY<0||(obstacles[checkY][checkX]==1&&explored[checkY][checkX]==1)){
							totalBlocks++;
							break;
						}
					}
				}
				if(totalBlocks>=2){
					count++;
				}
				break;
		}
		return  count;
	}

	//get degree between 2 point
	public static float getDegreeBetweenTwoPoint(float x,float y,float x2, float y2){
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
	
	public static float rotateToDirection(float currentDirection, float inDirection){
		////System.out.println("rotate from "+currentDirection+" to "+inDirection);
		float degree = degreeToRotateToDirection(currentDirection,  inDirection);
		r.rotate(degree);
		return degree;
	}
	public static float degreeToRotateToDirection(float currentDirection, float inDirection){
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
	
	
	//get Instructions To Location
	public Message moveToLocation(int x1, int y1,float facing, int x2, int y2) {
		
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
		DijkstraMinimunRotation d = new DijkstraMinimunRotation();
		java.util.List<Vertex> path = d.computePaths(s, e,vertices);
		//System.out.println("Path to travel: "+path);
		//System.out.println("I am facing "+direction);
		int forwardCount = 0;
		for(int i =0;i<path.size()-1;i++){
			Vertex v1 =path.get(i);
			Vertex v2 =path.get(i+1);
			float degree = getDegreeBetweenTwoPoint(v1.x,v1.y,v2.x,v2.y);
			if(degree!= direction)
			{
				if(forwardCount!=0){

					instructions.add("F"+forwardCount);
					r.moveForward(forwardCount);
					lastMovedBeforeCalibrate = lastMovedBeforeCalibrate+ forwardCount;
					if(lastMovedBeforeCalibrate>=intervalForCalibrate){
						instructions = addCalibrationCommand((int)v2.x,(int)v2.y,(int) direction,instructions,null);
					}
					forwardCount=0;
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
			if(forwardCount>=intervalForCalibrate){
				instructions.add("F"+forwardCount);
				r.moveForward(forwardCount);
				lastMovedBeforeCalibrate = lastMovedBeforeCalibrate+ forwardCount;
				if(lastMovedBeforeCalibrate>=intervalForCalibrate){
					instructions = addCalibrationCommand((int)v2.x,(int)v2.y,(int) direction,instructions,null);
				}
				forwardCount=0;
			}else{
				if(i==path.size()-2){
					if(forwardCount>0){
						instructions.add("F"+forwardCount);
						r.moveForward(forwardCount);
						lastMovedBeforeCalibrate = lastMovedBeforeCalibrate+ forwardCount;
						if(lastMovedBeforeCalibrate>=intervalForCalibrate){
							instructions = addCalibrationCommand((int)v2.x,(int)v2.y,(int) direction,instructions,null);
						}
						forwardCount=0;
					}
				}
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
	
	
	
	
	public Message moveToLocation(int x1, int y1,float facing, int x2, int y2, int endDirection) {

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
					r.moveForward(forwardCount);
					lastMovedBeforeCalibrate = lastMovedBeforeCalibrate+ forwardCount;
					if(lastMovedBeforeCalibrate>=intervalForCalibrate){
						instructions = addCalibrationCommand((int)v2.x,(int)v2.y,(int) direction,instructions,null);
					}
					forwardCount=0;
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
			if(forwardCount>=intervalForCalibrate){
				instructions.add("F"+forwardCount);
				r.moveForward(forwardCount);
				lastMovedBeforeCalibrate = lastMovedBeforeCalibrate+ forwardCount;
				if(lastMovedBeforeCalibrate>=intervalForCalibrate){
					instructions = addCalibrationCommand((int)v2.x,(int)v2.y,(int) direction,instructions,null);
				}
				forwardCount=0;
			}else{
				if(i==path.size()-2){
					if(forwardCount>0){
						instructions.add("F"+forwardCount);
						r.moveForward(forwardCount);
						lastMovedBeforeCalibrate = lastMovedBeforeCalibrate+ forwardCount;
						if(lastMovedBeforeCalibrate>=intervalForCalibrate){
							instructions = addCalibrationCommand((int)v2.x,(int)v2.y,(int) direction,instructions,null);
						}
						forwardCount=0;
					}
				}
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
	public int[] computeForwardLocation(int direction, int x, int y, int steps){
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
		int result[]={x,y,direction};
		return result;
	}
}
