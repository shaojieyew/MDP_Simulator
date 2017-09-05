package RPiInterface;

import Data.Map;
import Data.Position;
import Data.Robot;
import javafx.application.Platform;

public class RobotSensorSimulatorType2 extends RobotSensorSimulator {
	public void sensorInfoUpdate(){
		Robot robot = Robot.getInstance();
		Map map = Map.getInstance();
		int x = Math.round(robot.getPosX());
		int y = Math.round(robot.getPosY());
		float direction = robot.getDirection();
		//System.out.println("sensor info... x:"+x +" y:"+y +" direction:" + direction);
		if(x<0||y<0||x>14||y>19){
			return;
		}
		int exploredTiles[][] = map.getExploredTiles();
		int obstacles[][] = map.getObstacles();
		exploredTiles[(y+1)][ (x-1)]=1;
		exploredTiles[ (y+1)][ (x)]=1;
		exploredTiles[ (y+1)][ (x+1)]=1;
		exploredTiles[(y-1)][ (x-1)]=1;
		exploredTiles[ (y-1)][ (x)]=1;
		exploredTiles[ (y-1)][ (x+1)]=1;
		exploredTiles[ (y)][ (x-1)]=1;
		exploredTiles[ (y)][ (x)]=1;
		exploredTiles[ (y)][ (x+1)]=1;

		Position [][] lineOfSensors = getLineOfSensor(x,y,direction);


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
		return RobotSensorSimulatorFactory.SENSOR_TYPE_2;
	}

	@Override
	public Position[][] getLineOfSensor(int x, int y, float direction) {
		Position lineOfSensor1[] = new Position[4];
		Position lineOfSensor2[] = new Position[4];
		Position lineOfSensor3[] = new Position[4];
		Position lineOfSensor4[] = new Position[4];
		Position lineOfSensor5[] = new Position[4];
		Position lineOfSensor6[] = new Position[4];
		Position lineOfSensor7[] = new Position[4];
		Position[][] lineOfSensors = {lineOfSensor1,lineOfSensor2,lineOfSensor3,lineOfSensor4,lineOfSensor5,lineOfSensor6,lineOfSensor7};

		for(int i =0;i<3;i++){
			switch ((int)direction){
			case 0:
				lineOfSensor1[i]=new Position(x-1,y+2+i);
				lineOfSensor2[i]=new Position(x,y+2+i);
				lineOfSensor3[i]=new Position(x+1,y+2+i);
				lineOfSensor4[i]=new Position(x-2-i,y-1);
				lineOfSensor5[i]=new Position(x-2-i,y);
				lineOfSensor6[i]=new Position(x+2+i,y);
				lineOfSensor7[i]=new Position(x+2+i,y-1);
				break;
			case 90:
				lineOfSensor1[i]=new Position(x+2+i,y+1);
				lineOfSensor2[i]=new Position(x+2+i,y);
				lineOfSensor3[i]=new Position(x+2+i,y-1);
				lineOfSensor4[i]=new Position(x-1,y+2+i);
				lineOfSensor5[i]=new Position(x,y+2+i);
				lineOfSensor6[i]=new Position(x,y-2-i);
				lineOfSensor7[i]=new Position(x-1,y-2-i);
				break;
			case 180:
				lineOfSensor1[i]=new Position(x+1,y-2-i);
				lineOfSensor2[i]=new Position(x,y-2-i);
				lineOfSensor3[i]=new Position(x-1,y-2-i);
				lineOfSensor4[i]=new Position(x+2+i,y+1);
				lineOfSensor5[i]=new Position(x+2+i,y);
				lineOfSensor6[i]=new Position(x-2-i,y);
				lineOfSensor7[i]=new Position(x-2-i,y+1);
				break;
			case 270:
				lineOfSensor1[i]=new Position(x-2-i,y-1);
				lineOfSensor2[i]=new Position(x-2-i,y);
				lineOfSensor3[i]=new Position(x-2-i,y+1);

				lineOfSensor4[i]=new Position(x+1,y-2-i);
				lineOfSensor5[i]=new Position(x,y-2-i);
				
				lineOfSensor6[i]=new Position(x,y+2+i);
				lineOfSensor7[i]=new Position(x+1,y+2+i);
				break;
			}
		}
		return lineOfSensors;
	}
}
