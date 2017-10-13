package RPiInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import Data.Map;
import Data.Robot;
import algorithm.Exploration;
import algorithm.ExplorationFactory;
import algorithm.ExplorationType1;
import algorithm.FastestPath;
import algorithm.FastestPathFactory;
import algorithm.FastestPathType1;
import algorithm.FastestPathType2;
import application.MainController;
import util.HexBin;

public class RealRPIInterface extends RPiInterface implements Runnable{
	Socket socket;
	PrintWriter out;
	String address="127.0.0.1";
	int port=8088;
	
	MainController m;
	public void setM(MainController m) {
		this.m = m;
	}
	@Override
	public void startConnection() {
		Thread sensorSimulatorThread = new Thread(this);
		sensorSimulatorThread.start();
	}
	@Override
	public void disconnect() {
		if(socket!=null){
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void startConnection(String addressPort) {
		try{
			address = addressPort.split(":")[0];
			port = Integer.parseInt(addressPort.split(":")[1]);
		}catch(Exception ex){
			
		}
		startConnection();
	}

	@Override
	public void run() {
		try {
			socket = new Socket(address,port);
			BufferedReader in =new BufferedReader( new InputStreamReader(socket.getInputStream()));
			//while(true){
			//	out.println(string);
			//}
			String inputVar;
			System.out.println("addresS: " + address);
			while((inputVar=in.readLine()) != null){ 
				//string input = in.readLine();
				//while((inputVar=in.readLine()) != null);
				inputMessage(inputVar);
			}

		} catch (ConnectException e1) {
			//e1.printStackTrace();
			m.connectionFailed();
		} catch (UnknownHostException e1) {
			//e1.printStackTrace();
			m.connectionFailed();
		} catch (IOException e1) {
			m.connectionFailed();
			//e1.printStackTrace();
		} 
		
	}
	
	//on received message, process
	@Override
	public void inputMessage(String string) {
		if(string.length()==0){
			return;
		}
		//[action status]|[explored map]|[explored obstacles]|[x,y,degree]|[s1,s2,s3,s4,s5]|[wpx,wpy]
		//
		string = string.trim();
		System.out.println("Received from RPi: "+string);
		String parameters[] = string.split("\\|");
		
		
		//Message data
		String action_status = "EX"; 				//EX-exploring, FP-fastestpath. status to run different algorithm
		float robotLocationX = Robot.getInstance().getPosX(); 					//by grid
		float robotLocationY = Robot.getInstance().getPosY(); 		 			//by grid
		float robotDirection = Robot.getInstance().getDirection(); ; 
		int wayPointX = -1;					//by grid
		int wayPointY = -1;					//by grid
		float[] sensorInfo = {0,0,0,0,0,0};	//block away from robot;
		
		
		action_status = parameters[0];
		if(parameters[1].length()>0){
			String robotLocation[] = parameters[1].split(",");
			robotLocationX = Float.parseFloat(robotLocation[0]);
			robotLocationY = Float.parseFloat(robotLocation[1]);
			robotDirection = Float.parseFloat(robotLocation[2]);
			if(robotLocationY<1||robotLocationX<1){
				robotLocationY=1;
				robotLocationX=1;
			}
		}
		if(parameters[2].length()>0){
		String sensorInfoString[] = parameters[2].split(",");
			sensorInfo[0] = Float.parseFloat(sensorInfoString[0]);
			sensorInfo[1] = Float.parseFloat(sensorInfoString[1]);
			sensorInfo[2] = Float.parseFloat(sensorInfoString[2]);
			sensorInfo[3] = Float.parseFloat(sensorInfoString[3]);
			sensorInfo[4] = Float.parseFloat(sensorInfoString[4]);
			sensorInfo[5] = Float.parseFloat(sensorInfoString[5]);
		}
		if(parameters.length>3&&parameters[3].length()>0){
			String waypointLocation[]= parameters[3].split(",");
			wayPointX = Integer.parseInt(waypointLocation[0]);
			wayPointY = Integer.parseInt(waypointLocation[1]);
		}
		
		//set robot , way point , compute sensor
		setRobotLocation(robotLocationX,robotLocationY,robotDirection);
		setWayPoint(wayPointX,wayPointY);
		computeSensor(robotLocationX,robotLocationY,robotDirection,sensorInfo);
		Exploration explorationAlgorithm= ExplorationFactory.getInstance();
		
		
		
		//compute algo
		Message message;
		String hexExplored;
		String hexExploredObstacle;
		switch(action_status){
		case "EX": case "TE":
			if(action_status.equals("TE")){
				explorationAlgorithm.terminate();
			}
			
			message= explorationAlgorithm.start();
			//prepare return message
			hexExplored=HexBin.BinTohex(Map.getInstance().getBinaryExplored());
			hexExploredObstacle=HexBin.BinTohex("11111111"+Map.getInstance().getBinaryExploredObstacle()).substring(2);
			message.setExploredTile(hexExplored);
			message.setExploredObstacle(hexExploredObstacle);
			message.setStatus("EX");
			//send out message
			outputMessage(message.getMessage());
			break;

		case "FP":
			FastestPath fpAlgo =  FastestPathFactory.getInstance();
			message = fpAlgo.start();
			//prepare return message
			hexExplored=HexBin.BinTohex(Map.getInstance().getBinaryExplored());
			hexExploredObstacle=HexBin.BinTohex("11111111"+Map.getInstance().getBinaryExploredObstacle()).substring(2);
			message.setExploredTile(hexExplored);
			message.setExploredObstacle(hexExploredObstacle);
			message.setStatus(action_status);
			//send out message
			
			outputMessage(message.getMessage());
			break;
		}
	}
	
	//sending message to Rpi
	@Override
	public void outputMessage(String string) {
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			System.out.println("Sent to RPi: "+string);
			out.println(string);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
