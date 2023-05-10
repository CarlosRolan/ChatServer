package controller.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import controller.Server;
import controller.api.Request;
import log.ClientLog;
import controller.ChatReference;
import controller.Message;

public class ClientChannel extends Thread implements ClientStatusCodes {

    private Socket pSocket = null;
    private String nick;
    private ArrayList<ChatReference> chatReferences = new ArrayList<>();
    private ClientLog cLog;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private boolean chatting = false;

    public boolean isChatting() {
        return chatting;
    }

    public void setChatting(boolean isChatting) {
        chatting = isChatting;
    }

    public ArrayList<ChatReference> getChatRefs() {
        return chatReferences;
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
        System.out.println("[" + presentation.getEmisor() + "] IS ACCEPTED/online");
        if (presentation.getAction().equals(Request.PRESENT)) {
            nick = presentation.getEmisor();
            return true;
        } else {
            return false;
        }
    }

    private void sendComfirmation() {
        cLog = new ClientLog(this);
        Message comfirmation = new Message(PRESENTATION_SUCCES, "SERVER", nick);
        writeClientMessage(comfirmation);
        System.out.println("SENDING COMFIRMATION TO [" + comfirmation.getReceptor() + "]");
        cLog.logIn();
    }

    public void writeClientMessage(Message msg) {
        cLog.log("OUT==>" + msg.toString());
        try {
            oos.writeObject(msg);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message readClientMessage() {
        try {
            Message msg = (Message) ois.readObject();
            return msg;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.err.println("[" + getNick() + "] HAS LEFT");
            return null;
        }
    }

    public void handleRequest(Message msg) {

        cLog.log(msg.toString());

        switch (msg.getAction()) {
            case Request.SHOW_ALL_ONLINE:
                new Request().showOnlineUsers(this);
                break;

            // REQUEST SINGLE CHAT AS A EMISOR
            // TODO refractor
            case Request.SINGLE_REQUESTED:
                ClientChannel receptor = null;
                // We find the picked user by the requester=emisor
                if (msg.getText().equals(Request.BY_NICK)) {
                    receptor = Server.getInstance().getOnlineUserByNick(msg.getReceptor());
                } else if (msg.getText().equals(Request.BY_ID)) {
                    receptor = Server.getInstance().getOnlineUserByID(msg.getReceptor());
                }

                new Request().requestSingle(this, receptor);

                break;

            // REQUESTED CHAT AS A RECEPTOR = EMISOR
            case Request.ALLOW:
                ClientChannel clientComfirm = Server.getInstance().getOnlineUserByNick(msg.getReceptor());
                System.out.println(nick + " accepted. Startting chat with " + clientComfirm.nick);
                new Request().startSingle(this, clientComfirm);
                break;

            case Request.DENY:
                ClientChannel requester = Server.getInstance().getOnlineUserByNick(msg.getReceptor());
                new Request().denySingle(this, requester);
                break;

            case Request.SEND_DIRECT_MSG:
                System.out.println(msg.toString());
                ClientChannel receiver = Server.getInstance().getOnlineUserByNick(msg.getReceptor());
                new Request().sendDirectMessage(nick, receiver, msg.getText());
                break;

            case Request.CHAT_REQUESTED:
                System.out.println(msg.getAction());
                ChatReference charRef = new ChatReference(getId() + 100, msg.getReceptor(), msg.getText(), this);
                new Request().registerChat(charRef, this);
                break;

            case Request.START_CHAT:
                String ChatName = msg.getReceptor();

        }
    }

    public void registerChat(ChatReference chatRef) {
        this.chatReferences.add(chatRef);
    }

    public void deleteChat(ChatReference chatRef) {
        this.chatReferences.remove(chatRef);
    }

    // TODO When an user ledts one single chat, disconnets the other
    @Override
    public void run() {
        while (true) {
            Message msg = readClientMessage();
            try {
                System.out.println(msg.toString());
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
