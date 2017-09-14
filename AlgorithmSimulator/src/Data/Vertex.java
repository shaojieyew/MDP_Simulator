package Data;

import java.util.ArrayList;

public class Vertex implements Comparable<Vertex>
{
    public float x;
	public float y;
    public ArrayList<Vertex> adjacencies = new ArrayList<Vertex>();
    public double minDistance = Double.POSITIVE_INFINITY;
    public Vertex previous;
    public Vertex(float x, float y) { this.x = x;this.y= y; }
    public String toString() 
    { 
    	return "("+x+","+y+")"; 
    }
    public int compareTo(Vertex other)
    {
        return Double.compare(minDistance, other.minDistance);
    }

    public float getX() {
		return x;
	}
	public float getY() {
		return y;
	}
}
