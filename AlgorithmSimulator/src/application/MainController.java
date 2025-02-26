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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import Data.Map;
import Data.Position;
import Data.Robot;
import Data.RobotListener;
import Data.WayPoint;
import GUI.FXMLController;
import GUI.MapGUI;
import GUI.MapObstacleGUI;
import RPiInterface.RPiInterface;
import RPiInterface.RealRPIInterface;
import RPiInterface.RobotSensorSimulatorFactory;
import RPiInterface.RobotSensorSimulatorType1;
import RPiInterface.RobotSensorSimulatorType2;
import RobotMovement.RobotMovement;
import algorithm.Exploration;
import algorithm.ExplorationFactory;
import algorithm.ExplorationType1;
import algorithm.ExplorationType2;
import algorithm.ExplorationWallerType1;
import algorithm.ExplorationWallerType2;
import algorithm.FastestPath;
import algorithm.FastestPathFactory;
import algorithm.FastestPathType1;
import algorithm.FastestPathType2;
import algorithm.ObstaclesConfident;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
	private Button connectToRpiBtn;
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
	private TextField rpmTextField;
	@FXML
	private TextField waypointYpos;
	@FXML
	private Label Timer;
	@FXML
	private TextField forwardTextField;
	@FXML
	private TextField rotateTextField;
	@FXML
	private TextField calibrateTextField;

	@FXML
	private CheckBox auto_cali_cb;
	
	@FXML
	private TextField travelSpeedTextField;
	@FXML
	private TextField rotateSpeedTextField;

	@FXML
	private TextField terminateTimeTextField;
	@FXML
	private TextField terminateRateTextField;
	@FXML
	private TextField terminateRateTextField1;
	@FXML
	private TextField textfield_address_port;
	@FXML
	private TextField rotateCostTextField;
	

	@FXML
	private TextArea textArea;
	@FXML
	private ComboBox sensorCombo;
	@FXML
	private ComboBox explrCombo;
	@FXML
	private ComboBox fpCombo;
	
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
	private void onclickResetSimulator(){
		ObstaclesConfident.obstacleCounter = new int[20][15];
		Robot robot = Robot.getInstance();
		if(explorationAlgorithm!=null){
			explorationAlgorithm.terminate();
			Map.getInstance().removeListener(explorationAlgorithm);
			robot.removeListener(explorationAlgorithm);
		}
		//set robot location
		robot.setPosX(1);
		robot.setPosY(1);
		robot.setDirection(0);
		WayPoint.getInstance().setPosition(null);
		Map.getInstance().setExploredTiles(new int[20][15]);
		
		//terminate rpi simulator 
		if(robot.getSensorSimulator()!=null)
			robot.getSensorSimulator().stop();
		if(robot.isExploring()){
			robot.setExploring(false);
		}
		
		//terminate any movement
		robot.stopAllMovement();
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		MapGUI mapGui = new MapGUI();
		Robot.getInstance().addListener(this);
		mapPane.setCenter(mapGui);
		//sensorCombo.setValue(RobotSensorSimulatorFactory.SENSOR_TYPE_2);
		sensorCombo.setValue(RobotSensorSimulatorFactory.SENSOR_TYPE_1);
		//explrCombo.setValue(ExplorationFactory.EX_WALL1);
		explrCombo.setValue(ExplorationFactory.EX_WALL5);
		fpCombo.setValue(FastestPathFactory.FP1);

		ExplorationFactory.setSelectedType(ExplorationFactory.EX_WALL5);
		FastestPathFactory.setSelectedType(FastestPathFactory.FP1);
	}
	
	

	@Override
	public void updateRobot() {
		Platform.runLater(new Runnable() {
            @Override public void run() {  
            	Robot r = Robot.getInstance();
            	textArea.setText("Robot coordinate x:"+r.getPosX()+" y:"+r.getPosY()+"\n"+textArea.getText().substring(0,textArea.getText().length()>300?299:textArea.getText().length()));

            	 robotXpos.setText(r.getPosX()+"");
            	 robotYpos.setText(r.getPosY()+"");
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
	public void onRPMChange() {
		int rpm = Integer.parseInt(rpmTextField.getText());
		int speed = (int) (((60f/(((float)rpm)*(6f*3.14f))))*10*1000);
		Robot.setMilisecondPer10CM(speed);
		Robot.setMilisecondPerRotate(speed*6);
	}
	@FXML
	public void onCalibrateIntervalChange() {
		int calibrate = Integer.parseInt(calibrateTextField.getText());
		Exploration.intervalForCalibrate = calibrate;
	}
	@FXML
	public void onrotateCostTextFieldChange() {
		int calibrate = Integer.parseInt(rotateCostTextField.getText());
		Exploration.rotationCost = calibrate;
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
			String binaryExplored=Map.getInstance().getBinaryExplored();
			String binaryExploredObstacle=Map.getInstance().getBinaryExploredObstacle();
			String binaryObstacle=Map.getInstance().getBinaryObstacle();
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
	
	Exploration explorationAlgorithm;
	@FXML
	public void onclickAlgo1() {
		Robot.getInstance().setSensorSimulatorType((String) sensorCombo.getValue(), true);
		explorationAlgorithm = ExplorationFactory.getInstance((String)explrCombo.getValue());
		Robot.getInstance().addListener(explorationAlgorithm);
		Map.getInstance().addListener(explorationAlgorithm);
		explorationAlgorithm.start();
	}
	@FXML
	private void onclickStopExploration(){
		Robot.getInstance().getInstructions().clear();
		if(explorationAlgorithm!=null)
			explorationAlgorithm.terminate();
	}
	@FXML
	public void onSensorSelected() {
		Robot.getInstance().setSensorSimulatorType((String) sensorCombo.getValue(), false);
	}
	@FXML
	public void onExplrCombo() {
		ExplorationFactory.setSelectedType((String) explrCombo.getValue());
		if(ExplorationFactory.getSelectedType().equals(ExplorationFactory.EX_WALL1)){

			sensorCombo.setValue(RobotSensorSimulatorFactory.SENSOR_TYPE_2);
		}
		if(ExplorationFactory.getSelectedType().equals(ExplorationFactory.EX_WALL2)){

			sensorCombo.setValue(RobotSensorSimulatorFactory.SENSOR_TYPE_3);
		}
		if(ExplorationFactory.getSelectedType().equals(ExplorationFactory.EX_WALL4)){

			sensorCombo.setValue(RobotSensorSimulatorFactory.SENSOR_TYPE_2);
		}
		if(ExplorationFactory.getSelectedType().equals(ExplorationFactory.EX_WALL5)){

			sensorCombo.setValue(RobotSensorSimulatorFactory.SENSOR_TYPE_1);
		}
	}
	@FXML
	public void onclick_cb() {
		if(auto_cali_cb.isSelected()){
			Exploration.arduinoAutoCalibrate=true;
		}else{
			Exploration.arduinoAutoCalibrate=false;
		}
	}
	
	
	@FXML
	public void onFpCombo() {
		FastestPathFactory.setSelectedType((String) fpCombo.getValue());
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

	Runnable timerTask=null;
	ScheduledExecutorService timerExecutor=null;
	@Override
	public void onRobotStartExploring() {
		timerTask = new Runnable() {
		    public void run() {
				Platform.runLater(new Runnable() {
		            @Override public void run() {  
		            	long currentTimeStamp = System.currentTimeMillis();
		            	long seconds = ((currentTimeStamp-Robot.getInstance().getExploringStartTime())/1000)%60;
		            	long minutes = ((currentTimeStamp-Robot.getInstance().getExploringStartTime())/1000-seconds)/60;
		            	float exploredRate = Map.getInstance().getExploredRate();
		            	Timer.setText("Time: "+minutes+":"+seconds +"  Coverage: "+exploredRate);	
		            }
				});
		        }
		};

		timerExecutor = Executors.newScheduledThreadPool(1);
		timerExecutor.scheduleAtFixedRate(timerTask, 0, 1, TimeUnit.SECONDS);
	}


	@FXML
	public void onclickFastestPath() {
		FastestPath fp =  FastestPathFactory.getInstance((String)fpCombo.getValue());
		fp.start();
	}
	@FXML
	public void onTerminateTimeTextField() {
		Exploration.setAutoTerminate_time(Integer.parseInt(terminateTimeTextField.getText()));
	}
	@FXML
	public void onTerminateRateTextField() {
		Exploration.setAutoTerminate_explore_rate(Integer.parseInt(terminateRateTextField.getText()));
	}
	@FXML
	public void onTerminateRateTextField1() {
		ExplorationWallerType1.autoTerminateAfterWallhug = (Integer.parseInt(terminateRateTextField1.getText()));
	}
	RPiInterface rpi = null;
	@FXML
	public void onclickConnect() {
		if(rpi!=null){
			rpi.disconnect();
		}
		String address = textfield_address_port.getText();
		rpi = new RealRPIInterface();
		if(rpi instanceof RealRPIInterface){
			((RealRPIInterface) rpi).setM(this);
		}
		rpi.startConnection(address);
	}
	public void connectionFailed(){

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				connectToRpiBtn.setText("Reconnect");
			}
		});
	}
	
	@Override
	public void onRobotStopExploring() {
		if(timerExecutor!=null)
			timerExecutor.shutdown();
	}
}
