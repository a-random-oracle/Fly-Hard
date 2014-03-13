package net;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
//UDP import java.net.DatagramSocket;

import java.util.Scanner;

import scn.Game.DifficultySetting;
import cls.Aircraft;
import cls.Waypoint;

public class AircraftGame {
	
	public static ServerSocket server;
	
	public static Socket client;
	
	public static Socket socket;
	
	public static String serverIP = "PUT ADDRESS HERE";
	
	public static String connectIP = "PUT ADDRESS HERE";
	
	public static int port = 25560;
	
	
	
	public AircraftGame(boolean host) {
		Waypoint[] waypointList = new Waypoint[]{
				new Waypoint(0, 0, true, 0),
				new Waypoint(100, 100, true, 0),
				new Waypoint(25, 75, false, 0),
				new Waypoint(75, 25, false, 0),
				new Waypoint(50, 50, false, 0)};
		
		Aircraft testAircraft = new Aircraft("testAircraft", "Berlin", "Dublin",
				new Waypoint(100, 100, true, 0), new Waypoint(0, 0, true, 0),
				null, 10.0, waypointList, DifficultySetting.MEDIUM, null);
		
		Aircraft testAircraft2 = new Aircraft("testAircraft2", "Dublin", "Berlin",
				new Waypoint(0, 0, true, 0), new Waypoint(100, 100, true, 0),
				null, 10.0, waypointList, DifficultySetting.MEDIUM, null);
		
		try {
			if (host) {
				System.out.println("Hosting...");
				server = new ServerSocket(port, 4, InetAddress.getByName(serverIP));
				System.out.println("Ready!\nAwaiting client...");
				client = server.accept();
				System.out.println("Client connected!\nSetting up game...");
				
				ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(client.getInputStream());
				
				System.out.println("Streams set up!");
				
				new ThreadSend(testAircraft, out);
				new ThreadRecieve(testAircraft, in);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Get host choice here:
	 * 
	 * 
	 * 
	 * 
	 */
	
	/*
	 * Placeholder host selection. Just to test sending and 
	 * receiving an aircraft. Print results to console
	 */
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		System.out.println("Are you host? Y/N");
		boolean isHost = input.nextLine().toLowerCase().startsWith("y");
		
		if(isHost){
			System.out.println("Will host on "+serverIP+":"+port);
		}else{
			System.out.println("Will join to "+connectIP+":"+port);
		}
		new AircraftGame(isHost);
	}
	
}
