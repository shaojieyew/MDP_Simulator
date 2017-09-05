package RPiInterface;

import Data.Map;
import Data.Position;
import Data.Robot;
import javafx.application.Platform;

public class RobotSensorSimulatorType3 extends RobotSensorSimulator {
	public void sensorInfoUpdate(){
		Robot robot = Robot.getInstance();
		Map map = Map.getInstance();
		int x = Math.round(robot.getPosX());
		int y = Math.round(robot.getPosY());
		float direction = robot.getDirection();
		//System.out.println("sensor info... x:"+x +" y:"+y +" direction:" + direction);
		if(x<0||y<0){
			return;
		}
		int exploredTiles[][] = map.getExploredTiles();
		int obstacles[][] = map.getObstacles();
		exploredTiles[(y-1)][ (x-1)]=1;
		exploredTiles[ (y-1)][ (x)]=1;
		exploredTiles[ (y)][ (x-1)]=1;
		exploredTiles[ (y)][ (x)]=1;

		Position lineOfSensor1[] = new Position[3];
		Position lineOfSensor2[] = new Position[3];
		Position lineOfSensor3[] = new Position[3];
		Position lineOfSensor4[] = new Position[3];
		Position lineOfSensor5[] = new Position[3];
		Position lineOfSensor6[] = new Position[3];
		Position[][] lineOfSensors = {lineOfSensor1,lineOfSensor2,lineOfSensor3,lineOfSensor4,lineOfSensor5,lineOfSensor6};

		for(int i =0;i<3;i++){
			switch ((int)direction){
			case 0:
					lineOfSensor1[i]=new Position(x-1,y+1+i);
					lineOfSensor2[i]=new Position(x,y+1+i);
					lineOfSensor3[i]=new Position(x-2-i,y);
					lineOfSensor4[i]=new Position(x-2-i,y-1);
					lineOfSensor5[i]=new Position(x+1+i,y);
					lineOfSensor6[i]=new Position(x+1+i,y-1);
				
				break;

			case 90:
					lineOfSensor1[i]=new Position(x+1+i,y);
					lineOfSensor2[i]=new Position(x+1+i,y-1);
					lineOfSensor3[i]=new Position(x-1,y+1+i);
					lineOfSensor4[i]=new Position(x,y+1+i);
					lineOfSensor5[i]=new Position(x,y-2-i);
					lineOfSensor6[i]=new Position(x-1,y-2-i);
				
				break;
			case 180:
					lineOfSensor1[i]=new Position(x,y-2-i);
					lineOfSensor2[i]=new Position(x-1,y-2-i);
					lineOfSensor3[i]=new Position(x+1+i,y);
					lineOfSensor4[i]=new Position(x+1+i,y-1);
					lineOfSensor5[i]=new Position(x-2-i,y-1);
					lineOfSensor6[i]=new Position(x-2-i,y);
				
				break;
			case 270:
					lineOfSensor1[i]=new Position(x-2-i,y-1);
					lineOfSensor2[i]=new Position(x-2-i,y);
					lineOfSensor3[i]=new Position(x,y-2-i);
					lineOfSensor4[i]=new Position(x-1,y-2-i);
					lineOfSensor5[i]=new Position(x-1,y+1+i);
					lineOfSensor6[i]=new Position(x,y+1+i);
				
				break;
				/*
			case 45:
				if(i<6){
					if(i%2==1){
						lineOfSensor1[i]=new Position(x+1+(i/2),y+1+(i/2));
						lineOfSensor2[i]=new Position(x+1+(i/2),y+1+(i/2));
					}else{
						lineOfSensor1[i]=new Position(x+(i/2),y+1+(i/2));
						lineOfSensor2[i]=new Position(x+1+(i/2),y+(i/2));
					}
				}
				if(i<5){
					if(i%2==1){
						lineOfSensor3[i]=new Position(x+1+(i/2),y-2-(i/2));
						lineOfSensor4[i]=new Position(x-2-(i/2),y+1+(i/2));
					}else{
						lineOfSensor3[i]=new Position(x+1+(i/2),y-1-(i/2));
						lineOfSensor4[i]=new Position(x-1-(i/2),y+1+(i/2));
					}	
				}
				break;
			case 135:
				if(i<6){
					if(i%2==1){
						lineOfSensor1[i]=new Position(x+1+(i/2),y-2-(i/2));
						lineOfSensor2[i]=new Position(x+1+(i/2),y-2-(i/2));
					}else{
						lineOfSensor1[i]=new Position(x+1+(i/2),y-1-(i/2));
						lineOfSensor2[i]=new Position(x+(i/2),y-2-(i/2));
					}
				}
				if(i<5){
					if(i%2==1){
						lineOfSensor3[i]=new Position(x+1+(i/2),y+1+(i/2));
						lineOfSensor4[i]=new Position(x-2-(i/2),y-2-(i/2));
					}else{
						lineOfSensor3[i]=new Position(x+1+(i/2),y+(i/2));
						lineOfSensor4[i]=new Position(x-1-(i/2),y-2-(i/2));
					}	
				}
				break;
			
			case 225:
				if(i<6){
					if(i%2==1){
						lineOfSensor1[i]=new Position(x-2-(i/2),y-2-(i/2));
						lineOfSensor2[i]=new Position(x-2-(i/2),y-2-(i/2));
					}else{
						lineOfSensor1[i]=new Position(x-1-(i/2),y-2-(i/2));
						lineOfSensor2[i]=new Position(x-2-(i/2),y-1-(i/2));
					}
				}
				if(i<5){
					if(i%2==1){
						lineOfSensor3[i]=new Position(x+1+(i/2),y-2-(i/2));
						lineOfSensor4[i]=new Position(x-2-(i/2),y+1+(i/2));
					}else{
						lineOfSensor3[i]=new Position(x+(i/2),y-2-(i/2));
						lineOfSensor4[i]=new Position(x-2-(i/2),y+(i/2));
					}	
				}
				break;
			case 315:
				if(i<6){
					if(i%2==1){
						lineOfSensor1[i]=new Position(x-2-(i/2),y+1+(i/2));
						lineOfSensor2[i]=new Position(x-2-(i/2),y+1+(i/2));
					}else{
						lineOfSensor1[i]=new Position(x-2-(i/2),y+(i/2));
						lineOfSensor2[i]=new Position(x-1-(i/2),y+1+(i/2));
					}
				}
				if(i<5){
					if(i%2==1){
						lineOfSensor3[i]=new Position(x-2-(i/2),y-2-(i/2));
						lineOfSensor4[i]=new Position(x+1+(i/2),y+1+(i/2));
					}else{
						lineOfSensor3[i]=new Position(x-2-(i/2),y-1-(i/2));
						lineOfSensor4[i]=new Position(x+1+(i/2),y+(i/2));
					}	
				}
				break;*/
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
		return RobotSensorSimulatorFactory.SENSOR_TYPE_3;
	}
}