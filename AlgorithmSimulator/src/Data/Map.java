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

					  //System.out.println(exploredObstacleBit);
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

	//private static int relations1 [][][][] = new int[20][15][20][15];

	private static  ArrayList<int []>[][] relations = new ArrayList[20][15];
	//ArrayList<int []> relations2 = new ArrayList<int[]>();
	private  void layoutUpdated(){
		for(int x1=1;x1<14;x1++){
			for(int y1=1;y1<19;y1++){
				if(       exploredTiles[y1+1][x1+1]==1
						&&exploredTiles[y1][x1+1]==1
						&&exploredTiles[y1-1][x1+1]==1
						&&exploredTiles[y1+1][x1]==1
						&&exploredTiles[y1][x1]==1
						&&exploredTiles[y1-1][x1]==1
						&&exploredTiles[y1+1][x1-1]==1
						&&exploredTiles[y1][x1-1]==1
						&&exploredTiles[y1-1][x1-1]==1
						
						&&obstacles[y1+1][x1+1]==0
						&&obstacles[y1][x1+1]==0
						&&obstacles[y1-1][x1+1]==0
						&&obstacles[y1+1][x1]==0
						&&obstacles[y1][x1]==0
						&&obstacles[y1-1][x1]==0
						&&obstacles[y1+1][x1-1]==0
						&&obstacles[y1][x1-1]==0
						&&obstacles[y1-1][x1-1]==0						
						
						){
					int y2=y1;
					int x2=x1;
					for(int i=0;i<4;i++){
						if(i==0){
							y2=y1;
							x2=x1+1;
						}
						if(i==1){
							y2=y1+1;
							x2=x1;
						}
						if(i==2){
							y2=y1-1;
							x2=x1;
						}
						if(i==3){
							y2=y1;
							x2=x1-1;
						}
						if(		x2>0&&y2>0&&x2<14&&y2<19
								&&exploredTiles[y2+1][x2+1]==1
								&&exploredTiles[y2][x2+1]==1
								&&exploredTiles[y2-1][x2+1]==1
								&&exploredTiles[y2+1][x2]==1
								&&exploredTiles[y2][x2]==1
								&&exploredTiles[y2-1][x2]==1
								&&exploredTiles[y2+1][x2-1]==1
								&&exploredTiles[y2][x2-1]==1
								&&exploredTiles[y2-1][x2-1]==1
								
								&&obstacles[y2+1][x2+1]==0
								&&obstacles[y2][x2+1]==0
								&&obstacles[y2-1][x2+1]==0
								&&obstacles[y2+1][x2]==0
								&&obstacles[y2][x2]==0
								&&obstacles[y2-1][x2]==0
								&&obstacles[y2+1][x2-1]==0
								&&obstacles[y2][x2-1]==0
								&&obstacles[y2-1][x2-1]==0	){
							//relations[y1][x1][y2][x2]=1;
							int []a1 = {x2,y2};
							int []a2 = {x1,y1};
								relations[y1][x1]= new ArrayList<int []>();
								relations[y2][x2]= new ArrayList<int []>();
								relations[y1][x1].add(a1);
								relations[y2][x2].add(a2);
						}
					}
				}
			}
		}
		
		updateListener();
		for(int y=0;y<relations.length;y++){
			for(int x=0;x<relations[y].length;x++){
				if(relations[y][x]!=null)
				System.out.println(relations[y][x].get(0)[0]+","+relations[y][x].get(0)[1]);
			}
		}
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
