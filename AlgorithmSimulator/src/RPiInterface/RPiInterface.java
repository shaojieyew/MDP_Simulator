package RPiInterface;

import Data.Map;
import Data.Position;
import Data.Robot;
import Data.WayPoint;
import algorithm.ObstaclesConfident;

public abstract class RPiInterface {
	public abstract void startConnection();
	public abstract void startConnection(String address);
	public abstract void inputMessage(String string);
	public abstract void outputMessage(String string);
	public abstract void disconnect();
	
	
	
	
	public void setMap(String exploredTile,	String exploredObstacle){
		Map.getInstance().setMap(exploredTile,null,exploredObstacle);
	}
	public void setRobotLocation(float robotLocationX,	float robotLocationY, float robotDirection){
		Robot robot = Robot.getInstance();
		robot.stopAllMovement();
		robot.setPosX(robotLocationX);
		robot.setPosY(robotLocationY);
		robot.setDirection(robotDirection);
	}
	public void setWayPoint(int x, int y){
		if(x>0 && y>0){
			removeWayPoint();
			WayPoint.getInstance().setPosition(new Position(x,y));
		}
	}
	public void removeWayPoint(){
		WayPoint.getInstance().setPosition(null);
	}
	
/*	
	  1	3 5
  	  | | |
  __ 0
           
  __ 2     4 __ 
  
	  1	3 5
  	  | | |
  __ 0     4 __ 
           
  __ 2
	*/
	//
	//SS|
	//public static float sensorOffset[]={10.3f,10.5f,10.3f,8.3f,15.15f,10.5f};
	//public static float sensorOffset[]={11.99f,11.26f,10.83f,6.88f,15.24f,10.87f};
	//SS|11.57,10.33,10.21,5.77,-1.00,10.30
	public static float sensorOffset[]={9.42f,11.07f,10.21f,6.35f,21.56f,10.07f};
	public void computeSensor(float robotLocationX,float robotLocationY,float robotDirection,float sensorDistance []) {
		int sensorInfo[] = new int[6];
		int sensorIndex = 0;
		for(float distance : sensorDistance){
			if(distance<0){
				sensorInfo[sensorIndex] = 1;
				sensorIndex++;
				continue;
			}
			if(distance==0){
				sensorInfo[sensorIndex] = 0;
				sensorIndex++;
				continue;
			}
			if((distance-sensorOffset[sensorIndex]<=5)){
				sensorInfo[sensorIndex] = 1;
			}else{
				if((distance-sensorOffset[sensorIndex]<=15)){
					sensorInfo[sensorIndex] = 2;
				}else{
					if((distance-sensorOffset[sensorIndex]<=25)){
						sensorInfo[sensorIndex] = 3;
					}else{
						if(sensorIndex==4){
							if((distance-sensorOffset[sensorIndex]<=35)){
								sensorInfo[sensorIndex] = 4;
							}else{
								sensorInfo[sensorIndex] = 0;
							}
						}else{
							sensorInfo[sensorIndex] = 0;
						}
					}
				}
			}
			sensorIndex++;
			continue;
		}
		
		Map map = Map.getInstance();
		int exploredTiles[][] = map.getExploredTiles();
		int obstacles[][] = map.getObstacles();
		//System.out.println(robotLocationX+","+robotLocationY);
		exploredTiles[(int) robotLocationY-1][(int) robotLocationX-1]=1;
		exploredTiles[(int) robotLocationY-1][(int) robotLocationX]=1;
		exploredTiles[(int) robotLocationY-1][(int) robotLocationX+1]=1;
		exploredTiles[(int) robotLocationY][(int) robotLocationX-1]=1;
		exploredTiles[(int) robotLocationY][(int) robotLocationX]=1;
		exploredTiles[(int) robotLocationY][(int) robotLocationX+1]=1;
		exploredTiles[(int) robotLocationY+1][(int) robotLocationX-1]=1;
		exploredTiles[(int) robotLocationY+1][(int) robotLocationX]=1;
		exploredTiles[(int) robotLocationY+1][(int) robotLocationX+1]=1;
		
		
		ObstaclesConfident.obstacleCounter[(int) robotLocationY-1][(int) robotLocationX-1]=-10000;
		ObstaclesConfident.obstacleCounter[(int) robotLocationY-1][(int) robotLocationX]=-10000;
		ObstaclesConfident.obstacleCounter[(int) robotLocationY-1][(int) robotLocationX+1]=-10000;
		ObstaclesConfident.obstacleCounter[(int) robotLocationY][(int) robotLocationX-1]=-10000;
		ObstaclesConfident.obstacleCounter[(int) robotLocationY][(int) robotLocationX]=-10000;
		ObstaclesConfident.obstacleCounter[(int) robotLocationY][(int) robotLocationX+1]=-10000;
		ObstaclesConfident.obstacleCounter[(int) robotLocationY+1][(int) robotLocationX-1]=-10000;
		ObstaclesConfident.obstacleCounter[(int) robotLocationY+1][(int) robotLocationX]=-10000;
		ObstaclesConfident.obstacleCounter[(int) robotLocationY+1][(int) robotLocationX+1]=-10000;

		
		
		RobotSensorSimulator simulator2 = Robot.getInstance().getSensorSimulator();
		Position[][] lineOfSensors = simulator2.getLineOfSensor((int)robotLocationX, (int)robotLocationY, robotDirection);
		sensorIndex = 0;
		for(Position[] sensors:lineOfSensors){
			int sensorBlock = 1;
			for(Position sensor:sensors){
				if(sensor!=null&&sensor.getPosY()>=0&&(sensor.getPosY())<20&&(sensor.getPosX())>=0&&(sensor.getPosX())<15){
					//if(exploredTiles[sensor.getPosY()][(sensor.getPosX())]==0){
						if(sensorInfo[sensorIndex]==sensorBlock){
							//obstacles[sensor.getPosY()][(sensor.getPosX())]=1;																//(5-sensorBlock)
							ObstaclesConfident.obstacleCounter[sensor.getPosY()][(sensor.getPosX())]=ObstaclesConfident.obstacleCounter[sensor.getPosY()][(sensor.getPosX())]+((sensorBlock==1)?1000:(5-sensorBlock));
							if(ObstaclesConfident.obstacleCounter[sensor.getPosY()][(sensor.getPosX())]>=1){
								exploredTiles[sensor.getPosY()][(sensor.getPosX())]=1;
								obstacles[sensor.getPosY()][(sensor.getPosX())]=1;
								break;
							}
						}else{																															//(5-sensorBlock)
							ObstaclesConfident.obstacleCounter[sensor.getPosY()][(sensor.getPosX())]=ObstaclesConfident.obstacleCounter[sensor.getPosY()][(sensor.getPosX())]-((sensorBlock==1)?999:(5-sensorBlock));
							if(ObstaclesConfident.obstacleCounter[sensor.getPosY()][(sensor.getPosX())]<=-1){
								exploredTiles[sensor.getPosY()][(sensor.getPosX())]=1;
								obstacles[sensor.getPosY()][(sensor.getPosX())]=0;
							}else{
								if(obstacles[sensor.getPosY()][(sensor.getPosX())]==1){
									break;
								}
							}
						}
						//	obstacles[sensor.getPosY()][(sensor.getPosX())]=0;
					//}
					//if(exploredTiles[sensor.getPosY()][(sensor.getPosX())]==0){
					/*
					exploredTiles[sensor.getPosY()][(sensor.getPosX())]=1;
						if(sensorInfo[sensorIndex]==sensorBlock){
							obstacles[sensor.getPosY()][(sensor.getPosX())]=1;
							break;
						}
						
						*/
					
					//}else{
					//	if(obstacles[sensor.getPosY()][(sensor.getPosX())]==1){
					//		break;
					//	}
					//}
				}else{
					break;
				}
				sensorBlock++;
			}
			sensorIndex++;
		}

		map.setExploredTiles(exploredTiles);
		map.setObstacle(obstacles);
	}
	
	
	
	
	/*
	 	  0	3 1
	  	  | | |
	  __ 4
	           2 __ 
	  __ 5
	  
	 */
	
	/*-1, 10-35, 0*/
	
	/*
	public static float sensorOffset[]={10f,10f,5.4f,10f,14.9f,6f};
	public void computeSensor(float robotLocationX,float robotLocationY,float robotDirection,float sensorDistance []) {
		int sensorInfo[] = new int[6];
		int sensorIndex = 0;
		for(float distance : sensorDistance){
			if(distance<0){
				sensorInfo[sensorIndex] = 1;
				sensorIndex++;
				continue;
			}
			if(distance==0){
				sensorInfo[sensorIndex] = 0;
				sensorIndex++;
				continue;
			}
			if((distance-sensorOffset[sensorIndex]<=5)){
				sensorInfo[sensorIndex] = 1;
			}else{
				if((distance-sensorOffset[sensorIndex]<=15)){
					sensorInfo[sensorIndex] = 2;
				}else{
					if((distance-sensorOffset[sensorIndex]<=25)){
						sensorInfo[sensorIndex] = 3;
					}else{
						sensorInfo[sensorIndex] = 0;
					}
				}
			}
			sensorIndex++;
			continue;
		}
		
		Map map = Map.getInstance();
		int exploredTiles[][] = map.getExploredTiles();
		int obstacles[][] = map.getObstacles();
		System.out.println(robotLocationX+","+robotLocationY);
		exploredTiles[(int) robotLocationY-1][(int) robotLocationX-1]=1;
		exploredTiles[(int) robotLocationY-1][(int) robotLocationX]=1;
		exploredTiles[(int) robotLocationY-1][(int) robotLocationX+1]=1;
		exploredTiles[(int) robotLocationY][(int) robotLocationX-1]=1;
		exploredTiles[(int) robotLocationY][(int) robotLocationX]=1;
		exploredTiles[(int) robotLocationY][(int) robotLocationX+1]=1;
		exploredTiles[(int) robotLocationY+1][(int) robotLocationX-1]=1;
		exploredTiles[(int) robotLocationY+1][(int) robotLocationX]=1;
		exploredTiles[(int) robotLocationY+1][(int) robotLocationX+1]=1;
		
		
		RobotSensorSimulatorType1 simulator1 = new RobotSensorSimulatorType1();
		Position[][] lineOfSensors = simulator1.getLineOfSensor((int)robotLocationX, (int)robotLocationY, robotDirection);
		sensorIndex = 0;
		for(Position[] sensors:lineOfSensors){
			int sensorBlock = 1;
			for(Position sensor:sensors){
				if(sensor!=null&&sensor.getPosY()>=0&&(sensor.getPosY())<20&&(sensor.getPosX())>=0&&(sensor.getPosX())<15){

					//if(exploredTiles[sensor.getPosY()][(sensor.getPosX())]==0){
						obstacles[sensor.getPosY()][(sensor.getPosX())]=0;
					//}
					//if(exploredTiles[sensor.getPosY()][(sensor.getPosX())]==0){
						exploredTiles[sensor.getPosY()][(sensor.getPosX())]=1;
						if(sensorInfo[sensorIndex]==sensorBlock){
							obstacles[sensor.getPosY()][(sensor.getPosX())]=1;
							break;
						}
					//}else{
					//	if(obstacles[sensor.getPosY()][(sensor.getPosX())]==1){
					//		break;
					//	}
					//}
				}else{
					break;
				}
				sensorBlock++;
			}
			sensorIndex++;
		}

		map.setExploredTiles(exploredTiles);
		map.setObstacle(obstacles);
	}*/
}
