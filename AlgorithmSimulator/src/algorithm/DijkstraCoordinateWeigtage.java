package algorithm;


import java.util.PriorityQueue;

import Data.Vertex;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;



public class DijkstraCoordinateWeigtage extends Dijkstra
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
                weight = weight *  (v.x+1) ;
                weight = weight *  ((20-v.y)+1) ;
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