package RPiInterface;

import Data.Map;
import Data.Position;
import Data.Robot;
import javafx.application.Platform;

public class RobotSensorSimulatorType1 extends RobotSensorSimulator {
	public void sensorInfoUpdate(){
		Robot robot = Robot.getInstance();
		Map map = Map.getInstance();
		int x = Math.round(robot.getCoordinateX());
		int y = Math.round(robot.getCoordinateY());
		float direction = robot.getDirection();
		//System.out.println("sensor info... x:"+x +" y:"+y +" direction:" + direction);

		int exploredTiles[][] = map.getExploredTiles();
		int obstacles[][] = map.getObstacles();
		exploredTiles[(y-1)][ (x-1)]=1;
		exploredTiles[ (y-1)][ (x)]=1;
		exploredTiles[ (y)][ (x-1)]=1;
		exploredTiles[ (y)][ (x)]=1;

		Position lineOfSensor1[] = new Position[4];
		Position lineOfSensor2[] = new Position[4];
		Position lineOfSensor3[] = new Position[4];
		Position lineOfSensor4[] = new Position[4];
		Position[][] lineOfSensors = {lineOfSensor1,lineOfSensor2,lineOfSensor3,lineOfSensor4};

		for(int i =0;i<4;i++){
			switch ((int)direction){
			case 0:
				lineOfSensor1[i]=new Position(x-1,y+1+i);
				lineOfSensor2[i]=new Position(x,y+1+i);
				if(i<3){
					lineOfSensor3[i]=new Position(x-2,y+i);
					lineOfSensor4[i]=new Position(x+1,y+i);
				}
				break;
			case 90:
				lineOfSensor1[i]=new Position(x+1+i,y);
				lineOfSensor2[i]=new Position(x+1+i,y-1);
				if(i<3){
					lineOfSensor3[i]=new Position(x+i,y+1);
					lineOfSensor4[i]=new Position(x+i,y-2);
				}
				break;
			case 180:
				lineOfSensor1[i]=new Position(x,y-2-i);
				lineOfSensor2[i]=new Position(x-1,y-2-i);
				if(i<3){
					lineOfSensor3[i]=new Position(x+1,y-1-i);
					lineOfSensor4[i]=new Position(x-2,y-1-i);
				}
				break;
			case 270:
				lineOfSensor1[i]=new Position(x-2-i,y-1);
				lineOfSensor2[i]=new Position(x-2-i,y);

				if(i<3){
					lineOfSensor3[i]=new Position(x-1-i,y-2);
					lineOfSensor4[i]=new Position(x-1-i,y+1);
				}
				break;
			}
		}


		for(Position[] sensors:lineOfSensors){
			for(Position sensor:sensors){
				if(sensor!=null&&sensor.getPosY()>=0&&(sensor.getPosY())<20&&(sensor.getPosX())>=0&&(sensor.getPosX())<15){
					exploredTiles[sensor.getPosY()][(sensor.getPosX())]=1;
					if(obstacles[sensor.getPosY()][(sensor.getPosX())]==1){
						break;
					}
				}else{
					break;
				}
			}
		}
		map.setExploredTiles(exploredTiles);
	}

	@Override
	public String getSensorType() {
		return RobotSensorSimulatorFactory.SENSOR_TYPE_1;
	}
}
