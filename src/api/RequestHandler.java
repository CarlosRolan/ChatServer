package api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.chat.Chat;
import com.chat.Member;
import com.comunication.ApiCodes;
import com.comunication.Connection;
import com.comunication.MSG;
import com.comunication.PKG;

import controller.Server;

public class RequestHandler implements ApiCodes {

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
        respond.setParameters(selected.getmembersToString());

        return respond;

    }

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
            candidate.write(toCandidate);

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

    public MSG allowSingleChat(String requesterId, String requestedId, String requestedNick) {
        MSG respond = new MSG(MSG.Type.REQUEST);
        MSG toRequester = new MSG(MSG.Type.REQUEST);

        // the requester is waiting for the respond at the moment
        Connection requester = server.getConnectionById(requesterId);

        toRequester.setAction(REQ_START_SINGLE);
        toRequester.setEmisor(requesterId);
        toRequester.setReceptor(requestedId);
        toRequester.setParameter(0, requestedNick);

        requester.write(toRequester);

        respond.setAction(REQ_START_SINGLE);
        respond.setEmisor(requestedId);
        respond.setReceptor(requesterId);
        respond.setParameter(0, requester.getNick());

        return respond;
    }

    public void sendSingleMsg(String emisorId, String receptorId, String text) {

        Connection receptor = server.getConnectionById(receptorId);

        MSG directMSG = new MSG(MSG.Type.MESSAGE);

        directMSG.setAction(MSG_TO_SINGLE);
        directMSG.setEmisor(emisorId);
        directMSG.setReceptor(receptorId);
        directMSG.setBody(text);

        receptor.write(directMSG);
    }

    public void sendMsgToChat(Chat selectedChat, String emisorId, String emisorNick, String text) {
        MSG toChat = new MSG(MSG.Type.MESSAGE);

        toChat.setAction(MSG_TO_CHAT);
        toChat.setEmisor(emisorId);
        toChat.setParameter(0, emisorNick);
        toChat.setBody(text);

        for (Member iMember : selectedChat.getMembers()) {
            Connection memberCon = Server.getInstance().getConnectionById(iMember.getConnectionId());
            memberCon.write(toChat);
        }
    }

    public void exitSigle(String emisorId, String receptorId) {
        Connection receptor = server.getConnectionById(receptorId);

        MSG exitSingle = new MSG(MSG.Type.REQUEST);

        exitSingle.setAction(REQ_EXIT_SINGLE);
        exitSingle.setEmisor(emisorId);
        exitSingle.setReceptor(receptorId);

        receptor.write(exitSingle);
    }

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

    // To update or send the new chatCreated
    public MSG sendChatInstance(Chat chat) {
        MSG respond = null;
        if (chat != null) {
            respond = new MSG(MSG.Type.REQUEST);
            respond.setAction(REQ_INIT_CHAT);
            respond.setEmisor(chat.getChatId());
            respond.setReceptor(chat.getTitle());
            respond.setBody(chat.getDescription());
            respond.setParameters(chat.getmembersToString());
        } else {
            respond = new MSG(MSG.Type.ERROR);
            respond.setAction(ERROR_CHAT_NOT_FOUND);
        }

        return respond;
    }

    public PKG sendUpdatedChats(String emisorId) {
        PKG stateUpdated = new PKG(PKG.Type.COLLECTION);

        for (Chat iChat : server.getAllChats()) {
            for (Member iMeber : iChat.getMembers()) {
                if (iMeber.getConnectionId().equals(emisorId)) {
                    MSG chatInstance = sendChatInstance(iChat);
                    stateUpdated.addMsg(chatInstance);
                }
            }
        }

        return stateUpdated;
    }

    public PKG sendUpdatedClients(String emisorId) {

        PKG updatedState = new PKG(PKG.Type.COLLECTION);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        for (Connection iCon : server.getAllConnections()) {
            MSG msgCon = new MSG(MSG.Type.REQUEST);
            msgCon.setAction("CON_INFO");
            msgCon.setEmisor(dtf.format(now));
            msgCon.setBody(iCon.getConId() + "_" + iCon.getNick());
            updatedState.addMsg(msgCon);
        }
        return updatedState;

    }

}
