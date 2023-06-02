package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import controller.chat.ChatReference;
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

	private ArrayList<ClientChannel> allOnlineUsers = new ArrayList<>();
	private ArrayList<ChatReference> chatReferences = new ArrayList<>();

	private ServerSocket serverSocket = null;

	

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

	public ClientChannel getOnlineUserByID(String userID) {
		long parsedUserID = Long.parseLong(userID);
		System.out.println(parsedUserID + " parsed ID value");
		for (ClientChannel iterable : allOnlineUsers) {
			if (iterable.getId() == parsedUserID) {
				return iterable;
			}
		}
		return null;
	}

	public ClientChannel getOnlineUserByNick(String userNick) {
		for (ClientChannel iterable : allOnlineUsers) {
			if (iterable.getNick().equals(userNick)) {
				return iterable;
			}
		}
		return null;
	}

	public void registerConnection(ClientChannel c) {
		this.allOnlineUsers.add(c);
	}

	public void deleteConnection(ClientChannel c) {
		this.allOnlineUsers.remove(c);
	}

	public int getNumberOfOnlineUsers() {
		return allOnlineUsers.size();
	}

	public ArrayList<ChatReference> getChatRefs() {
        return chatReferences;
    }

	public ChatReference getChatReferenceByID(long chatID) {
		for (ChatReference iterator : chatReferences) {
			if( iterator.getChatID() == chatID) {
				return iterator;
			}
		}
		return null;
	}


    public void registerChat(ChatReference chatRef) {
        this.chatReferences.add(chatRef);
    }

    public void updateChat(ChatReference chatRef) {
        chatReferences.remove(chatRef);
        chatReferences.add(chatRef);
    }

    public void deleteChat(ChatReference chatRef) {
        chatReferences.remove(chatRef);
    }

    public ChatReference getChatReference(String chatNick) {
        for (ChatReference iterator : chatReferences) {
            if (iterator.getChatName().equals(chatNick)) {
                return iterator;
            }
        }

        return null;
    }

}
