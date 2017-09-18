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
		System.out.println("Received from RPi: "+string);
		String parameters[] = string.split("|");
		//Message data
		String status = "EX"; 				//EX-exploring, FP-fastestpath. status to run different algorithm
		boolean terminateExploring = false; //status to end exploration, when android input terminate
		float robotLocationX = 8; 			//by grid
		float robotLocationY = 10; 			//by grid
		float robotDirection = Robot.DIRECTION_EAST; 
		int wayPointX = 1;					//by grid
		int wayPointY = 18;					//by grid
		int sensorInfo[] = {0,0,0,0,0};		//block away from robot
		String exploredTile="ffe07fc0ff81ff03fe07fc0ff81ff03fe07ff8ffe1ffe07fc0dc01b800600000000000000003";
		String exploredObstacle="00000000000100000000000001000200027f";
		//String exploredTile="ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
		//String exploredObstacle="000000000000000000000000000000000000000000000000000000001c00c00000000000000f";
		
		
		/*
		status = parameters[0];
		String robotLocation = parameters[1];
		robotLocationX = Float.parseFloat(robotLocation.split(",")[0]);
		robotLocationY = Float.parseFloat(robotLocation.split(",")[1]);
		robotDirection = Float.parseFloat(robotLocation.split(",")[2]);
		String waypointLocation = parameters[2];
		wayPointX = Integer.parseInt(waypointLocation.split(",")[0]);
		wayPointY = Integer.parseInt(waypointLocation.split(",")[1]);
		String sensorInfoString = parameters[3];
		sensorInfo[0] = Integer.parseInt(sensorInfoString.split(",")[0]);
		sensorInfo[1] = Integer.parseInt(sensorInfoString.split(",")[1]);
		sensorInfo[2] = Integer.parseInt(sensorInfoString.split(",")[2]);
		sensorInfo[3] = Integer.parseInt(sensorInfoString.split(",")[3]);
		sensorInfo[4] = Integer.parseInt(sensorInfoString.split(",")[4]);
		
		*/
		
		//F10,R90,F20,L180
		//1. set map & obstacles
		setMap(exploredTile,exploredObstacle);
		
		//2. set robot 
		setRobotLocation(robotLocationX,robotLocationY,robotDirection);

		//3. set way point 
		setWayPoint(wayPointX,wayPointY);
		
		//4. compute sensor
		computeSensor(robotLocationX,robotLocationY,robotDirection,sensorInfo);
		
		//5. compute algo
		Message message;
		String hexExplored;
		String hexExploredObstacle;
		switch(status){
		case "EX":
			//process algo
			Robot.getInstance().setSensorSimulatorType("type1", false);
			Exploration explorationAlgorithm = new ExplorationType1(terminateExploring);
			message= explorationAlgorithm.start();
			//prepare return message
			hexExplored=HexBin.BinTohex(Map.getInstance().getBinaryExplored());
			hexExploredObstacle=HexBin.BinTohex("11111111"+Map.getInstance().getBinaryExploredObstacle()).substring(2);
			message.setExploredTile(hexExplored);
			message.setExploredObstacle(hexExploredObstacle);
			message.setStatus(status);
			//send out message
			outputMessage(message.getMessage(Message.MESSAGE_HEADER_ARDUINO));
			outputMessage(message.getMessage(Message.MESSAGE_HEADER_ANDROID));
			break;

		case "FP":
			//process algo
			FastestPath fpAlgo = new FastestPathType2();
			message = fpAlgo.start();
			//prepare return message
			hexExplored=HexBin.BinTohex(Map.getInstance().getBinaryExplored());
			hexExploredObstacle=HexBin.BinTohex("11111111"+Map.getInstance().getBinaryExploredObstacle()).substring(2);
			message.setExploredTile(hexExplored);
			message.setExploredObstacle(hexExploredObstacle);
			message.setStatus(status);
			//send out message
			
			outputMessage(message.getMessage(Message.MESSAGE_HEADER_ARDUINO));
			outputMessage(message.getMessage(Message.MESSAGE_HEADER_ANDROID));
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
