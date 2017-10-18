package Data;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import RPiInterface.RobotSensorSimulator;
import RPiInterface.RobotSensorSimulatorFactory;
import RPiInterface.RobotSensorSimulatorType1;
import RPiInterface.RobotSensorSimulatorType2;
import RobotMovement.ForwardMovement;
import RobotMovement.RobotMovement;
import RobotMovement.RotateMovement;

public class Robot {
	public static Robot robot=null;
	public static Robot getInstance(){
		if(robot==null){
			robot= new Robot();
		}
		return robot;
	}
	
	ArrayList<RobotMovement> instructions=new ArrayList<RobotMovement>();  
	
	public ArrayList<RobotMovement> getInstructions() {
		return instructions;
	}
	public Robot(){
		/*
		RobotSensorSimulator sensorSimulator = new RobotSensorSimulatorType1();
		Thread sensorSimulatorThread = new Thread(sensorSimulator);
		sensorSimulatorThread.start();
		*/
		//setSensorSimulatorType(RobotSensorSimulatorFactory.SENSOR_TYPE_1);
	}
	
	
	private static final int framePer10CM = 30;
	private static final int framePerRotate = 90;
	private static  int milisecondPer10CM = 265;
	private static  int milisecondPerRotate = 1590;

	
	private long exploringStartTime = 0;
	private long exploringEndTime = 0;
	private boolean isExploring =false;
	public boolean isExploring() {
		return isExploring;
	}
	public void setExploring(boolean isExploring) {
		if(isExploring){
			 exploringStartTime = System.currentTimeMillis();
		}else{
			exploringEndTime = System.currentTimeMillis();
		}
		this.isExploring = isExploring;
		updateExploringListener(isExploring);
	}
	private boolean isMoving =false;
	
	private RobotSensorSimulator sensorSimulator = new RobotSensorSimulatorType2();

	public RobotSensorSimulator getSensorSimulator() {
		return sensorSimulator;
	}
	public void setSensorSimulatorType(String value, boolean simulation) {
		if(sensorSimulator!=null){
			sensorSimulator.stop();
		}
		sensorSimulator = RobotSensorSimulatorFactory.getInstance(value);
		if(sensorSimulator!=null && simulation){
			Thread sensorSimulatorThread = new Thread(sensorSimulator);
			sensorSimulatorThread.start();
		}
		updateListener();
	}

	private float posX=1f;
	private float posY=1f;
	private float direction =0;
	public static final float DIRECTION_NORTH =0;
	public static final float DIRECTION_EAST =90;
	public static final float DIRECTION_SOUTH =180;
	public static final float DIRECTION_WEST =270;

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
	public long getExploringStartTime() {
		return exploringStartTime;
	}
	public void setExploringStartTime(long exploringStartTime) {
		this.exploringStartTime = exploringStartTime;
	}
	public long getExploringEndTime() {
		return exploringEndTime;
	}
	public void setExploringEndTime(long exploringEndTime) {
		this.exploringEndTime = exploringEndTime;
	}

	public void moveForward(float distance) {
			ForwardMovement forwardProcessor = new ForwardMovement(distance , framePer10CM,  milisecondPer10CM);
			instructions.add(forwardProcessor);
			if(instructions.size()==1){
				Thread forwardMovementThread = new Thread(forwardProcessor);
				forwardMovementThread.start();
			}
	}

	public void rotate(float degree) {
			RotateMovement rotateMovementProcessor = new RotateMovement(degree,framePerRotate, milisecondPerRotate);
			instructions.add(rotateMovementProcessor);
			if(instructions.size()==1){
				Thread forwardMovementThread = new Thread(rotateMovementProcessor);
				forwardMovementThread.start();
			}
	}
	
	public void calibrate (){
		moveForward(3);
		moveForward(-3);
		moveForward(1);
		moveForward(-1);
		moveForward(3);
		moveForward(-3);
		moveForward(1);
		moveForward(-1);
	}
	public void runNextInstruction(){
		if(instructions.size()>0){
			instructions.remove(0);
		}
		if(instructions.size()>0){
			RobotMovement movement = instructions.get(0);
			if(movement!=null){
				Thread forwardMovementThread = new Thread(movement);
				forwardMovementThread.start();
			}else{
				setMoving(false);
			}
		}else{
			setMoving(false);
		}
	}
	
	
	private  ArrayList<RobotListener> arr = new ArrayList<RobotListener>();
	public  void addListener(RobotListener listener){
		arr.add(listener);
	}public  void removeListener(RobotListener listener){
		arr.remove(listener);
	}
	public  void updateListener(){
		for(int i =0;i<arr.size();i++){
			RobotListener a = arr.get(i);
			a.updateRobot();
		}
	}
	public  void updateExploringListener(boolean isExploring){
		for(int i =0;i<arr.size();i++){
			RobotListener a = arr.get(i);
			if(isExploring){
				a.onRobotStartExploring();
			}else{
				a.onRobotStopExploring();
			}
		}
	}
	
	public void stopAllMovement(){
		if(getInstructions().size()>0){
			RobotMovement movement = getInstructions().get(0);
			getInstructions().clear();
			movement.stop();
		}
	}
}
