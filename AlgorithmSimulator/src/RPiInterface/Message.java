package RPiInterface;

public class Message {
	//public static final 
	public static final String MESSAGE_HEADER_ALL="0";
	public static final String MESSAGE_HEADER_ARDUINO="1";
	public static final String MESSAGE_HEADER_ANDROID="2";
	private String exploredTile;
	private String exploredObstacle;
	private String[] movements;
	private int[] robotLocation;
	private boolean endOfExploration;
	private float direction;
	private String status;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public float getDirection() {
		return direction;
	}
	public void setDirection(float checkDirection) {
		this.direction = checkDirection;
	}
	public String getExploredTile() {
		return exploredTile;
	}
	public void setExploredTile(String exploredTile) {
		this.exploredTile = exploredTile;
	}
	public String getExploredObstacle() {
		return exploredObstacle;
	}
	public void setExploredObstacle(String exploredObstacle) {
		this.exploredObstacle = exploredObstacle;
	}
	public String[] getMovements() {
		return movements;
	}
	public void setMovements(String[] movements) {
		this.movements = movements;
	}
	public int[] getRobotLocation() {
		return robotLocation;
	}
	public void setRobotLocation(int[] robotLocation) {
		this.robotLocation = robotLocation;
	}
	public boolean isEndOfExploration() {
		return endOfExploration;
	}
	public void setEndOfExploration(boolean endOfExploration) {
		this.endOfExploration = endOfExploration;
	}
	
	public String getMessage(){
		String str = "";
		String movementString="";
		for(String movement: movements){
			movementString = movementString+movement+",";
		}
		if(movementString.length()>0){
			movementString=movementString.substring(0, movementString.length()-1);
		}
		
		str = status+"|"+ exploredTile+"|"+exploredObstacle+"|"+movementString+"|"+robotLocation[0]+","+robotLocation[1]+","+Math.round(direction);

		if(endOfExploration){
			str=str+"|1";
		}else{
			str=str+"|0";
		}
		return str;
	}
	
	public String getArduinoMessage(){
		String str = "";
		String movementString="";
		for(String movement: movements){
			movementString = movementString+movement+",";
		}
		if(movementString.length()>0){
			movementString=movementString.substring(0, movementString.length()-1);
		}
		
		str = "MV|"+movementString;
		return str;
	}

	public String getAndroidMessage(){
		String str = "";
		String movementString="";
		for(String movement: movements){
			movementString = movementString+movement+",";
		}
		if(movementString.length()>0){
			movementString=movementString.substring(0, movementString.length()-1);
		}
		
		str = status+"|"+ exploredTile+"|"+exploredObstacle+"|"+robotLocation[0]+","+robotLocation[1]+","+Math.round(direction)+"|"+movementString;

		if(endOfExploration){
			str=str+"|1";
		}else{
			str=str+"|0";
		}
		return str;
	}
}
