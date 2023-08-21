package controller.connection;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.chat.Chat;
import com.chat.Member;
import com.comunication.Connection;
import com.comunication.MSG;
import com.comunication.MSG.Type;
import com.comunication.handlers.IMSGHandler;
import com.comunication.handlers.IPKGHandler;

import controller.Server;
import log.ClientLog;

public class ClientChannel extends Connection {

    private ClientLog cLog;

    public ClientChannel(String nick, IMSGHandler msgHandler, IPKGHandler pckgHandler) {
        super(nick, msgHandler, pckgHandler);
        cLog = new ClientLog(this);
    }

    public ClientChannel(Socket socket, IMSGHandler msgHandler, IPKGHandler pckgHandler) {
        super(socket, msgHandler, pckgHandler);
        cLog = new ClientLog(this);
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
                e.printStackTrace();
                System.out.println(e.getClass());
                System.out.println("IOException");
                System.err.println("[" + getNick() + "] HAS LEFT");
                System.out.println(
                        INFO_CONNECTION_CLOSED + " [" + getNick() + "]");
                Server.getInstance().deleteConnection(this);
                cLog.logOut();
            }
        }
    }

    private boolean recievePresentation() {
        MSG presentation;
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
        MSG comfirmation = new MSG(Type.REQUEST);
        comfirmation.setAction(INFO_PRESENTATION_SUCCES);
        comfirmation.setReceptor(getConId());
        System.out.println("SENDING COMFIRMATION TO [" + comfirmation.getReceptor() + "]");
        write(comfirmation);
    }

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

}
