package algorithm;


import java.util.PriorityQueue;

import Data.Vertex;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;



public class DijkstraMinimunRotation extends Dijkstra
{
	@Override
	protected  List<Vertex> computePaths(Vertex source,Vertex dest)
    {
    	if(source==null||dest==null){
    		return null;
    	}
        source.minDistance = 0.;
        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
    vertexQueue.add(source);

    while (!vertexQueue.isEmpty()) {
        Vertex u = vertexQueue.poll();

            // Visit each edge exiting u
            for (Vertex v : u.adjacencies)
            {	
                double weight = 1;
                if(u.previous!=null){
                	if(u.previous.x==u.x&&v.x==u.x){
                		weight = weight/2.0;
                	}
                	if(u.previous.y==u.y&&v.y==u.y){
                		weight = weight/2.0;
                	}
                }else{
                	if(v.x==source.x){
                		weight = weight/2.0;
                	}
                	if(v.x==dest.x){
                		weight = weight/2.0;
                	}
                	if(v.y==source.y){
                		weight = weight/2.0;
                	}
                	if(v.y==dest.y){
                		weight = weight/2.0;
                	}
                }
                double distanceThroughU = u.minDistance + weight;
        if (distanceThroughU < v.minDistance) {
            vertexQueue.remove(v);

            v.minDistance = distanceThroughU ;
            v.previous = u;
            vertexQueue.add(v);
        }
            }
        }

	    List<Vertex> path = getShortestPathTo(source,dest);
	    return path;
    }
}