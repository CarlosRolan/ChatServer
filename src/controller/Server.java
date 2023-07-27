package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.chat.Chat;
import com.comunication.ApiCodes;
import com.comunication.Connection;
import com.comunication.Msg;

import api.RequestHandler;
import controller.connection.env.Enviroment;

public class Server implements Enviroment, ApiCodes {

	private static Server instance;

	public static Server getInstance() {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

	private List<Connection> allOnlineCon = new ArrayList<>();
	private List<Chat> allChats = new ArrayList<>();

	private ServerSocket serverSocket = null;

	// Constructor
	private Server() {
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
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

	public List<Connection> getOnlineCon() {
		return allOnlineCon;
	}

	public List<Chat> getAllChats() {
		return allChats;
	}

	public void registerConnection(Connection c) {
		allOnlineCon.add(c);
	}

	public void deleteConnection(Connection c) {
		allOnlineCon.remove(c);
	}

	public void registerChat(Chat chat) {
		allChats.add(chat);
	}

	public void deleteChat(Chat chat) {
		allChats.remove(chat);
	}

	public int getNumberOfOnlineUsers() {
		return allOnlineCon.size();
	}

	public int getNumberOfChats() {
		return allChats.size();
	}

	public Connection getClientConnectionById(int userId) {
		for (Connection iter : allOnlineCon) {
			if (userId == iter.getId()) {
				return iter;
			}
		}
		return null;
	}

	public void handleRequest(Msg msg, Connection currentClient) {

		Msg respond = null;

		switch (msg.getAction()) {

			case REQ_SHOW_ALL_CON:
				respond = new RequestHandler().showOnlineUsers(currentClient);
				break;

			case REQ_SHOW_ALL_CHAT:
				respond = new RequestHandler().showAllChats(currentClient.getConId());
				break;

			case REQ_SINGLE:
				respond = new RequestHandler().askForSingle(currentClient.getConId(), msg.getReceptor(), msg.getBody());
				break;

			case REQ_ALLOW:
				respond = new RequestHandler().allowSingleChat(msg.getReceptor(), currentClient.getConId(), currentClient.getNick());
				break;

			case MSG_SINGLE_MSG:
				new RequestHandler().sendSingleMsg(msg.getEmisor(), msg.getReceptor(), msg.getBody());
				break;

			case REQ_EXIT_SINGLE:
				new RequestHandler().exitSigle(currentClient.getConId(), msg.getReceptor());
				break;

			case REQ_CHAT:
				break;
		}

		if (respond != null) {
			currentClient.writeMessage(respond);
		}
	}
}
