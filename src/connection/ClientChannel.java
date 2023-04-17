package connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import chats.Chat;
import controller.Server;
import controller.Message;
import controller.Request;

public class ClientChannel extends Thread implements ClientStatusCodes {

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
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
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
                new Request().sendDirectMessage(nick, receiver, msg.getText());
                break;
        }
    }

    @Override
    public void run() {
        while (true) {
            Message msg = readClientMessage();
            System.out.println(msg.toString());
            try {
                handleRequest(msg);
            } catch (NullPointerException e) {
                System.out.println(
                        CONNECTION_CLOSED + " [" + nick + "]");
                Server.getInstance().deleteConnection(this);
                break;
            }
        }

    }
}
