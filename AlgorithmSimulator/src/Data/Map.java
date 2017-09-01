package Data;

import java.util.ArrayList;

import util.HexBin;

public class Map {
	public static Map map=null;
	public static Map getInstance(){
		if(map==null){
			map= new Map();
		}
		return map;
	}
	private static Position[] START_POSITION = {new Position(0,0),new Position(1,0),new Position(2,0),new Position(0,1),new Position(1,1),new Position(2,1),new Position(0,2),new Position(1,2),new Position(2,2)};
	public static StartPoint STARTPOINT = new StartPoint(START_POSITION);
	private static Position[] END_POSITION = {new Position(14,19),new Position(14,18),new Position(14,17),new Position(13,19),new Position(13,18),new Position(13,17),new Position(12,19),new Position(12,18),new Position(12,17)};
	public static StartPoint ENDPOINT = new StartPoint(END_POSITION);
	
	private  ArrayList<MapListener> arr = new ArrayList<MapListener>();
	private static int exploredTiles [][] = new int[20][15];
	private static int obstacles [][] = new int[20][15];
	

	public  int[][] getObstacles() {
		return obstacles;
	}
	public int[][] getExploredTiles() {
		return exploredTiles;
	}
	public void setExploredTiles(int[][] exploredTiles) {
		this.exploredTiles = exploredTiles;
		layoutUpdated();
	}
	public  void setObstacle(int[][] obstacles) {
		this.obstacles = obstacles;
		layoutUpdated();
	}
	
	
	
	//in hexdecimal, from map descriptor file to arrays
	public void setMap(String exploredTileHex,String obstacleHex, String exploredObstacleHex) {
		 // System.out.println (exploredTile2);
		  String exploredTileBinary = HexBin.hexToBin(exploredTileHex);
		  exploredTileBinary =exploredTileBinary.substring(2, exploredTileBinary.length()-2);
		  
		  String obstacleBinary = HexBin.hexToBin(obstacleHex);
		  obstacleBinary =obstacleBinary.substring(2, obstacleBinary.length()-2);
		  
		  String exploredObstacleBinary=null;
		  if(exploredObstacleHex!=null){
			  exploredObstacleBinary = HexBin.hexToBin(exploredObstacleHex);
			  exploredObstacleBinary =exploredObstacleBinary.substring(8);
		  }
		  
		  int obstacleIndex=0;
		  for(int i =0;i<exploredTileBinary.length();i++){
			  char exploreBit = exploredTileBinary.charAt(i);
			  char obstacleBit = obstacleBinary.charAt(i);
			  int y = (i-(i%15))/15;
			  int x = i%15;
			  exploredTiles[y][x]=0;
			  obstacles[y][x]=0;
			  if(obstacleBit=='1'){
				  obstacles[y][x]=1;
			  }
			  if(exploreBit=='1'){
				  exploredTiles[y][x]=1;
				  if(exploredObstacleBinary!=null){
					  char exploredObstacleBit = exploredObstacleBinary.charAt(obstacleIndex);
					  if(exploredObstacleBit=='1'){
						  obstacles[y][x]=1;
					  }
				  }
				  obstacleIndex++;
			  }

			  if((x==0&&y==0)||(x==1&&y==0)||(x==2&&y==0)||
				  (x==0&&y==1)||(x==1&&y==1)||(x==2&&y==1)||
				  (x==0&&y==2)||(x==1&&y==2)||(x==2&&y==2)){
				  exploredTiles[y][x]=1;
				  obstacles[y][x]=0;
			  }

			  if((x==14&&y==19)||(x==13&&y==19)||(x==12&&y==19)||
				  (x==14&&y==18)||(x==13&&y==18)||(x==12&&y==18)||
				  (x==14&&y==17)||(x==13&&y==17)||(x==12&&y==17)){
				  exploredTiles[y][x]=1;
				  obstacles[y][x]=0;
			  }
		  }
		layoutUpdated();
	}
	
	private  void layoutUpdated(){
		updateListener();
	}
	
	public  void addListener(MapListener listener){
		arr.add(listener);
	}
	public  void updateListener(){
		for(MapListener a: arr){
			a.updateMap();
		}
	}
}
