package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.api.Codes;
import com.chat.Chat;
import com.chat.Member;
import com.controller.Connection;
import com.controller.handlers.IMSGHandler;
import com.controller.handlers.IPKGHandler;
import com.data.MSG;
import com.data.PKG;

import api.RequestHandler;

public class Server implements Enviroment, Codes {

	private volatile static Server instance;

	synchronized public static Server getInstance() {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

	// can be MSG or PKG
	public Object respond = null;

	private final List<Connection> allOnlineCon = new ArrayList<>();
	private final List<Chat> allChats = new ArrayList<>();

	private ServerSocket serverSocket = null;

	/* CONSTRUCTOR */
	private Server() {
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.out.println("IOException");
		}
	}

	/* PUBLIC METHODS */
	public ClientCon initChannel(Socket socket) {
		return new ClientCon(socket, pMSG_HANDLER, pPCKG_HANDLER);
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

	/* IMPLEMENTATION */
	// MSG
	public final IMSGHandler pMSG_HANDLER = new IMSGHandler() {

		@Override
		public void handleError(MSG error) {
			respond = null;

			switch (error.getAction()) {
				default:
					System.out.println(WARN_UREGISTERED_MSG_ERROR_ACTION);
					break;
			}
		}

		@Override
		public void handleMessage(MSG msg) {
			respond = null;
			System.out.println(msg.toString());

			switch (msg.getAction()) {

				case MSG_TO_SINGLE:
					// emisorId = msg.getEmisor
					// emisorNick = msg.getParameter(0)
					// receptorId = msg.getReceptor()
					// msgText = msg.getBody()
					new RequestHandler().sendSingleMsg(msg.getEmisor(), msg.getParameter(0), msg.getReceptor(),
							msg.getBody());
					break;

				case MSG_TO_CHAT:
					System.out.println(msg.toString());

					/*
					 * msgOut.setAction(MSG_TO_CHAT);
					 * msgOut.setEmisor(emisorId);
					 * msgOut.setReceptor(chatId);
					 * msgOut.setBody(line);
					 */

					Chat chat = getChatById(msg.getReceptor());
					new RequestHandler().sendMsgToChat(chat, msg.getEmisor(), msg.getBody());
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
					// String emisorId = msg.getEmisor();
					respond = new RequestHandler().showOnlineUsers(msg.getEmisor());
					break;

				case REQ_SHOW_ALL_CHAT:
					respond = new RequestHandler().showAllChats();
					break;

				case REQ_SHOW_ALL_MEMBERS_OF_CHAT:
					// chatId = msg.getEmisor();
					// Chat selected = getChatById(chatId);
					respond = new RequestHandler().showAllMemberOfChat(getChatById(msg.getEmisor()));
					break;

				case REQ_SINGLE:
					// requesterid = msg.getEmisor();
					// candidateId = msg.getReceptor();
					// requesterNIck = msg.getBody();
					respond = new RequestHandler().askForSingle(msg.getEmisor(), msg.getReceptor(), msg.getBody());
					break;

				case REQ_ALLOW:
					// requesterId = msg.getReceptor();
					// requestedId = msg.getEmisor();
					// requestedNick = msg.getBody();
					respond = new RequestHandler().allowSingleChat(msg.getReceptor(), msg.getEmisor(),
							msg.getBody());
					break;

				case REQ_EXIT_SINGLE:
					// emisorId = msg.getEmisor();
					// receptorId = msg.getReceptor();
					new RequestHandler().exitSigle(msg.getEmisor(), msg.getReceptor());
					break;

				case REQ_CREATE_CHAT:
					Chat newChat = Chat.createChat(msg);
					registerChat(newChat);
					break;

				case REQ_CHAT:
					// chatId = msg.getReceptor
					Chat selected = getChatById(msg.getReceptor());
					respond = new RequestHandler().sendChatInstance(selected);
					break;

				case REQ_UPDATE_STATE:
					// emisorId = msg.getEmisor();
					respond = new RequestHandler().sendStateUpdate(msg.getEmisor());
					break;

				case REQ_ADD_MEMBER:
					Chat updatedChat = getChatById(msg.getEmisor());
					Connection selectedCon = getConnectionById(msg.getReceptor());
					if (selectedCon != null) {
						Member newMember = Member.newMember(selectedCon, msg.getParameter(0));
						updatedChat.addMember(newMember);
						respond = new RequestHandler().sendChatInstance(updatedChat);
						try {
							selectedCon.write(respond);
						} catch (SocketException e) {
							System.err.println("SocketException" + REQ_ADD_MEMBER + respond.toString());
							e.printStackTrace();
						} catch (IOException e) {
							System.err.println("IOException" + REQ_ADD_MEMBER + respond.toString());
							e.printStackTrace();
						}
					} else {
						respond = new RequestHandler().sendConInstance(null);
					}

					break;

				default:
					System.err.println(WARN_UNREGISTERED_MSG_MESSAGE_ACTION);
			}
		}

		@Override
		public void unHandledMSG(MSG arg0) {
			throw new UnsupportedOperationException("Unimplemented MSG TYPE");
		}
	};
	// PKG
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
			throw new UnsupportedOperationException("Unimplemented PKG TYPE");
		}

	};

}
