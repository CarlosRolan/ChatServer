package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import controller.connection.ClientConnection;
import controller.connection.env.Enviroment;

public class Server implements Enviroment {

	private static Server instance;

	public static Server getInstance() {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

	private ArrayList<ClientConnection> allOnlineCon = new ArrayList<>();

	private ServerSocket serverSocket = null;

	// Constructor
	private Server() {
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.out.println(e);
			System.out.println("IOException");
		}
	}

	public Socket listenConnections() {
		System.out.println("LISTENING CONEECTIONS ON PORT " + PORT);
		try {
			return serverSocket.accept();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @return the allOnlineCon
	 */
	public ArrayList<ClientConnection> getOnlineCon() {
		return allOnlineCon;
	}

	public void registerConnection(ClientConnection c) {
		allOnlineCon.add(c);
	}

	public void deleteConnection(ClientConnection c) {
		allOnlineCon.remove(c);
	}

	public int getNumberOfOnlineUsers() {
		return allOnlineCon.size();
	}
}
