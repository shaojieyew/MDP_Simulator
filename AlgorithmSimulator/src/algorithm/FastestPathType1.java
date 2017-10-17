package algorithm;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.sun.javafx.geom.Line2D;

import Data.Position;
import Data.Vertex;
import Data.WayPoint;
import GUI.MapGUI;
import RPiInterface.Message;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.awt.Component;
import java.awt.Polygon;
import java.awt.Rectangle;

public class FastestPathType1 extends FastestPath {
	public static ArrayList<Line> debugLine = new  ArrayList<Line>();
	public static boolean debugPath = true;
	public static float bufferArea = 1.1f;
	public static int  lastMovedBeforeCalibrate=0;
	
	@Override
	public Message computeAction() {		
		
		// ***Create the obstacles by taking user input and eliminate the impossible robot positions
		// I have asked for user input, but you already have obstacles
		//int obstacles[][] = m.getObstacles();
		//int explored[][] = m.getExploredTiles();
				
		// Array to add all the possible robotPositions
		// Vertex is nothing but a class with two variables for x and y
		//***checking available robot positions by checking all the 9 positions around
		// Add to the possiblePositions array all the possible robot positions 
		Vertex[][] vertices = m.getVertices();

		
		// Select starting point and remove it from the list
		// Ask input for way point
		//But you already have it somewhere with you. So just need to supply
		Vertex start=vertices[1][1];
		Vertex end=vertices[18][13];
		Vertex waypoint=end;
		Position wp= WayPoint.getInstance().getPosition();
		if(wp!=null){
			waypoint=vertices[wp.getPosY()][wp.getPosX()];
			if(waypoint==null){
				waypoint=end;
			}
		}
		
		DijkstraMinimunRotation d = new DijkstraMinimunRotation();
		List<Vertex> path = d.computePaths(start, waypoint,vertices);
		System.out.println("Path1: "+path);
		if(end.x!=waypoint.x||end.y!=waypoint.y){
			List<Vertex> path2 = d.computePaths(waypoint, end,vertices);
			//System.out.println("Path2: "+path2);
			path.remove(path.size()-1);
			path.addAll(path2);
		}
	
		//System.out.println("Path: "+path);
		float direction = 0;
		int forwardCount = 0;
		ArrayList<String> instructions = new ArrayList<String>();
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
					if(lastMovedBeforeCalibrate>=Exploration.intervalForCalibrate){
						instructions = addCalibrationCommand((int)v2.x,(int)v2.y,(int) direction,instructions);
					}
					forwardCount=0;
				}
				
				float degreeToMove = rotateToDirection(direction,degree);
				int intDegree = Math.round(degreeToMove);
				String movement= "R"+intDegree;
				if(intDegree<0){
					movement= "L"+(intDegree*-1);
				}
				instructions.add(movement);
				lastMovedBeforeCalibrate = lastMovedBeforeCalibrate+ Exploration.rotationCost;
				direction = degree;
			}
			
			forwardCount=forwardCount+10;
			if(forwardCount>=Exploration.intervalForCalibrate){
				instructions.add("F"+forwardCount);
				r.moveForward(forwardCount);
				lastMovedBeforeCalibrate = lastMovedBeforeCalibrate+ forwardCount;
				if(lastMovedBeforeCalibrate>=Exploration.intervalForCalibrate){
					instructions = addCalibrationCommand((int)v2.x,(int)v2.y,(int) direction,instructions);
				}
				forwardCount=0;
			}else{
				if(i==path.size()-2){
					if(forwardCount>0){
						instructions.add("F"+forwardCount);
						r.moveForward(forwardCount);
						lastMovedBeforeCalibrate = lastMovedBeforeCalibrate+ forwardCount;
						if(lastMovedBeforeCalibrate>=Exploration.intervalForCalibrate){
							instructions = addCalibrationCommand((int)v2.x,(int)v2.y,(int) direction,instructions);
						}
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
	
	
	//robot movement duplicated codes from exploration and fastestpath
	private static float rotateToDirection(float currentDirection, float inDirection){
		//System.out.println("rotate from "+currentDirection+" to "+inDirection);
		float degree = degreeToRotateToDirection(currentDirection,  inDirection);
		r.rotate(degree);
		return degree;
	}
	private static float degreeToRotateToDirection(float currentDirection, float inDirection){
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



	public ArrayList<String>  addCalibrationCommand(int x, int y, int direction, ArrayList<String> instruction){
		int bestDirection = direction;
		int bestCount = getTotalSideForCalibration(x,y,direction);

		int countE = getTotalSideForCalibration(x,y,(direction+90)%360);
		if(countE>bestCount){
			bestDirection = (direction+90)%360;
			bestCount = countE;
		}
		int countW = getTotalSideForCalibration(x,y,(direction+270)%360);
		if(countW>bestCount){
			bestDirection = (direction-90)%360;
			bestCount = countW;
		}
		int countS = getTotalSideForCalibration(x,y,(direction+180)%360);
		if(countS>bestCount){
			bestDirection = (direction-90)%360;
			bestCount = countS;
		}
		if(bestCount>0){
			float degreeToMove =rotateToDirection(direction,bestDirection);
			int intDegree = Math.round(degreeToMove);
			if(intDegree!=0){
				String rmovement= "R"+intDegree;
				if(intDegree<0){
					rmovement= "L"+(intDegree*-1);
				}
				instruction.add(rmovement);
				lastMovedBeforeCalibrate = lastMovedBeforeCalibrate+ Exploration.rotationCost;
			}
			instruction.add("C");
			lastMovedBeforeCalibrate=0;
			r.calibrate();
			degreeToMove =rotateToDirection(bestDirection,direction);
			intDegree = Math.round(degreeToMove);
			if(intDegree!=0){
				String rmovement= "R"+intDegree;
				if(intDegree<0){
					rmovement= "L"+(intDegree*-1);
				}
				instruction.add(rmovement);
				lastMovedBeforeCalibrate = lastMovedBeforeCalibrate+ Exploration.rotationCost;
			}
		}
		
		return instruction;
	}
	
	
	public int getTotalSideForCalibration(int x, int y, int direction){
		int count=0;
		int totalBlocks = 0;
		int obstacles[][] = m.getObstacles();
		int explored[][] = m.getExploredTiles();
		switch (direction){
		case 0:
			/*front sensorcheck*/
			 totalBlocks = 0;
			for(int i=0;i<3;i++){
				for(int j=0;j<3;j++){
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
				for(int j=0;j<3;j++){
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
				for(int j=0;j<3;j++){
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
				for(int j=0;j<3;j++){
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
					for(int j=0;j<3;j++){
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
					for(int j=0;j<3;j++){
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
					for(int j=0;j<3;j++){
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
					for(int j=0;j<3;j++){
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

	@Override
	public String geType() {
		// TODO Auto-generated method stub
		return FastestPathFactory.FP1;
	}
	
	
}
