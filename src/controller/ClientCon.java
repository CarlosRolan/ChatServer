package controller;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.chat.Chat;
import com.chat.Member;
import com.controller.Connection;
import com.controller.handlers.IMSGHandler;
import com.controller.handlers.IPKGHandler;
import com.data.MSG;
import com.data.MSG.Type;

import controller.log.ClientLog;

public class ClientCon extends Connection {

    private ClientLog cLog;

    private boolean recievePresentation() {
        MSG presentation = null;
        try {
            presentation = (MSG) read();
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException" + " reading presentation");
        } catch (IOException e) {
            System.err.println("IOException" + " reading presentation");
        }

        setConId(Thread.currentThread().getId());
        setNick(presentation.getEmisor());

        cLog = new ClientLog(getConId(), getNick());

        if (presentation.getAction().equals(REQ_PRESENT)) {
            System.out.println("[" + presentation.getEmisor() + "] IS ACCEPTED");
            return true;
        } else {
            return false;
        }
    }

    private void sendComfirmation() {

        MSG comfirmation = new MSG(Type.REQUEST);
        comfirmation.setAction(INFO_PRESENTATION_SUCCES);
        comfirmation.setReceptor(getConId());

        System.out.println("SENDING COMFIRMATION TO [" + comfirmation.getReceptor() + "]");

        try {
            write(comfirmation);
        } catch (SocketException e) {
            System.err.println("SocketException" + comfirmation.toString());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException" + comfirmation.toString());
            e.printStackTrace();
        }

    }

    /**
     * 
     * @return
     */
    public List<Chat> getChats() {
        List<Chat> chatsParticipated = new ArrayList<>();
        for (Chat chat : Server.getInstance().getAllChats()) {
            for (Member member : chat.getMembers()) {
                if (member.getConnectionId().equals(getConId())) {
                    chatsParticipated.add(chat);
                }
            }
        }
        return chatsParticipated;
    }

    /* CONSTRUCTORs */
    /**
     * 
     * @param socket
     * @param msgHandler
     * @param pckgHandler
     */
    public ClientCon(Socket socket, IMSGHandler msgHandler, IPKGHandler pckgHandler) {
        super(socket, msgHandler, pckgHandler);
    }

    @Override
    public void run() {
        if (recievePresentation()) {
            Server.getInstance().registerConnection(this);
            sendComfirmation();
            try {
                while (true) {
                    /* FROM CLIENT */
                    listen();

                    write(Server.getInstance().respond);

                }
            } catch (Exception e) {
                /*
                 * e.printStackTrace();
                 * System.out.println(e.getClass());
                 * System.out.println("IOException");
                 */
                System.err.println("[" + getNick() + "] HAS LEFT");
                System.out.println(
                        INFO_CONNECTION_CLOSED + " [" + getNick() + "]");
                Server.getInstance().deleteConnection(this);
                cLog.logOut();
            }
        }
    }

}
