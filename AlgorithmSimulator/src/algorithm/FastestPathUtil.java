package algorithm;

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import Data.Vertex;

public class FastestPathUtil {
	static boolean isClearPath(Vertex v1,Vertex v2, ArrayList<ArrayList<Vertex>> outOfBound){
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
	
	static ArrayList<ArrayList<Vertex>> areaToPolygons(Area a){
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
	
}
