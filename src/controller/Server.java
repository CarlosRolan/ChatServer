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

	public List<Connection> getAllConnections() {
		return allOnlineCon;
	}

	public List<Chat> getAllChats() {
		return allChats;
	}

	public Connection getConnection(int conId) {
		return allOnlineCon.get(conId);
	}

	public Connection getConnection(String conId) {
		return allOnlineCon.get(Integer.parseInt(conId));
	}

	public void registerConnection(Connection c) {
		allOnlineCon.add(c);
	}

	public void deleteConnection(Connection c) {
		allOnlineCon.remove(c);
	}

	public Chat getChat(int chatId) {
		return allChats.get(chatId);
	}

	public Chat getChat(String position) {
		return allChats.get(Integer.parseInt(position));
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

	public Msg handleRequest(Msg msg) {

		Msg respond = null;

		switch (msg.getAction()) {

			case REQ_SHOW_ALL_CON:
				respond = new RequestHandler().showOnlineUsers(msg.getEmisor());
				break;

			case REQ_SHOW_ALL_CHAT:
				respond = new RequestHandler().showAllChats();
				break;

			case REQ_SINGLE:
				respond = new RequestHandler().askForSingle(msg.getEmisor(), msg.getReceptor(), msg.getBody());
				break;

			case REQ_ALLOW:
				respond = new RequestHandler().allowSingleChat(msg.getReceptor(), msg.getEmisor(), msg.getBody());
				break;

			case MSG_SINGLE_MSG:
				new RequestHandler().sendSingleMsg(msg.getEmisor(), msg.getReceptor(), msg.getBody());
				break;

			case REQ_EXIT_SINGLE:
				new RequestHandler().exitSigle(msg.getEmisor(), msg.getReceptor());
				break;

			case REQ_CREATE_CHAT:
				Chat chat = Chat.createChatAsAdmin(msg);
				registerChat(chat);
				respond = new RequestHandler().sendChatInstance(chat);
				break;
			case REQ_CHAT:
				respond = new RequestHandler().sendChatInstance(getChat(msg.getReceptor()));
				break;
		}
		return respond;
	}
}
