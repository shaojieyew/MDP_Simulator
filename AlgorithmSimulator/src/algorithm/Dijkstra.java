package algorithm;

import java.util.PriorityQueue;
import Data.Vertex;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Dijkstra
{
    public static List<Vertex> computePaths(Vertex source,Vertex dest)
    {
    	return computePaths(source, dest,0, 0);
    }
	
    public static List<Vertex> computePaths(Vertex source,Vertex dest,int leanX, int leanY)
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
                double distanceThroughU = u.minDistance + weight;
        if (distanceThroughU < v.minDistance) {
            vertexQueue.remove(v);

            v.minDistance = distanceThroughU ;
            v.previous = u;
            vertexQueue.add(v);
        }
            }
        }

	    List<Vertex> path = getShortestPathTo(dest);
	    return path;
    }

    public static List<Vertex> getShortestPathTo(Vertex target)
    {
        List<Vertex> path = new ArrayList<Vertex>();
        for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
            path.add(vertex);

        Collections.reverse(path);
        return path;
    }

    public static void main(String[] args)
    {
        // mark all the vertices 
        Vertex A = new Vertex(0,0);
        Vertex B = new Vertex(0,1);
        Vertex C = new Vertex(0,2);
        Vertex D = new Vertex(1,0);
        Vertex E = new Vertex(1,1);
        Vertex F = new Vertex(1,2);
        Vertex G = new Vertex(2,0);
        Vertex H = new Vertex(2,1);
        Vertex I = new Vertex(2,2);

        // set the edges and weight
        A.adjacencies.add(B);
        A.adjacencies.add(D);
        B.adjacencies.add(A);
        B.adjacencies.add(E);
        B.adjacencies.add(C);
        C.adjacencies.add(B);
        C.adjacencies.add(F);
        D.adjacencies.add(E);
        D.adjacencies.add(G);
        D.adjacencies.add(A);
        E.adjacencies.add(B);
        E.adjacencies.add(F);
        E.adjacencies.add(H);
        E.adjacencies.add(D);
        F.adjacencies.add(C);
        F.adjacencies.add(E);
        F.adjacencies.add(I);
        G.adjacencies.add(D);
        G.adjacencies.add(H);
        I.adjacencies.add(F);
        I.adjacencies.add(H);
        H.adjacencies.add(E);
        H.adjacencies.add(I);
        H.adjacencies.add(G);
        System.out.println("Path: " + computePaths(C,G));
    }
}