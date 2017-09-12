package RPiInterface;

import Data.Map;
import Data.Position;
import Data.Robot;
import Data.WayPoint;

public abstract class RPiInterface {
	public abstract void startConnection();
	public abstract void inputMessage(String string);
	public abstract void outputMessage(String string);
	
	
	public void setMap(String exploredTile,	String exploredObstacle){
		Map.getInstance().setMap(exploredTile,null,exploredObstacle);
	}
	public void setRobotLocation(int robotLocationX,	int robotLocationY, float robotDirection){
		Robot robot = Robot.getInstance();
		robot.stopAllMovement();
		robot.setPosX(robotLocationX);
		robot.setPosY(robotLocationY);
		robot.setDirection(robotDirection);
	}
	public void setWayPoint(int x, int y){
		WayPoint.getInstance().setPosition(new Position(x,y));
	}
	public void removeWayPoint(){
		WayPoint.getInstance().setPosition(null);
	}
	
	
	public void computeSensor(int robotLocationX,int robotLocationY,float robotDirection,int sensorInfo []) {
		Map map = Map.getInstance();
		int exploredTiles[][] = map.getExploredTiles();
		int obstacles[][] = map.getObstacles();
		RobotSensorSimulatorType1 simulator1 = new RobotSensorSimulatorType1();
		Position[][] lineOfSensors = simulator1.getLineOfSensor(robotLocationX, robotLocationY, robotDirection);
		int sensorIndex = 0;
		for(Position[] sensors:lineOfSensors){
			int sensorBlock = 1;
			for(Position sensor:sensors){
				if(sensor!=null&&sensor.getPosY()>=0&&(sensor.getPosY())<20&&(sensor.getPosX())>=0&&(sensor.getPosX())<15){
					exploredTiles[sensor.getPosY()][(sensor.getPosX())]=1;
					if(sensorInfo[sensorIndex]==sensorBlock){
						obstacles[sensor.getPosY()][(sensor.getPosX())]=1;
						break;
					}
				}else{
					break;
				}
				sensorBlock++;
			}
			sensorIndex++;
		}
	}
}
