package Data;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import RPiInterface.RobotSensorSimulator;
import RPiInterface.RobotSensorSimulatorFactory;
import RPiInterface.RobotSensorSimulatorType1;
import RPiInterface.RobotSensorSimulatorType2;
import RobotMovement.ForwardMovement;
import RobotMovement.RotateMovement;

public class Robot {
	public static Robot robot=null;
	public static Robot getInstance(){
		if(robot==null){
			robot= new Robot();
		}
		return robot;
	}

	public Robot(){
		/*
		RobotSensorSimulator sensorSimulator = new RobotSensorSimulatorType1();
		Thread sensorSimulatorThread = new Thread(sensorSimulator);
		sensorSimulatorThread.start();
		*/
		setSensorSimulatorType(RobotSensorSimulatorFactory.SENSOR_TYPE_1);
	}
	
	
	private static final int framePer10CM = 30;
	private static final int framePerRotate = 30;
	private static  int milisecondPer10CM = 500;

	private static  int milisecondPerRotate = 500;

	private boolean isExploring =false;
	private boolean isMoving =false;
	
	private RobotSensorSimulator sensorSimulator = null;

	public RobotSensorSimulator getSensorSimulator() {
		return sensorSimulator;
	}
	public void setSensorSimulatorType(String value) {
		if(sensorSimulator!=null){
			sensorSimulator.stop();
		}
		sensorSimulator = RobotSensorSimulatorFactory.getInstance(value);
		if(sensorSimulator!=null){
			Thread sensorSimulatorThread = new Thread(sensorSimulator);
			sensorSimulatorThread.start();
		}
		updateListener();
	}

	private float posX=1f;
	private float posY=1f;
	private float direction =0;


	public float getPosX() {
		return posX;
	}

	public void setPosX(float posX) {
		this.posX = posX;
		updateListener();
	}

	public float getPosY() {
		return posY;
	}

	public void setPosY(float posY) {
		this.posY = posY;
		updateListener();
	}

	public static void setMilisecondPer10CM(int milisecondPer10CM) {
		Robot.milisecondPer10CM = milisecondPer10CM;
	}

	public static void setMilisecondPerRotate(int milisecondPerRotate) {
		Robot.milisecondPerRotate = milisecondPerRotate;
	}
	public boolean isMoving() {
		return isMoving;
	}

	public void setMoving(boolean isMoving) {
		this.isMoving = isMoving;
		if(isMoving==false){
			for(RobotListener a: arr){
				a.onRobotStop();
			}
		}
	}
	public float getDirection() {
		return direction;
	}
	public void setDirection(float direction) {
		this.direction = direction%360;
		updateListener();
	}
	

	public ForwardMovement forwardProcessor=null;
	public RotateMovement rotateMovementProcessor=null;
	public void moveForward(float cm){
			direction = direction%360;
		    double radians = Math.toRadians(direction);
			float x =  ((cm/10f)*(float)Math.sin(radians));
			float y =  ((cm/10f)*(float)Math.cos(radians));
			forwardProcessor = new ForwardMovement(x,y, framePer10CM,  milisecondPer10CM);
			Thread forwardMovementThread = new Thread(forwardProcessor);
			forwardMovementThread.start();
	}

	public Semaphore robotSemaphore = new Semaphore(1);
	public void rotate(float degree){
			direction = direction%360;
			rotateMovementProcessor = new RotateMovement(degree,framePerRotate, milisecondPerRotate);
			Thread rotateMovementThread = new Thread(rotateMovementProcessor);
			rotateMovementThread.start();
	}
	
	private  ArrayList<RobotListener> arr = new ArrayList<RobotListener>();
	public  void addListener(RobotListener listener){
		arr.add(listener);
	}
	public  void updateListener(){
		for(RobotListener a: arr){
			a.updateRobot();
		}
	}
	public void stopMovement() {
		if(forwardProcessor!=null)
			forwardProcessor.stop();
		if(rotateMovementProcessor!=null)
			rotateMovementProcessor.stop();
	}

}
