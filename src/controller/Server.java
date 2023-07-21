package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import controller.connection.ClientChannel;
import controller.connection.env.Enviroment;

public class Server implements Enviroment {

	private static Server instance;

	public static Server getInstance() {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

	private ArrayList<ClientChannel> allOnlineCon = new ArrayList<>();

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
	public ArrayList<ClientChannel> getOnlineCon() {
		return allOnlineCon;
	}

	public void registerConnection(ClientChannel c) {
		allOnlineCon.add(c);
	}

	public void deleteConnection(ClientChannel c) {
		allOnlineCon.remove(c);
	}

	public int getNumberOfOnlineUsers() {
		return allOnlineCon.size();
	}

	public ClientChannel getClientConnectionById(int userId) {
		for (ClientChannel iter : allOnlineCon) {
			if (userId == iter.getId()) {
				return iter;
			}
		}
		return null;
	}
}
