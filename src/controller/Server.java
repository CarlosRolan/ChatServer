package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import connection.ClientChannel;


public class Server implements Enviroment {

	private static Server instance;
	public static Server getInstance() {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

	private ArrayList<ClientChannel> allOnlineUsers;
	private ServerSocket serverSocket;
	// Constructor
	private Server() {
		try {
			this.serverSocket = new ServerSocket(PORT);
			this.allOnlineUsers = new ArrayList<>();
		} catch (IOException e) {
		}
	}

	public Socket listenConnections() {
		System.out.println("LISTENING CONEECTIONS ON PORT " + PORT);
		try {
			return this.serverSocket.accept();
		} catch (IOException e) {
			return null;
		}
	}

	public ArrayList<ClientChannel> getOnlineChannels() {
		return this.allOnlineUsers;
	}

	public ClientChannel getOnlineUser(String nick) {
		for (ClientChannel iter : allOnlineUsers) {
			if (iter.getNick().equals(nick))
			return iter;
		} return null;
	} 
	public void registerConnection(ClientChannel c) {
		this.allOnlineUsers.add(c);
	}
	public void deleteConnection(ClientChannel c) {
		this.allOnlineUsers.remove(c);
	}


}
