package GUI;

import Data.Map;
import Data.MapListener;
import Data.Position;
import Data.Robot;
import Data.RobotListener;
import Data.WayPoint;
import Data.WayPointListener;
import RPiInterface.RobotSensorSimulator;
import RPiInterface.RobotSensorSimulatorFactory;
import algorithm.Test;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class MapGUI extends BorderPane implements MapListener, RobotListener, WayPointListener{
	public MapGUI(){
		Map.getInstance().addListener(this);
		Robot.getInstance().addListener(this);
		WayPoint.getInstance().addListener(this);
		Platform.runLater(new Runnable() {
            @Override public void run() {  
        		loadGraphic();   
            }
		});
	}
	
	public void loadGraphic(){
	         		getChildren().clear();
	         		loadMap();
	         		loadGrid();
	         		loadRobot();
	}

	private  Line[] vertGridLines = new Line[16];
	private  Line[] horiGridLines = new Line[21];
	protected  Rectangle[][] tiles = new Rectangle[20][15];
	private  Ellipse sensor = new Ellipse();
	private  Ellipse  robot =  new Ellipse();
	private  Line[] sensors =  new Line[4];
	
	protected void loadMap(){
		for(int x =0;x<15;x++){
			for(int y=0;y<20;y++){
				getChildren().remove(tiles[y][x]);
				if(tiles[y][x]==null){
					tiles[y][x] = new Rectangle();
					tiles[y][x].widthProperty().bind(widthProperty().divide(15));
					tiles[y][x].heightProperty().bind(heightProperty().divide(20));
					tiles[y][x].xProperty().bind(widthProperty().divide(15).multiply(x));
					tiles[y][x].yProperty().bind(heightProperty().subtract(heightProperty().divide(20).multiply(y+1)));
				}
				int exploredValue = Map.getInstance().getExploredTiles()[y][x];
				if(exploredValue==0){
					tiles[y][x].setFill(Color.DARKRED);
				}else{
					int obstacleValue = Map.getInstance().getObstacles()[y][x];
					if(obstacleValue==1){
						tiles[y][x].setFill(Color.BLACK);
					}else{
						tiles[y][x].setFill(Color.WHITE);
					}
				}
				getChildren().add(tiles[y][x]);
			}
		}
		loadStartEnd();
		loadWayPoint();
	}
	
	protected void loadStartEnd(){
		Position[][] definedPoints= {Map.STARTPOINT.getPositions(),Map.ENDPOINT.getPositions()};
		for(Position positions[]: definedPoints){
			for(Position pos: positions){
				int x = pos.getPosX();
				int y = pos.getPosY();
				int exploredValue = Map.getInstance().getExploredTiles()[y][x];
				if(exploredValue==1){
					if(tiles[y][x]==null){
						tiles[y][x] = new Rectangle();
						tiles[y][x].widthProperty().bind(widthProperty().divide(15));
						tiles[y][x].heightProperty().bind(heightProperty().divide(20));
						tiles[y][x].xProperty().bind(widthProperty().divide(15).multiply(x));
						tiles[y][x].yProperty().bind(heightProperty().subtract(heightProperty().divide(20).multiply(y+1)));
					}
					tiles[y][x].setFill(Color.LIGHTGRAY);
					getChildren().remove(tiles[y][x]);
					getChildren().add(tiles[y][x]);
				}
			}
		}
	}
	
	protected void loadWayPoint(){
		Position wp = WayPoint.getInstance().getPosition();
		if(wp!=null){
			int x = wp.getPosX();
			int y = wp.getPosY();
			if(x<0||y<0||x>14||y>19)
				return ;
			int exploredValue = Map.getInstance().getExploredTiles()[y][x];
			if(exploredValue==1){
				if(tiles[y][x]==null){
					tiles[y][x] = new Rectangle();
					tiles[y][x].widthProperty().bind(widthProperty().divide(15));
					tiles[y][x].heightProperty().bind(heightProperty().divide(20));
					tiles[y][x].xProperty().bind(widthProperty().divide(15).multiply(x));
					tiles[y][x].yProperty().bind(heightProperty().subtract(heightProperty().divide(20).multiply(y+1)));
				}
				tiles[y][x].setFill(Color.LIGHTSKYBLUE);
				getChildren().remove(tiles[y][x]);
				getChildren().add(tiles[y][x]);
			}
		}
	}
	protected void loadRobot(){
		getChildren().removeAll(robot,sensor);
		Robot r = Robot.getInstance();
		float x = r.getPosX();
		float y = r.getPosY();
		if(x<0||y<0||x>14||y>19)
			return ;
		float direction = r.getDirection();
		float xCenterPosition = (float) (x+0.5);
		float yCenterPosition = (float) (19.5-y);
		robot.centerXProperty().bind(widthProperty().divide(15).multiply((xCenterPosition)));
		robot.centerYProperty().bind(heightProperty().divide(20).multiply(yCenterPosition));         
		robot.radiusXProperty().bind(widthProperty().divide(15));   
		robot.radiusYProperty().bind(heightProperty().divide(20));
		robot.setFill(Color.GREEN);
		direction = direction%360;
	    double radians = Math.toRadians(direction);
		sensor.centerXProperty().bind(widthProperty().divide(15).multiply(xCenterPosition).add(widthProperty().divide(15f/0.5f).multiply(Math.sin(radians)))); 
		sensor.centerYProperty().bind(heightProperty().divide(20).multiply(yCenterPosition).subtract(heightProperty().divide(20f/0.5f).multiply(Math.cos(radians))));     
		sensor.radiusXProperty().bind(widthProperty().divide(15).divide(4));   
		sensor.radiusYProperty().bind(heightProperty().divide(20).divide(4));
		sensor.setFill(Color.BLACK);
		getChildren().addAll(robot,sensor);
		RobotSensorSimulator ss= r.getSensorSimulator();
		if(ss!=null){
			//loadSensorSimulator(ss.getSensorType());
		}
	}

	protected void loadSensorSimulator(String sensorType){
		Robot r = Robot.getInstance();
		float x = r.getPosX();
		float y = r.getPosY();
		if(x<0||y<0)
			return ;
		float direction = r.getDirection();
	    double radians = Math.toRadians(direction);
		for(int i =0;i<sensors.length;i++){
			if(sensors[i]==null){
				sensors[i]=new Line();
				sensors[i].setStroke(Color.web("0xff0000"));
				sensors[i].setFill(Color.TRANSPARENT);
				sensors[i].setStrokeWidth(2);	
			}
			
			if(sensorType.equals(RobotSensorSimulatorFactory.SENSOR_TYPE_1)){
				switch(i){
					case 0:radians = Math.toRadians(direction-50);
					break;
					case 1:radians = Math.toRadians(direction+50);
					break;
					case 2:radians = Math.toRadians(direction-110);
					break;
					case 3:radians = Math.toRadians(direction+110);
					break;
				}
			}
			if(sensorType.equals(RobotSensorSimulatorFactory.SENSOR_TYPE_2)){
				switch(i){
					case 0:radians = Math.toRadians(direction-50);
					break;
					case 1:radians = Math.toRadians(direction+50);
					break;
					case 2:radians = Math.toRadians(direction-60);
					break;
					case 3:radians = Math.toRadians(direction+60);
					break;
				}
			}
			sensors[i].startXProperty().bind(widthProperty().divide(15).multiply(x).add(widthProperty().divide(15f/0.5f).multiply(Math.sin(radians)))); 
			sensors[i].startYProperty().bind(heightProperty().divide(20).multiply(20-y).subtract(heightProperty().divide(20f/0.5f).multiply(Math.cos(radians))));     
			if(i==3||i==2){
				sensors[i].startXProperty().bind(widthProperty().divide(15).multiply(x).add(widthProperty().divide(15f/0.9f).multiply(Math.sin(radians)))); 
				sensors[i].startYProperty().bind(heightProperty().divide(20).multiply(20-y).subtract(heightProperty().divide(20f/0.9f).multiply(Math.cos(radians))));     
			}
			sensors[i].setStrokeWidth(2);

			if(sensorType.equals(RobotSensorSimulatorFactory.SENSOR_TYPE_1)){
				switch(i){
					case 0:radians = Math.toRadians(direction-5);
					break;
					case 1:radians = Math.toRadians(direction+5);
					break;
					case 2:radians = Math.toRadians(direction-25);
					break;
					case 3:radians = Math.toRadians(direction+25);
					break;
				}
			}
			if(sensorType.equals(RobotSensorSimulatorFactory.SENSOR_TYPE_2)){
				switch(i){
					case 0:radians = Math.toRadians(direction-5);
					break;
					case 1:radians = Math.toRadians(direction+5);
					break;
					case 2:radians = Math.toRadians(direction-85);
					break;
					case 3:radians = Math.toRadians(direction+85);
					break;
				}
			}
			sensors[i].endXProperty().bind(widthProperty().divide(15).multiply(x).add(widthProperty().divide(15f/5f).multiply(Math.sin(radians)))); 
			sensors[i].endYProperty().bind(heightProperty().divide(20).multiply(20-y).subtract(heightProperty().divide(20f/5f).multiply(Math.cos(radians)))); 
		}
		getChildren().addAll(sensors);
	}
	@Override
	public void updateMap() {
		Platform.runLater(new Runnable() {
            @Override public void run() {  
        		loadGraphic();   
            }
		});
	}
	@Override
	public void updateRobot() {
		Platform.runLater(new Runnable() {
            @Override public void run() {  
            	loadRobot();   
            }
		});
	}
	@Override
	public void updateWayPoint() {
		Platform.runLater(new Runnable() {
            @Override public void run() {  
        		loadGraphic();   
            }
		});
	}
	
	
	private void loadGrid(){
		for(int i =0;i<vertGridLines.length;i++){
			if(vertGridLines[i]==null){
				vertGridLines[i]=new Line();
				vertGridLines[i].setStroke(Color.web("0xaaaaaa"));
				vertGridLines[i].setFill(Color.TRANSPARENT);
				vertGridLines[i].setStrokeWidth(2);
				vertGridLines[i].startYProperty().bind(heightProperty().multiply(0));
				vertGridLines[i].endYProperty().bind(heightProperty().multiply(1));
				vertGridLines[i].startXProperty().bind(widthProperty().divide(15).multiply(i));
				vertGridLines[i].endXProperty().bind(widthProperty().divide(15).multiply(i));
			}
		}

		for(int i =0;i<horiGridLines.length;i++){
			if(horiGridLines[i]==null){
				horiGridLines[i]=new Line();
				horiGridLines[i].setStroke(Color.web("0xaaaaaa"));
				horiGridLines[i].setFill(Color.TRANSPARENT);
				horiGridLines[i].setStrokeWidth(2);
				horiGridLines[i].startXProperty().bind(widthProperty().multiply(0));
				horiGridLines[i].endXProperty().bind(widthProperty().multiply(1));
				horiGridLines[i].startYProperty().bind(heightProperty().divide(20).multiply(i));
				horiGridLines[i].endYProperty().bind(heightProperty().divide(20).multiply(i));
			}
		}
		getChildren().removeAll(vertGridLines);
		getChildren().removeAll(horiGridLines);
		getChildren().addAll(vertGridLines);
		getChildren().addAll(horiGridLines);
	}

	@Override
	public void onRobotStop() {
	}

	@Override
	public void onRobotStartExploring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRobotStopExploring() {
		// TODO Auto-generated method stub
		
	}

}
