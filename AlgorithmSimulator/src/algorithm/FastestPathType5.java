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

public class FastestPathType5 extends FastestPath {
	public static ArrayList<Line> debugLine = new  ArrayList<Line>();
	public static boolean debugPath = true;
	public static float bufferArea = 1.1f;
	
	@Override
	public Message computeAction() {		
		Exploration.lastMovedBeforeCalibrate=0;
		r.setDirection(0);
		r.setPosX(1f);
		r.setPosY(1f);
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
		
		Dijkstra d = new DijkstraMinimunRotation();
		if(calibration){
			 d = new DijkstraForCalibration();
		}
		List<Vertex> path = d.computePaths(start, waypoint,vertices);
	//	System.out.println("Path1: "+path);
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
				Exploration.lastMovedBeforeCalibrate = Exploration.lastMovedBeforeCalibrate+ Exploration.rotationCost;
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
						instructions = Exploration.addCalibrationCommand((int)v2.x,(int)v2.y,(int) direction,instructions,getNextInstruction(direction,v3,v4));
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
			if(Exploration.lastMovedBeforeCalibrate+forwardCount>=Exploration.intervalForCalibrate){
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


	
	@Override
	public String geType() {
		// TODO Auto-generated method stub
		return FastestPathFactory.FP1;
	}
	
	
}
