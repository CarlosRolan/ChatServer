package controller.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import api.Request;
import api.RequestCodes;
import controller.Msg;
import controller.Msg.MsgType;
import controller.Server;
import log.ClientLog;

public class ClientConnection extends Thread implements RequestCodes {

    Server server = Server.getInstance();

    private Socket pSocket = null;
    private String pNick;
    private long mId;
    private ClientLog cLog;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    /**
     * @return the mId
     */
    public String getConId() {
        return String.valueOf(mId);
    }

    /**
     * @param ois the ois to set
     */
    public void setOis(ObjectInputStream ois) {
        this.ois = ois;
    }

    /**
     * @param oos the oos to set
     */
    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }

    /**
     * @return the ois
     */
    public ObjectInputStream getOis() {
        return ois;
    }

    /**
     * @return the oos
     */
    public ObjectOutputStream getOos() {
        return oos;
    }

    /**
     * @return the pNick
     */
    public String getNick() {
        return pNick;
    }

    // Constructor
    public ClientConnection(Socket socket) {

        pSocket = socket;

        try {
            oos = new ObjectOutputStream(pSocket.getOutputStream());
            ois = new ObjectInputStream(pSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean recievePresentation() {
        Msg presentation = readClientMessage();
        mId = Thread.currentThread().threadId();
        pNick = presentation.getEmisor();
        if (presentation.getAction().equals(PRESENT)) {
            System.out.println("[" + presentation.getEmisor() + "] IS ACCEPTED");
            return true;
        } else {
            return false;
        }
    }

    private void sendComfirmation() {
        cLog = new ClientLog(this);
        Msg comfirmation = new Msg(MsgType.REQUEST);
        comfirmation.setAction(PRESENTATION_SUCCES);
        comfirmation.setReceptor(getConId());
        System.out.println("SENDING COMFIRMATION TO [" + comfirmation.getReceptor() + "]");
        writeClientMessage(comfirmation);

        cLog.logIn();
    }

    public void writeClientMessage(Msg msg) {
        System.out.println(getConId() + getNick());
        System.out.println("OUT==>" + msg.toString());
        try {
            oos.writeObject(msg);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Msg readClientMessage() {
        try {
            Msg msg = (Msg) ois.readObject();
            System.out.println("<==IN" + msg.toString());
            return msg;
        } catch (NullPointerException e) {
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.err.println("[" + getNick() + "] HAS LEFT");
            return null;
        }
    }

    public void handleRequest(Msg msg) {

        Msg respond = null;

        switch (msg.getAction()) {

            case SHOW_ALL_MEMBERS:

                break;
            case SHOW_ALL_ONLINE:
                respond = new Request().showOnlineUsers(this);
                writeClientMessage(respond);
                break;

            // REQUEST SINGLE CHAT AS A EMISOR
            case SINGLE_REQUESTED:
                respond = new Request().askForSingle(getConId(), msg.getReceptor(), msg.getBody());
                writeClientMessage(respond);
                break;

            // REQUESTED CHAT AS A RECEPTOR = EMISOR
            case ALLOW:
                respond = new Request().allowSingleChat(msg.getReceptor(), getConId(), getNick());
                writeClientMessage(respond);
                break;

            case DENY:

                break;

            case DIRECT_MSG:
                new Request().sendDirectMsg(msg.getEmisor(), msg.getReceptor(), msg.getBody());
                break;

            case NEW_CHAT:

                break;

            case SHOW_ALL_CHATS:

                break;

            case START_CHAT:

                break;

            case ADD_MEMBER:

                break;

            case TO_CHAT:

                break;
        }
    }

    @Override
    public void run() {
        if (recievePresentation()) {
            server.registerConnection(this);
            sendComfirmation();
            while (true) {
                Msg msg = readClientMessage();
                try {
                    handleRequest(msg);
                } catch (NullPointerException e) {
                    System.out.println(
                            CONNECTION_CLOSED + " [" + pNick + "]");
                    server.deleteConnection(this);
                    break;
                }
            }
        }

    }

}
