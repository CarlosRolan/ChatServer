package api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.chat.Chat;
import com.comunication.ApiCodes;
import com.comunication.Connection;
import com.comunication.Msg;
import com.comunication.Msg.MsgType;

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

    public Msg showOnlineUsers(String emisorId) {
        String[] allOnline = new String[server.getNumberOfOnlineUsers() - 1];
        int i = 0;

        for (Connection iter : server.getOnlineCon()) {
            if (!iter.getConId().equals(emisorId)) {
                System.out.println(iter.getNick());
                allOnline[i] = "[" + iter.getConId() + "]-" + iter.getNick();
                System.out.println(allOnline[i]);
                i++;
            }
        }

        Msg respond = new Msg(MsgType.REQUEST);

        respond.setAction(REQ_SHOW_ALL_CON);

        if (allOnline.length > 0) {
            respond.setParameters(allOnline);
        } else if (allOnline.length == 0) {
            String[] parameters = { "You are the only user ONLINE" };
            respond.setParameters(parameters);
        }

        return respond;
    }

    public Msg askForSingle(String requesterId, String candidateId, String requesterNick) {
        Msg respond = null;
        Msg toCandidate = null;
        String[] parameters = new String[1];
        System.out.println("ID CANDIDATE [" + candidateId + "]");
        Connection candidate = server.getClientConnectionById(Integer.valueOf(candidateId));

        if (candidateId.equals(requesterId)) {
            respond = new Msg(MsgType.ERROR);
            respond.setAction(ERROR_SELF_REFERENCE);
        } else if (candidate != null) {

            // to candidate
            parameters[0] = requesterNick;
            toCandidate = new Msg(MsgType.REQUEST);
            toCandidate.setAction(REQ_ASKED_FOR_PERMISSION);
            toCandidate.setEmisor(requesterId);
            toCandidate.setReceptor(String.valueOf(candidateId));
            toCandidate.setParameters(parameters);
            candidate.writeMessage(toCandidate);

            // to requester
            parameters[0] = requesterNick;
            respond = new Msg(MsgType.REQUEST);
            respond.setAction(REQ_WAITING_FOR_PERMISSION);
            respond.setEmisor(candidateId);
            respond.setReceptor(requesterId);
            respond.setParameters(parameters);
            respond.setBody(requesterNick + " waiting for " + candidate.getNick());
        } else {
            respond = new Msg(MsgType.ERROR);
            respond.setAction(ERROR_CLIENT_NOT_FOUND);
        }

        return respond;
    }

    public Msg allowSingleChat(String requesterId, String requestedId, String requestedNick) {
        Msg respond = new Msg(MsgType.REQUEST);
        Msg toRequester = new Msg(MsgType.REQUEST);
        String[] parameters = new String[1];

        // the requester is waiting for the respond at the moment
        Connection requester = server.getClientConnectionById(Integer.parseInt(requesterId));

        toRequester.setAction(REQ_START_SINGLE);
        toRequester.setEmisor(requesterId);
        toRequester.setReceptor(requestedId);
        parameters[0] = requestedNick;
        toRequester.setParameters(parameters);

        requester.writeMessage(toRequester);

        respond.setAction(REQ_START_SINGLE);
        respond.setEmisor(requestedId);
        respond.setReceptor(requesterId);
        parameters[0] = requester.getNick();
        toRequester.setParameters(parameters);

        return respond;
    }

    public void sendSingleMsg(String emisorId, String receptorId, String text) {

        Connection receptor = server.getClientConnectionById(Integer.parseInt(receptorId));

        Msg directMsg = new Msg(MsgType.MESSAGE);

        directMsg.setAction(MSG_SINGLE_MSG);
        directMsg.setEmisor(emisorId);
        directMsg.setReceptor(receptorId);
        directMsg.setBody(text);
        receptor.writeMessage(directMsg);
    }

    public void exitSigle(String emisorId, String receptorId) {
        Connection receptor = server.getClientConnectionById(Integer.parseInt(receptorId));

        Msg exitSingle = new Msg(MsgType.REQUEST);

        exitSingle.setAction(REQ_EXIT_SINGLE);
        exitSingle.setEmisor(emisorId);
        exitSingle.setReceptor(receptorId);

        receptor.writeMessage(exitSingle);
    }

    public Msg showAllChats() {
        Msg respond = new Msg(MsgType.REQUEST);
        String[] chats = new String[server.getAllChats().size()];
        respond.setAction(REQ_SHOW_ALL_CHAT);
        int i = 0;

        for (Chat iter : server.getAllChats()) {
            chats[i] = "[" + iter.getChatId() + "]" + iter.getTitle() + "-" + iter.getDescription();
            respond.setParameter(i, chats[i]);
        }

        return respond;

    }

    public Msg createNewChat(Msg msg) {

        Chat chat = Chat.createChatAsAdmin(msg);

        server.registerChat(chat);

        Msg respond = new Msg(MsgType.REQUEST);
        respond.setAction(REQ_INIT_CHAT);
        respond.setEmisor(msg.getEmisor());
        respond.setReceptor(msg.getParameter(0));
        respond.setBody(msg.getParameter(1));
        respond.setParameters(chat.getmembersToString());

        return respond;
    }

}
