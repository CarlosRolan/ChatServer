package api;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.api.Codes;
import com.chat.Chat;
import com.chat.Member;
import com.controller.Connection;
import com.data.MSG;
import com.data.PKG;

import controller.Server;

public class RequestHandler implements Codes {

    private Server server = Server.getInstance();

    public static void newRequest(String methodName) {
        try {
            Class c = RequestHandler.class;
            Method m = c.getMethod(methodName);
            m.invoke(c.newInstance());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public void simpleMethod() {
        System.out.println("Solo soy un gusano");
    }

    public MSG showOnlineUsers(String emisorId) {
        String[] allOnline = new String[server.getNumberOfOnlineUsers() - 1];
        int i = 0;

        for (Connection iter : server.getAllConnections()) {
            if (!iter.getConId().equals(emisorId)) {
                System.out.println(iter.getNick());
                allOnline[i] = "[" + iter.getConId() + "]-" + iter.getNick();
                System.out.println(allOnline[i]);
                i++;
            }
        }

        MSG respond = new MSG(MSG.Type.REQUEST);

        respond.setAction(REQ_SHOW_ALL_CON);

        if (allOnline.length > 0) {
            respond.setParameters(allOnline);
        } else if (allOnline.length == 0) {
            respond.setParameter(0, "You are the only user ONLINE");
        }

        return respond;
    }

    public MSG showAllMemberOfChat(Chat selected) {
        MSG respond = new MSG(MSG.Type.REQUEST);

        respond.setAction(REQ_SHOW_ALL_MEMBERS_OF_CHAT);
        respond.setParameters(selected.getMembersRef());

        return respond;

    }

    /**
     * 
     * @param requesterId   msg.getEmisor()
     * @param candidateId   msg.getReceptor()
     * @param requesterNick msg.getBody()
     * @return MSG
     */
    public MSG askForSingle(String requesterId, String candidateId, String requesterNick) {
        MSG respond = null;
        MSG toCandidate = null;

        System.out.println("ID CANDIDATE [" + candidateId + "]");
        Connection candidate = server.getConnectionById(candidateId);

        if (candidateId.equals(requesterId)) {
            respond = new MSG(MSG.Type.ERROR);
            respond.setAction(ERROR_SELF_REFERENCE);
        } else if (candidate != null) {

            // to candidate
            toCandidate = new MSG(MSG.Type.REQUEST);
            toCandidate.setAction(REQ_ASKED_FOR_PERMISSION);
            toCandidate.setEmisor(requesterId);
            toCandidate.setReceptor(String.valueOf(candidateId));
            toCandidate.setParameter(0, requesterNick);
            try {
                candidate.write(toCandidate);
            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // to requester
            respond = new MSG(MSG.Type.REQUEST);
            respond.setAction(REQ_WAITING_FOR_PERMISSION);
            respond.setEmisor(candidateId);
            respond.setReceptor(requesterId);
            respond.setParameter(0, candidate.getNick());
            respond.setBody(requesterNick + " waiting for " + candidate.getNick());
        } else {
            respond = new MSG(MSG.Type.ERROR);
            respond.setAction(ERROR_CLIENT_NOT_FOUND);
        }

        return respond;
    }

    /**
     * Also send a MSG to the requester as
     * MSG[REQUEST]
     * action = REQ_START_SINGLE
     * emisor = requesterId
     * receptor = requestedId
     * parameter[0] = requestedNick
     * @param requesterId   msg.getReceptor()
     * @param requestedId   msg.getEmisor()
     * @param requestedNick msg.getBody()
     * @return
     * MSG[REQUEST]
     * action = REQ_START_SINGLE
     * emisor = requestedId
     * receptor = requestedId
     * parameter[0] = requester.getNick
     */
    public MSG allowSingleChat(String requesterId, String requestedId, String requestedNick) {
        MSG respond = new MSG(MSG.Type.REQUEST);
        MSG toRequester = new MSG(MSG.Type.REQUEST);

        // the requester is waiting for the respond at the moment
        Connection requester = server.getConnectionById(requesterId);

        toRequester.setAction(REQ_START_SINGLE);
        toRequester.setEmisor(requesterId);
        toRequester.setReceptor(requestedId);
        toRequester.setParameter(0, requestedNick);

        try {
            requester.write(toRequester);
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        respond.setAction(REQ_START_SINGLE);
        respond.setEmisor(requestedId);
        respond.setReceptor(requesterId);
        respond.setParameter(0, requester.getNick());

        return respond;
    }

    /**
     * 
     * @param emisorId   msg.getEmisor
     * @param emisorNick msg.getParameter(0)
     * @param receptorId msg.getReceptor()
     * @param text       msg.getBody()
     */
    public void sendSingleMsg(String emisorId, String emisorNick, String receptorId, String text) {

        Connection receptor = server.getConnectionById(receptorId);

        MSG directMSG = new MSG(MSG.Type.MESSAGE);

        directMSG.setAction(MSG_TO_SINGLE);
        directMSG.setEmisor(emisorId);
        directMSG.setReceptor(receptorId);
        directMSG.setParameter(0, emisorNick);
        directMSG.setBody(text);

        try {
            receptor.write(directMSG);
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendMsgToChat(Chat selectedChat, String emisorId, String emisorNick, String text) {
        MSG toChat = new MSG(MSG.Type.MESSAGE);

        toChat.setAction(MSG_TO_CHAT);
        toChat.setEmisor(emisorId);
        toChat.setParameter(0, emisorNick);
        toChat.setBody(text);

        for (Member iMember : selectedChat.getMembers()) {
            Connection memberCon = Server.getInstance().getConnectionById(iMember.getConnectionId());
            try {
                memberCon.write(toChat);
            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void exitSigle(String emisorId, String receptorId) {
        Connection receptor = server.getConnectionById(receptorId);

        MSG exitSingle = new MSG(MSG.Type.REQUEST);

        exitSingle.setAction(REQ_EXIT_SINGLE);
        exitSingle.setEmisor(emisorId);
        exitSingle.setReceptor(receptorId);

        try {
            receptor.write(exitSingle);
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 
     * @return
     *         MSG[REQUEST]
     *         action = REQ_SHOW_ALL_CHAT
     *         parameters = all active chats in the server
     */
    public MSG showAllChats() {
        MSG respond = new MSG(MSG.Type.REQUEST);
        String[] chats = new String[server.getNumberOfChats()];
        respond.setAction(REQ_SHOW_ALL_CHAT);
        int i = 0;

        if (chats.length >= 1) {
            for (Chat iter : server.getAllChats()) {
                chats[i] = "[" + iter.getChatId() + "]" + iter.getTitle() + "-" + iter.getDescription();
                respond.setParameter(i, chats[i]);
            }
        } else {
            respond.setParameter(0, "You do not have any chat yet");
        }
        return respond;
    }

    // TODO make difeerent when created new chat and wehn added
    // To update or send the new chatCreated
    public MSG sendChatInstance(Chat chat) {
        MSG respond = null;
        if (chat != null) {
            respond = new MSG(MSG.Type.REQUEST);
            respond.setAction(REQ_INIT_CHAT);
            respond.setEmisor(chat.getChatId());
            respond.setReceptor(chat.getTitle());
            respond.setBody(chat.getDescription());
            respond.setParameters(chat.getMembersRef());
        } else {
            respond = new MSG(MSG.Type.ERROR);
            respond.setAction(ERROR_CHAT_NOT_FOUND);
        }

        return respond;
    }

    /**
     * 
     * @param con the connection to send the info
     * @return
     *         MSG[REQUEST],
     *         action = REQ_INIT_CON,
     *         emisor = connection id,
     *         receptor = connection nick,
     *         body = date-time
     *         OR
     *         MSG[ERROR],
     *         action = ERROR_CLIENT_NOT_FOUND
     */
    public MSG sendConInstance(Connection con) {
        MSG respond = null;

        if (con != null) {
            respond = new MSG(MSG.Type.REQUEST);
            respond.setAction(REQ_INIT_CON);
            respond.setEmisor(con.getConId());
            respond.setReceptor(con.getNick());
            // respond.setParameters(con.getChatsRef());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            respond.setBody(dtf.format(now));

        } else {
            respond = new MSG(MSG.Type.ERROR);
            respond.setAction(ERROR_CLIENT_NOT_FOUND);
        }
        return respond;
    }

    /**
     * 
     * @param emisorId
     * @return
     */
    public PKG sendStateUpdate(String emisorId) {
        PKG updatedState = new PKG(PKG.Type.COLLECTION);
        updatedState.setName(COLLECTION_UPDATE);

        for (Connection iCon : server.getAllConnections()) {
            if (!iCon.getConId().equals(emisorId)) {
                MSG msgCon = sendConInstance(iCon);
                updatedState.addMsg(msgCon);
            }
        }

        for (Chat iChat : server.getAllChats()) {
            for (Member iMeber : iChat.getMembers()) {
                if (iMeber.getConnectionId().equals(emisorId)) {
                    MSG msgChat = sendChatInstance(iChat);
                    updatedState.addMsg(msgChat);
                }
            }
        }

        return updatedState;

    }


}
