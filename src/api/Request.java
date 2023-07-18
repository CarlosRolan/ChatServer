package api;

import com.Msg;
import com.Msg.MsgType;
import com.RequestCodes;

import controller.Server;
import controller.connection.ClientConnection;

public class Request implements RequestCodes {

    public Server server = Server.getInstance();

    public Msg showOnlineUsers(ClientConnection cc) {
        String[] allOnline = new String[server.getNumberOfOnlineUsers() - 1];
        int i = 0;

        for (ClientConnection iter : server.getOnlineCon()) {
            if (!iter.getConId().equals(cc.getConId()) && !iter.getNick().equals(cc.getNick())) {
                System.out.println(iter.getNick());
                allOnline[i] = "[" + iter.getConId() + "]-" + iter.getNick();
                System.out.println(allOnline[i]);
                i++;
            }
        }

        Msg respond = new Msg(MsgType.REQUEST);

        respond.setAction(REQ_SHOW_ALL_ONLINE);

        if (allOnline.length > 0) {
            respond.setParameters(allOnline);
        } else if (allOnline.length == 0) {
            respond.setParameter(0, "You are the only user ONLINE");
        }

        return respond;
    }

    public Msg askForSingle(String requesterId, String candidateId, String requesterNick) {
        Msg respond = null;
        Msg toCandidate = null;
        System.out.println("ID CANDIDATE [" + candidateId + "]");
        ClientConnection candidate = server.getClientConnectionById(Integer.valueOf(candidateId));

        if (candidateId.equals(requesterId)) {
            respond = new Msg(MsgType.ERROR);
            respond.setAction(ERROR_SELF_REFERENCE);
        } else if (candidate != null) {
            // to candidate
            toCandidate = new Msg(MsgType.REQUEST);
            toCandidate.setAction(REQ_ASKED_FOR_PERMISSION);
            toCandidate.setEmisor(requesterId);
            toCandidate.setReceptor(String.valueOf(candidateId));
            toCandidate.setParameter(0, requesterNick);
            candidate.writeClientMessage(toCandidate);
            // to requester
            respond = new Msg(MsgType.REQUEST);
            respond.setAction(REQ_WAITING_FOR_PERMISSION);
            respond.setEmisor(candidateId);
            respond.setReceptor(requesterId);
            respond.setParameter(0, candidate.getNick());
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

        // the requester is waiting for the respond at the moment
        ClientConnection requester = server.getClientConnectionById(Integer.parseInt(requesterId));

        toRequester.setAction(REQ_START_SINGLE);
        toRequester.setEmisor(requesterId);
        toRequester.setReceptor(requestedId);
        toRequester.setParameter(0, requestedNick);

        requester.writeClientMessage(toRequester);

        respond.setAction(REQ_START_SINGLE);
        respond.setEmisor(requestedId);
        respond.setReceptor(requesterId);
        respond.setParameter(0, requester.getNick());

        return respond;
    }

    public void sendDirectMsg(String emisorId, String receptorId, String text) {

        ClientConnection receptor = server.getClientConnectionById(Integer.parseInt(receptorId));

        Msg directMsg = new Msg(MsgType.MESSAGE);

        directMsg.setAction(DIRECT_MSG);
        directMsg.setEmisor(emisorId);
        directMsg.setReceptor(receptorId);
        directMsg.setBody(text);
        receptor.writeClientMessage(directMsg);
    }

}
