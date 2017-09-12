package RPiInterface;

import java.util.Scanner;

import Data.Map;
import Data.Robot;
import algorithm.Exploration;
import algorithm.ExplorationType1;
import util.HexBin;

public class SimulateRPIInterface extends RPiInterface implements Runnable{
	//simulate start server connection with rpi
	@Override
	public void startConnection() {
		Thread sensorSimulatorThread = new Thread(this);
		sensorSimulatorThread.start();
	}

	//simulate rpi message
	@Override
	public void run() {
		System.out.print("Listening to RPi: ");
		Scanner sc = new Scanner(System.in);
		String input = "";
		while(!input.equals("esc")){
			input = sc.nextLine();
			inputMessage(input);
		}
	}
	
	//on received message, process
	@Override
	public void inputMessage(String string) {
		System.out.println("Received from RPi: "+string);
		
		//Message data
		String status = "EX"; 				//EX-exploring, FP-fastestpath. status to run different algorithm
		boolean terminateExploring = false; //status to end exploration, when android input terminate
		int robotLocationX = 8; 			//by grid
		int robotLocationY = 11; 			//by grid
		float robotDirection = Robot.DIRECTION_EAST; 
		int wayPointX = 7;					//by grid
		int wayPointY = 11;					//by grid
		int sensorInfo[] = {3,5,1,2,0};		//block away from robot
		String exploredTile="ffe07fc0ff81ff03fe07fc0ff81ff03fe07ff8ffe1ffe07fc0dc01b800600000000000000003";
		String exploredObstacle="00000000000100000000000001000200027f";
		
		
		//1. set map & obstacles
		setMap(exploredTile,exploredObstacle);
		
		//2. set robot 
		setRobotLocation(robotLocationX,robotLocationY,robotDirection);

		//3. set way point 
		setWayPoint(wayPointX,wayPointY);
		
		//4. compute sensor
		computeSensor(robotLocationX,robotLocationY,robotDirection,sensorInfo);
		
		//5. compute algo
		switch(status){
		case "EX":
			Robot.getInstance().setSensorSimulatorType("type1", false);
			Exploration explorationAlgorithm = new ExplorationType1(terminateExploring);
			AlgoReturnMessage message= explorationAlgorithm.start();
			
			String hexExplored=HexBin.BinTohex(Map.getInstance().getBinaryExplored());
			String hexExploredObstacle=HexBin.BinTohex("11111111"+Map.getInstance().getBinaryExploredObstacle()).substring(2);
			message.setExploredTile(hexExplored);
			message.setExploredObstacle(hexExploredObstacle);
			outputMessage(message.toString());
			break;

		case "FP":
			break;
		}
	}
	
	//sending message to Rpi
	@Override
	public void outputMessage(String string) {
		System.out.println("Sent to RPi: "+string);
	}
}
