package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.api.Codes;
import com.chat.Chat;
import com.chat.ChatBuilder;
import com.chat.Member;
import com.controller.Connection;
import com.controller.handlers.IMSGHandler;
import com.controller.handlers.IPKGHandler;
import com.data.MSG;
import com.data.PKG;

import api.RequestHandler;

public class Server implements Enviroment, Codes {

	private static Server instance;

	public static Server getInstance() {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

	public Object respond = null;

	public ClientCon initChannel(Socket socket) {
		return new ClientCon(socket, pMSG_HANDLER, pPCKG_HANDLER);
	}

	private List<Connection> allOnlineCon = new ArrayList<>();
	private List<Chat> allChats = new ArrayList<>();

	public final IMSGHandler pMSG_HANDLER = new IMSGHandler() {

		@Override
		public void handleError(MSG error) {
			respond = null;

			switch (error.getAction()) {
				default:
					System.out.println(WARN_UNREGISTERED_PKG_MIXED_ACTION);
					break;
			}

		}

		@Override
		public void handleMessage(MSG msg) {
			respond = null;

			switch (msg.getAction()) {

				case MSG_TO_SINGLE:
					new RequestHandler().sendSingleMsg(msg.getEmisor(), msg.getParameter(0), msg.getReceptor(),
							msg.getBody());
					break;

				case MSG_TO_CHAT:
					Chat chat = getChatById(msg.getReceptor());
					new RequestHandler().sendMsgToChat(chat, msg.getEmisor(), msg.getParameter(0), msg.getBody());
					break;

				default:
					System.err.println(WARN_UNREGISTERED_MSG_MESSAGE_ACTION);
					break;
			}

		}

		@Override
		public void handleRequest(MSG msg) {
			respond = null;

			switch (msg.getAction()) {

				case REQ_SHOW_ALL_CON:
					respond = new RequestHandler().showOnlineUsers(msg.getEmisor());
					break;

				case REQ_SHOW_ALL_CHAT:
					respond = new RequestHandler().showAllChats();
					break;

				case REQ_SHOW_ALL_MEMBERS_OF_CHAT:
					respond = new RequestHandler().showAllMemberOfChat(getChatById(msg.getEmisor()));
					break;

				case REQ_SINGLE:
					respond = new RequestHandler().askForSingle(msg.getEmisor(), msg.getReceptor(), msg.getBody());
					break;

				case REQ_ALLOW:
					respond = new RequestHandler().allowSingleChat(msg.getReceptor(), msg.getEmisor(),
							msg.getBody());
					break;

				case REQ_EXIT_SINGLE:
					new RequestHandler().exitSigle(msg.getEmisor(), msg.getReceptor());
					break;

				case REQ_CREATE_CHAT:
					Chat newChat = ChatBuilder.createChatAsAdmin(msg);
					registerChat(newChat);
					respond = new RequestHandler().sendChatInstance(newChat);
					break;

				case REQ_CHAT:
					Chat selected = getChatById(msg.getReceptor());
					respond = new RequestHandler().sendChatInstance(selected);
					break;

				case REQ_UPDATE_STATE:
					respond = new RequestHandler().sendStateUpdate(msg.getEmisor());
					break;

				case REQ_ADD_MEMBER:
					Chat updatedChat = getChatById(msg.getEmisor());
					Connection selectedCon = getConnectionById(msg.getReceptor());
					if (selectedCon != null) {
						Member newMember = ChatBuilder.newMember(selectedCon, msg.getParameter(0));
						updatedChat.addMember(newMember);
						respond = new RequestHandler().sendChatInstance(updatedChat);
						try {
							selectedCon.write(respond);
						} catch (SocketException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						respond = new RequestHandler().sendConInstance(null);
					}

					break;

				default:
					System.err.println(WARN_UNREGISTERED_MSG_REQUEST_ACTION);
					break;
			}

		}

		@Override
		public void unHandledMSG(MSG arg0) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'unHandledMSG'");
		}
	};
	public final IPKGHandler pPCKG_HANDLER = new IPKGHandler() {

		@Override
		public void handleCollection(PKG arg0) {
			respond = null;
		}

		@Override
		public void handleMixed(PKG arg0) {
			respond = null;
		}

		@Override
		public void unHandledPKG(PKG arg0) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'unHandledPKG'");
		}

	};

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

	public void registerConnection(Connection c) {
		allOnlineCon.add(c);
	}

	public void deleteConnection(Connection c) {
		allOnlineCon.remove(c);
	}

	public Chat getChatById(String chatId) {
		for (Chat chat : allChats) {
			if (chatId.equals(chat.getChatId())) {
				return chat;
			}
		}

		return null;
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

	public Connection getConnectionById(int userId) {
		for (Connection iter : allOnlineCon) {
			if (userId == iter.getId()) {
				return iter;
			}
		}
		return null;
	}

	public Connection getConnectionById(String userId) {

		for (Connection iter : allOnlineCon) {
			if (userId.equals(iter.getConId())) {
				return iter;
			}
		}
		return null;
	}

}
