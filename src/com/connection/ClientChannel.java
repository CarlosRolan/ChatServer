package com.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.chats.Chat;
import com.controller.Message;
import com.controller.Request;
import com.controller.Server;

public class ClientChannel extends Thread implements ConStatusCodes {

    private Socket pSocket = null;
    private String nick;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private boolean chatting = false;

    public boolean isChatting() {
        return chatting;
    }

    public void setChatting(boolean stateChatting) {
        chatting = stateChatting;
    }

    public String getNick() {
        return nick;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public void setOis(ObjectInputStream newOis) {
        ois = newOis;
    }

    public void SetOos(ObjectOutputStream newOos) {
        oos = newOos;
    }

    // Constructor
    public ClientChannel(Socket socket) {

        pSocket = socket;

        try {
            oos = new ObjectOutputStream(pSocket.getOutputStream());
            ois = new ObjectInputStream(pSocket.getInputStream());

            if (presenting()) {
                sendComfirmation();
            } else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean presenting() {
        Message presentation = readClientMessage();
        System.out.println(presentation.requestInfo());
        if (presentation.getAction().equals(Request.PRESENTATION)) {
            nick = presentation.getEmisor();
            return true;
        } else {
            return false;
        }
    }

    private void sendComfirmation() {
        Message comfirmation = new Message(PRESENTATION_SUCCES, "SERVER", nick);
        writeClientMessage(comfirmation);
        System.out.println(comfirmation.requestInfo());
    }

    public void writeClientMessage(Message msg) {
        try {
            oos.writeObject(msg);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message readClientMessage() {
        try {
            return (Message) ois.readObject();
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found on method readClientMesssage");
            // e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.out.println("IOEXception ");
            // e.printStackTrace();
            return null;
        }
    }

    public void handleRequest(Message msg) {

        switch (msg.getAction()) {
            case Request.SHOW_ALL_ONLINE:
                new Request().showOnlineUsers(this);
                break;

            // REQUEST CHAT AS A EMISOR
            case Request.CHAT_REQUESTED:
                ClientChannel receptor = null;
                // We find the picked user by the requester=emisor
                if (msg.getText().equals(Request.SELECT_USER_BY_NICKNAME)) {
                    receptor = Server.getInstance().getOnlineUserByNick(msg.getReceptor());
                } else if (msg.getText().equals(Request.SELECT_USER_BY_ID)) {
                    receptor = Server.getInstance().getOnlineUserByID(msg.getReceptor());
                }

                new Request().requestChatting(this, receptor);

                break;

            // REQUEST CHAT AS A RECEPTOR = EMISOR
            case Request.ACCEPT_CHAT:
                ClientChannel clientComfirm = Server.getInstance().getOnlineUserByNick(msg.getReceptor());
                System.out.println(nick + " accepted. Startting chat with " + clientComfirm.nick);
                new Request().startChat(this, clientComfirm);
                break;

            case Request.REJECT_CHAT:
                // new Request().rejectChat(msg);
                break;

            case Request.TO_CHAT:
                System.out.println(msg.toString());
                ClientChannel receiver = Server.getInstance().getOnlineUserByNick(msg.getReceptor());
                new Request().sendDirectMessage(nick, receiver, ASKING_PERMISSION);
                break;
        }
    }

    @Override
    public void run() {
        while (true) {
            Message msg = readClientMessage();
            if (msg != null) {
                try {
                    System.out.println(msg.toString());
                    handleRequest(msg);
                } catch (NullPointerException e) {
                    System.out.println(
                            CONNECTION_CLOSED + " [" + nick + "]");
                    Server.getInstance().deleteConnection(this);
                    break;
                }
            } else {
                System.out.println("Listening Loop closed for connection with " + this.getNick());
                System.out.println(
                        CONNECTION_CLOSED + " [" + nick + "]");
                Server.getInstance().deleteConnection(this);
                // El MSG es nulo lo que significa que no se ha podido recibir o se ha perdido
                // la conecxion
                break;
            }
        }

    }
}
