package Data;

import java.util.ArrayList;

public class WayPoint {
	public static WayPoint wp=null;
	private Position position=null;
	public static WayPoint getInstance(){
		if(wp==null){
			wp= new WayPoint();
		}
		return wp;
	}
	
	private  ArrayList<WayPointListener> arr = new ArrayList<WayPointListener>();
	public  void addListener(WayPointListener listener){
		arr.add(listener);
	}
	public  void updateListener(){
		for(WayPointListener a: arr){
			a.updateWayPoint();
		}
	}

	public Position getPosition() {
		return position;
	}
	public void setPosition(Position position) {
		this.position = position;
		
		updateListener();
	}
}
