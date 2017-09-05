package application;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import Data.Map;
import Data.Position;
import Data.Robot;
import Data.RobotListener;
import Data.WayPoint;
import GUI.FXMLController;
import GUI.MapGUI;
import GUI.MapObstacleGUI;
import RPiInterface.RobotSensorSimulatorFactory;
import RPiInterface.RobotSensorSimulatorType1;
import RPiInterface.RobotSensorSimulatorType2;
import algorithm.Test;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import util.FileUtility;
import util.FilesChooser;
import util.HexBin;

public class MainController extends FXMLController  implements Initializable, RobotListener{

	@FXML
	private BorderPane rootPane;
	@FXML
	private BorderPane mapPane;
	@FXML
	private Button startEndBtn;
	@FXML
	private Button resetBtn;
	@FXML
	private Button exploreBtn;
	@FXML
	private TextField robotXpos;
	@FXML
	private TextField robotYpos;
	@FXML
	private TextField waypointXpos;
	@FXML
	private TextField waypointYpos;
	@FXML
	private TextField forwardTextField;
	@FXML
	private TextField rotateTextField;

	@FXML
	private TextField travelSpeedTextField;
	@FXML
	private TextField rotateSpeedTextField;

	@FXML
	private TextArea textArea;
	@FXML
	private ComboBox sensorCombo;
	
	@FXML
	private void onclickLoadMapFileBtn(){
		String exploredTile="C000000000000000000000000000000000000000000000000000000000000000000000000003";
		String obstacle="C000000000000000000000000000000000000000000000000000000000000000000000000003";
		String exploredObstacle=null;
		
		
		//MapInfo.updateLayout();
		File file = FilesChooser.show(getStage(), "Load Map", Paths.get("").toAbsolutePath().toString(), FilesChooser.FORMAT_TEXT);
		try{
			  FileInputStream fstream = new FileInputStream(file);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  int line = 1;
			  while ((strLine = br.readLine()) != null)   {
					  switch(line){
					  case 1:
						  exploredTile=strLine;
						  break;
					  case 2:
						  exploredObstacle=strLine;
						  break;
					  case 3:
						  obstacle=strLine;
						  break;
					  }
					  line++;

					  System.out.println(line+":"+strLine);
				//if(line==2)
				//	break;
			}
			in.close();
		}catch (Exception e){
			 System.err.println("Error: " + e.getMessage());
		}
		Map.getInstance().setMap(exploredTile,obstacle,exploredObstacle);
	}

	@FXML
	private void robotPosXChanged(){
		Robot robot =Robot.getInstance();
		float coordinateX = Float.parseFloat(robotXpos.getText());
		robot.setPosX(coordinateX);
	}
	@FXML
	private void robotPosYChanged(){
		Robot robot =Robot.getInstance();
		float coordinateY = Float.parseFloat(robotYpos.getText());
		robot.setPosY(coordinateY);
	}
	@FXML
	private void waypointPosChanged(){
		WayPoint wp =WayPoint.getInstance();
		if(waypointYpos.getText().length()>0&&waypointXpos.getText().length()>0){
			int posX = Integer.parseInt(waypointXpos.getText());
			int posY = Integer.parseInt(waypointYpos.getText());
			wp.setPosition(new Position(posX,posY));
		}else{
			wp.setPosition(null);
		}
	}
	

	@FXML
	private void onClickMoveForward(){
		Robot robot =Robot.getInstance();
		float moveDistance = Float.parseFloat(forwardTextField.getText());
		robot.moveForward(moveDistance);
	}
	@FXML
	private void onclickRotate(){
		Robot robot =Robot.getInstance();
		int rotate = Integer.parseInt(rotateTextField.getText());
		robot.rotate(rotate);
	}
	
	@FXML
	private void onclickStop(){
		Robot.getInstance().stopMovement();
	}
	
	@FXML
	private void onclickResetSimulator(){
		Robot robot = Robot.getInstance();
		robot.setPosX(1);
		robot.setPosY(1);
		robot.setDirection(0);
		Map map = Map.getInstance();
		WayPoint.getInstance().setPosition(null);
		

	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		MapGUI mapGui = new MapGUI();
		Robot.getInstance().addListener(this);
		mapPane.setCenter(mapGui);
		sensorCombo.setValue(RobotSensorSimulatorFactory.SENSOR_TYPE_1);
		
	}

	@Override
	public void updateRobot() {
		Platform.runLater(new Runnable() {
            @Override public void run() {  
            	Robot r = Robot.getInstance();
            	textArea.setText("Robot coordinate x:"+r.getPosX()+" y:"+r.getPosY()+"\n"+textArea.getText().substring(0,textArea.getText().length()>300?299:textArea.getText().length()));
            }
		});
	}

	@FXML
	public void onRotatSpeedChange() {
		int rotatingSpeed = Integer.parseInt(rotateSpeedTextField.getText());
		Robot.setMilisecondPerRotate(rotatingSpeed);
	}

	@FXML
	public void onTravelSpeedChange() {
		int travelSpeed = Integer.parseInt(travelSpeedTextField.getText());
		Robot.setMilisecondPer10CM(travelSpeed);
	}
	@FXML
	public void onclickDiscoverAll() {
		int tiles[][]=new int[20][15];
		for (int[] row : tiles)
		    Arrays.fill(row, 1);
		Map.getInstance().setExploredTiles(tiles);
	}
	@FXML
	public void onClickHideAll() {
		Map.getInstance().setExploredTiles(new int[20][15]);
	}
	@FXML
	public void onclickGenerateMapStatus() {
		//MapInfo.updateLayout();
		File file = FilesChooser.save(getStage(), "Save Map Status", Paths.get("").toAbsolutePath().toString(), FilesChooser.FORMAT_TEXT);
		if(file!=null){
			String binaryExplored="11";
			String binaryExploredObstacle="";
			String binaryObstacle="11";
			int exploredTile[][]=Map.getInstance().getExploredTiles();
			int obstacles[][]=Map.getInstance().getObstacles();
			for(int y =0;y<20;y++){
				for(int x =0;x<15;x++){
					binaryExplored=binaryExplored+exploredTile[y][x];
					binaryObstacle=binaryObstacle+obstacles[y][x];
					if(exploredTile[y][x]==1){
						binaryExploredObstacle=binaryExploredObstacle+obstacles[y][x];
					}
				}
			}
			binaryExplored=binaryExplored+"11";
			binaryObstacle=binaryObstacle+"11";
			if(binaryExploredObstacle.length()%8!=0){
				for(int i =0;i<binaryExploredObstacle.length()%8;i++){
					binaryExploredObstacle=binaryExploredObstacle+"1";
				}
			}
			FileUtility.writeWordsToText(HexBin.BinTohex(binaryExplored)+"\n"+HexBin.BinTohex("11111111"+binaryExploredObstacle).substring(2)+"\n"+HexBin.BinTohex(binaryObstacle), file.getAbsolutePath());
		}
	}

	@Override
	public void onRobotStop() {
		Platform.runLater(new Runnable() {
            @Override public void run() {  
            	Robot r = Robot.getInstance();
            	textArea.setText("Robot Stopped\n"+textArea.getText().substring(0,textArea.getText().length()>300?299:textArea.getText().length()));
            }
		});
	}
	@FXML
	public void onclickAlgo1() {
		Test t = new Test();
		Robot.getInstance().addListener(t);
		Map.getInstance().addListener(t);
	}
	@FXML
	public void onSensorSelected() {
			Robot.getInstance().setSensorSimulatorType((String) sensorCombo.getValue());
	}
	@FXML
	public void onclickEditObstacle() {
		BorderPane bp = new BorderPane();
		MapObstacleGUI summaryGUI = new MapObstacleGUI();
		bp.setCenter(summaryGUI);
		bp.setMaxWidth(100);
		bp.setMaxHeight(100);
		Scene scene2 = new Scene(bp, 400, 550);
	   Stage stage = new Stage();
	   stage.setTitle("Obstacles");
	   stage.setScene(scene2);
	   stage.show();
	}

	public void onKeyEvent(KeyEvent event) {
		if(event.getEventType().equals(KeyEvent.KEY_PRESSED)){
			Robot robot = Robot.getInstance();
	        if(event.getCode() == KeyCode.UP) {
	        	onClickMoveForward();
	        }
	        if(event.getCode() == KeyCode.RIGHT) {
	    		robot.rotate(90);
	        }
	        if(event.getCode() == KeyCode.LEFT) {
	    		robot.rotate(-90);
	        }
			
		}
	}
}
