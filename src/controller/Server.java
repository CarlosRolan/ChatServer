package controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

	private ArrayList<ClientChannel> allChannels;
	private ServerSocket serverSocket;
	// Constructor
	private Server() {
		try {
			this.serverSocket = new ServerSocket(PORT);
			this.allChannels = new ArrayList<>();
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

	public ArrayList<ClientChannel> getChannels() {
		return this.allChannels;
	}
	public void registerConnection(ClientChannel c) {
		this.allChannels.add(c);
	}
	public void deleteConnection(ClientChannel c) {
		this.allChannels.remove(c);
	}


}
