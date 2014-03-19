package net;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
//UDP import java.net.DatagramSocket;

import java.util.Scanner;

import scn.MultiPlayerGame;


public class NetworkGame {
	
	public static ServerSocket server;
	
	public static Socket client;
	
	public static Socket socket;
	
	public static String serverIP = "144.32.179.129";
	
	public static String connectIP = "144.32.179.129";
	
	public static int port = 25560;
	
	public MultiPlayerGame multiPlayerGame;
	
	
	
	public NetworkGame(boolean host) {
		
		
		try {
			multiPlayerGame = MultiPlayerGame.createMultiPlayerGame(null, null); 
			
			ObjectOutputStream outStream = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());
			
			if (host) {
				System.out.println("Hosting...");
				server = new ServerSocket(port, 4, InetAddress.getByName(serverIP));
				System.out.println("Ready!\nAwaiting client...");
				client = server.accept();
				System.out.println("Client connected!\nSetting up game...");
			
				System.out.println("Streams set up!");
				
				new ThreadSend(multiPlayerGame, outStream);
				//new ThreadRecieve(multiPlayerGame, inStream);
				System.out.println("Creating multiplayer game!");	
			} else {
				System.out.println("Connecting...");
				socket = new Socket(connectIP, port);
				System.out.println("Connected!\nBuffering...");
				inStream = new ObjectInputStream(socket.getInputStream());
				outStream = new ObjectOutputStream(socket.getOutputStream());
				System.out.println("Buffered\nPinging for 256 bytes...");
				outStream.flush();
				System.out.println("Starting threads...");
				new ThreadRecieve(multiPlayerGame, inStream);
				//new ThreadSend(multiPlayerGame, outStream);
				System.out.println("Started!");
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
		new NetworkGame(isHost);
		
		input.close();
	}
	
}
