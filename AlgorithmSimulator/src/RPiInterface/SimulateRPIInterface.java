package RPiInterface;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import Data.Map;
import Data.Robot;
import algorithm.Exploration;
import algorithm.ExplorationFactory;
import algorithm.ExplorationType1;
import algorithm.FastestPath;
import algorithm.FastestPathType1;
import algorithm.FastestPathType2;
import util.HexBin;

public class SimulateRPIInterface extends RPiInterface implements Runnable{
	//simulate start server connection with rpi
	@Override
	public void startConnection() {
		try {
			InetAddress ip=InetAddress.getByName("172.22.212.255");
			DatagramSocket socket = new DatagramSocket();
			byte[] send_data=new byte[1024];
	        String string=new String("Marathe Ajinkya Avinash,TE1,172.21.145.224");
	        send_data=string.getBytes();
	        DatagramPacket request=new DatagramPacket(send_data,send_data.length,ip,8088);
            socket.send(request);
            socket.close();



		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void startConnection(String address) {
		startConnection() ;
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
		//[action status]|[explored map]|[explored obstacles]|[x,y,degree]|[s1,s2,s3,s4,s5]|[wpx,wpy]
		//
		string = string.trim();
		System.out.println("Received from RPi: "+string);
		String parameters[] = string.split("\\|");
		
		
		//Message data
		String action_status = "EX"; 				//EX-exploring, FP-fastestpath. status to run different algorithm
		float robotLocationX = 1; 			//by grid
		float robotLocationY = 1; 			//by grid
		float robotDirection = Robot.DIRECTION_NORTH; 
		int wayPointX = -1;					//by grid
		int wayPointY = -1;					//by grid
		int sensorInfo[] = {0,0,0,0,0,0};	//block away from robot;
		
		
		action_status = parameters[0];
		if(parameters[1].length()>0){
			String robotLocation[] = parameters[1].split(",");
			robotLocationX = Float.parseFloat(robotLocation[0]);
			robotLocationY = Float.parseFloat(robotLocation[1]);
			robotDirection = Float.parseFloat(robotLocation[2]);
		}
		if(parameters[2].length()>0){
		String sensorInfoString[] = parameters[2].split(",");
			sensorInfo[0] = Integer.parseInt(sensorInfoString[0]);
			sensorInfo[1] = Integer.parseInt(sensorInfoString[1]);
			sensorInfo[2] = Integer.parseInt(sensorInfoString[2]);
			sensorInfo[3] = Integer.parseInt(sensorInfoString[3]);
			sensorInfo[4] = Integer.parseInt(sensorInfoString[4]);
			sensorInfo[5] = Integer.parseInt(sensorInfoString[5]);
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
			Robot.getInstance().setSensorSimulatorType("type1", false);
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
			FastestPath fpAlgo = new FastestPathType2();
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
		System.out.println("Sent to RPi: "+string);
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

}
