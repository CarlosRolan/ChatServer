package controller.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import controller.Message;
import controller.Message.MsgType;
import controller.Server;
import controller.api.Request;
import controller.api.RequestCodes;
import controller.chat.ChatReference;
import log.ClientLog;

public class ClientChannel extends Thread implements RequestCodes {

    private Socket pSocket = null;
    private String nick;
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
        Message comfirmation = new Message(MsgType.REQUEST, PRESENTATION_SUCCES, "SERVER", nick);
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

        ChatReference chatReference = null;

        cLog.log(msg.toString());

        switch (msg.getAction()) {

            case Request.SHOW_ALL_MEMBERS:
                ChatReference selectedChat = Server.getInstance().getChatReferenceByID(Long.valueOf(msg.getReceptor()));
                new Request().showAllMembers(this, selectedChat);
                break;
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

            case Request.NEW_CHAT:
                long chatID = 100 + this.getId();
                String chatTitle = msg.getReceptor();
                String chatDesc = msg.getText();
                ChatReference ref = new ChatReference(chatID, chatTitle, chatDesc, this);
                Server.getInstance().registerChat(ref);
                new Request().registerChat(this, ref);
                break;

            case Request.SHOW_ALL_CHATS:
                new Request().showAllChatsForUser(this);
                break;

            case Request.START_CHAT:
                chatReference = Server.getInstance().getChatReferenceByID(Long.valueOf(msg.getEmisor()));
                new Request().openChat(this, chatReference);
                break;

            case Request.ADD_MEMBER:
                chatReference = Server.getInstance().getChatReferenceByID(Long.valueOf(msg.getEmisor()));
                ClientChannel newMember = Server.getInstance().getOnlineUserByID(msg.getReceptor());
                new Request().addMember(this, newMember, chatReference);
                break;

            case Request.TO_CHAT:
                ChatReference refs = Server.getInstance().getChatReferenceByID(Long.valueOf(msg.getEmisor()));
                new Request().sendToChat(this, refs, msg.getReceptor());
                break;
        }
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
