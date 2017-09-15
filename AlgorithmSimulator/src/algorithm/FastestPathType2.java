package algorithm;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

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

public class FastestPathType2 extends FastestPath {
	public static ArrayList<Line> debugLine = new  ArrayList<Line>();
	public static boolean debugPath = true;
	public static float bufferArea = 1.2f;
	
	
	@Override
	public Message computeAction() {
		//Initialization
				ArrayList<ArrayList<Vertex>> outOfBound= updateOutofBound();
				MapGUI map = MapGUI.getInstance();
				map.getChildren().removeAll(debugLine);
				debugLine.clear();
				//check if start and end path is cleared
				Vertex start = new Vertex(getStartingX()+0.5f,getStartingX()+0.5f);
				Vertex end = new Vertex(13+0.5f,18+0.5f);
				Position wp = WayPoint.getInstance().getPosition();
				Vertex waypoint;
				if(wp!=null){
					waypoint = new Vertex(wp.getPosX()+0.5f,wp.getPosY()+0.5f);
				}else{
					waypoint = end;
				}
				if(isClearPath(start,waypoint,outOfBound)&&isClearPath(waypoint,end,outOfBound)){
					start.adjacencies.add(waypoint);
					end.adjacencies.add(waypoint);
					waypoint.adjacencies.add(start);
					waypoint.adjacencies.add(end);
					addDebugLine(waypoint,start);
					addDebugLine(waypoint,end);
				}else{
					//setup links
					for(ArrayList<Vertex> boundary1 : outOfBound){
						for(Vertex v1 : boundary1){
							System.out.println(v1.x+","+v1.y);
							for(ArrayList<Vertex> boundary2 : outOfBound){
								for(Vertex v2 : boundary2){
									if(v1.x!=v2.x||v1.y!=v2.y){
										if(v1.x==7.0 && v1.y==9 &&v2.x==7 && v2.y==14 ){
											System.out.println(v1.x+","+v1.y+"=22222>"+v2.x+","+v2.y);
										}
										if(isClearPath(v1,v2,outOfBound)){
											v1.adjacencies.add(v2);
											System.out.println(v1.x+","+v1.y+"=>"+v2.x+","+v2.y);
											addDebugLine(v1,v2);
										}
									}
								}
							}
						}
					}  
					
					for(ArrayList<Vertex> boundary2 : outOfBound){
						for(Vertex v2 : boundary2){
							if(start.x!=v2.x||start.y!=v2.y){
								if(isClearPath(start,v2,outOfBound)){
									start.adjacencies.add(v2);
									v2.adjacencies.add(start);
									addDebugLine(v2,start);
								}
							}
							if(waypoint.x!=v2.x||waypoint.y!=v2.y){
								if(isClearPath(waypoint,v2,outOfBound)){
									waypoint.adjacencies.add(v2);
									v2.adjacencies.add(waypoint);
									addDebugLine(v2,waypoint);
								}
							}
							if(end.x!=v2.x||end.y!=v2.y){
								if(isClearPath(end,v2,outOfBound)){
									end.adjacencies.add(v2);
									v2.adjacencies.add(end);
									addDebugLine(v2,end);
								}
							}
						}
					}
				}
				if(isClearPath(start,waypoint,outOfBound)){
					start.adjacencies.add(waypoint);
					waypoint.adjacencies.add(start);
					addDebugLine(waypoint,start);
				}
				if(isClearPath(waypoint,end,outOfBound)){
					end.adjacencies.add(waypoint);
					waypoint.adjacencies.add(end);
					addDebugLine(waypoint,end);
				}
				
				
				if(debugPath)
					map.getChildren().addAll(debugLine);
				
				ArrayList<Vertex> temp = new ArrayList<Vertex>();
				temp.add(start);
				temp.add(waypoint);
				temp.add(end);

				ArrayList<ArrayList<Vertex>> allVertices = new ArrayList<ArrayList<Vertex>>();
				allVertices.addAll(outOfBound);
				
				DijkstraDistanceWeighted d = new DijkstraDistanceWeighted();
				List<Vertex> path = d.computePaths(start, waypoint,allVertices);
				if(end.x!=waypoint.x||end.y!=waypoint.y){
					List<Vertex>  path2 = d.computePaths(waypoint, end,allVertices);
					path.remove(path.size()-1);
					path.addAll(path2);
				}
				System.out.println("Path : "+path);

				float forwardCount = 0;
				float direction = 0;
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
		return  areaToPolygons(areas);
	}
	private static ArrayList<ArrayList<Vertex>> areaToPolygons(Area a){
		//Polygon mask_tmp = new Polygon();
	    PathIterator path = a.getPathIterator(null);
	    ArrayList<ArrayList<Vertex>> allObstacles=new ArrayList< ArrayList< Vertex>>();
		ArrayList<Vertex> boundary= new ArrayList< Vertex>();
	    while (!path.isDone()) {
	    	float[] point = new float[2];
	        if(path.currentSegment(point) != PathIterator.SEG_CLOSE){
	            // mask_tmp.addPoint((int) point[0], (int) point[1]);
	            System.out.println((int) point[0]+","+ (int) point[1]);
	        	Vertex v = new Vertex(point[0]/100f,point[1]/100f);
	        	int count = boundary.size();
	        	if(count>1){
	        		if(boundary.get(count-1).x==boundary.get(count-2).x&&boundary.get(count-2).x==v.x){
	        			boundary.remove(count-1);
	        		}else{
	        			if(boundary.get(count-1).y==boundary.get(count-2).y&&boundary.get(count-2).y==v.y){
		        			boundary.remove(count-1);
		        		}
	        		}
	        	}
	            boundary.add(v);
	        }else{
	        	int count = boundary.size();
	        	if(count>2){
	        		if(boundary.get(0).x==boundary.get(1).x&&boundary.get(1).x==boundary.get(count-1).x){
	        			boundary.remove(0);
	        		}else{
	        			if(boundary.get(0).y==boundary.get(1).y&&boundary.get(1).y==boundary.get(count-1).y){
		        			boundary.remove(0);
		        		}
	        		}
	        	}
	        	allObstacles.add(boundary);
	        	boundary= new ArrayList< Vertex>();
	        }
            path.next();
	    }
	    return allObstacles;
	}
	
	private  Polygon getPolygonOfAnObstacle(int x, int y){
		Polygon r1 = new Polygon();
		r1.addPoint((int)((x-bufferArea)*100),(int) ((y-bufferArea)*100));
		r1.addPoint((int)((x+1f+bufferArea)*100), (int)((y-bufferArea)*100));
		r1.addPoint((int)((x+1f+bufferArea)*100), (int)((y+1f+bufferArea)*100));
		r1.addPoint((int)((x-bufferArea)*100), (int)((y+1f+bufferArea)*100));
		return r1;
	}
	private void addDebugLine(Vertex v1,Vertex v2){
		MapGUI map =MapGUI.getInstance();
		 javafx.scene.shape.Line line = new Line();
		 line.setStroke(Color.web("0xff0000"));
		 line.setStrokeWidth(1);
		 line.startXProperty().bind(map.widthProperty().divide(15).multiply(v1.x));
		 line.startYProperty().bind(map.heightProperty().subtract(map.heightProperty().divide(20).multiply(v1.y)));
		 line.endXProperty().bind(map.widthProperty().divide(15).multiply(v2.x));
		 line.endYProperty().bind(map.heightProperty().subtract(map.heightProperty().divide(20).multiply(v2.y)));
		 debugLine.add(line);
	}
	
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
	

	
	private static boolean isClearPath(Vertex v1,Vertex v2, ArrayList<ArrayList<Vertex>> outOfBound){
		float checkEvery =0.3f;
		float distance = (float) Math.hypot(v1.x-v2.x, v1.y-v2.y);
		int periods = (int) (distance/checkEvery);
		float xIncrement = (v2.x-v1.x)/(float)periods;
		float yIncrement = (v2.y-v1.y)/(float)periods;
		if(v1.x==3&&v1.y==3&&v2.x==7&&v2.y==0){
			System.out.println("aaa");
		}
		for(ArrayList<Vertex> polygon : outOfBound){
			float x = v1.x;
			float y = v1.y;
			for(int i =0;i<periods-1;i++){
				 x = x+xIncrement;
				 y = y+yIncrement;
				if(pointInPolygon(x,y,polygon)){
					return false;
				}
			}
		}
		
		return true;
	}
	
	
	
	
	
	
	public static boolean isPointOnTheLine(Point2D.Float A, Point2D.Float B, Point2D.Float P) {  
		/*
	    double m = (B.y - A.y) / (B.x - A.x);

	    //handle special case where the line is vertical
	    if (Double.isInfinite(m)) {
	        if(A.x == P.x) return true;
	        else return false;
	    }

	    if ((P.y - A.y) == m * (P.x - A.x)) return true;
	    else return false;
	    */
		if(A.x==B.x && A.x==P.x){
			if(A.y<=P.y&&B.y>=P.y){
				return true;
			}else{
				if(A.y>=P.y&&B.y<=P.y){
					return true;
				}
			}
		}else{
			if(A.y==B.y && A.y==P.y){
				if(A.x<=P.x&&B.x>=P.x){
					return true;
				}else{
					if(A.x>=P.x&&B.x<=P.x){
						return true;
					}
				}
			}else{
				return false;
			}
		}
		return false;
	}
	private static boolean linesIntersect(final double X1, final double Y1, final double X2, final double Y2,
		      final double X3, final double Y3, final double X4, final double Y4) {
		    return ((relativeCCW(X1, Y1, X2, Y2, X3, Y3)
		        * relativeCCW(X1, Y1, X2, Y2, X4, Y4) <= 0) && (relativeCCW(X3,
		            Y3, X4, Y4, X1, Y1)
		            * relativeCCW(X3, Y3, X4, Y4, X2, Y2) <= 0));
		  }
	
	private static int relativeCCW(final double X1, final double Y1, double X2, double Y2, double PX,
		      double PY) {
		    X2 -= X1;
		    Y2 -= Y1;
		    PX -= X1;
		    PY -= Y1;
		    double ccw = PX * Y2 - PY * X2;
		    if (ccw == 0) {
		      ccw = PX * X2 + PY * Y2;
		      if (ccw > 0) {
		        PX -= X2;
		        PY -= Y2;
		        ccw = PX * X2 + PY * Y2;
		        if (ccw < 0) {
		          ccw = 0;
		        }
		      }
		    }
		    return (ccw < 0) ? -1 : ((ccw > 0) ? 1 : 0);
		  }

	public static boolean pointInPolygon(float x, float y, ArrayList<Vertex> polygon){
        int intersectionCount = 0;
	      for(int i =1;i<polygon.size();i++){
	    	  Vertex v1 = polygon.get(i-1);
	    	  Vertex v2 = polygon.get(i);
		      if(linesIntersect(v1.x, v1.y, v2.x, v2.y,x, y, Double.MAX_VALUE, Float.MAX_VALUE)) {
		                intersectionCount++;
		      }
	      }
	      boolean inPolygon =(intersectionCount  %2 != 0);
	      boolean pointOnLine = false;
	      if(inPolygon){
			    Point2D.Float temp1 = new Point2D.Float(x , y);
		    	int size = polygon.size();
			    for(int i =0;i<polygon.size();i++){
				        Point2D.Float temp2 = new Point2D.Float(polygon.get(i%size).x , polygon.get(i%size).y);
				        Point2D.Float temp3 = new Point2D.Float(polygon.get((i+1)%size).x , polygon.get((i+1)%size).y);
						if(isPointOnTheLine(temp2, temp3,temp1)){
							pointOnLine = true;
							break;
						}
			      }
			    if(pointOnLine){
			    	inPolygon=false;
			    }
	      }
	      return inPolygon;
	}
}
