package controller.connection;

import java.io.IOException;
import java.net.Socket;

import com.comunication.Connection;
import com.comunication.Msg;
import com.comunication.Msg.MsgType;

import controller.Server;
import log.ClientLog;

public class ClientChannel extends Connection {

    private ClientLog cLog;

    public ClientChannel(String nick) {
        super(nick);
        cLog = new ClientLog(this);
    }

    public ClientChannel(Socket socket) {
        super(socket);
        cLog = new ClientLog(this);
    }

    @Override
    public void writeMessage(Msg msg) {
        System.out.println(getConId() + getNick());
        System.out.println("OUT==>" + msg.toString());
        super.writeMessage(msg);
    }

    @Override
    public Msg readMessage() {
        try {
            Msg msg = (Msg) getOis().readObject();
            System.out.println("<==IN " + msg.toString());
            return msg;
        } catch (NullPointerException e) {
            System.out.println("NullPointerException");
            return null;
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException");
            return null;
        } catch (IOException e) {
            System.out.println("IOException");
            System.err.println("[" + getNick() + "] HAS LEFT");
            return null;
        }
    }

    @Override
    public void run() {
        if (recievePresentation()) {
            Server.getInstance().registerConnection(this);
            sendComfirmation();
            try {
                while (true) {
                    Msg msg = readMessage();
                    Server.getInstance().handleRequest(msg, this);
                }
            } catch (Exception e) {
                System.out.println(e.getClass());
                System.out.println(
                        INFO_CONNECTION_CLOSED + " [" + getNick() + "]");
                Server.getInstance().deleteConnection(this);
                cLog.logOut();
            }

        }

    }

    private boolean recievePresentation() {
        Msg presentation;
        presentation = readMessage();

        setConId(Thread.currentThread().getId());
        setNick(presentation.getEmisor());
        if (presentation.getAction().equals(REQ_PRESENT)) {
            System.out.println("[" + presentation.getEmisor() + "] IS ACCEPTED");
            return true;
        } else {
            return false;
        }
    }

    private void sendComfirmation() {
        Msg comfirmation = new Msg(MsgType.REQUEST);
        comfirmation.setAction(INFO_PRESENTATION_SUCCES);
        comfirmation.setReceptor(getConId());
        System.out.println("SENDING COMFIRMATION TO [" + comfirmation.getReceptor() + "]");
        writeMessage(comfirmation);

    }

}
