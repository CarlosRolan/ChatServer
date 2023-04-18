package controller.api;

import connection.ClientChannel;
import connection.ClientStatusCodes;
import controller.Message;
import controller.Server;

public class Request implements ClientStatusCodes, RequestCodes {

    private Server server = Server.getInstance();

    //SERVER ACTIONS
    public void showOnlineUsers(ClientChannel requester) {
        String nickNames = "";
        for (int i = 0; i < server.getOnlineChannels().size(); i++) {
            ClientChannel current = server.getOnlineChannels().get(i);
            nickNames += current.getId() + current.getNick() + ",";
        }
        System.out.println("__RESULT__[" + nickNames + "] ==> " + requester.getNick());
        requester.writeClientMessage(new Message(SHOW_ALL_ONLINE, "SERVER", requester.getNick(), nickNames));
    }

    //SINGLE CONVERSATION BEETWEEN 2 USERS ONLY
    public void requestSingle(ClientChannel requester, ClientChannel receptor) {

        if (receptor == null) {
            //CLient not foud
            System.out.println(CLIENT_NOT_FOUND);           
            requester.writeClientMessage(new Message(CLIENT_NOT_FOUND));
        } else if (requester.getNick().equals(receptor.getNick()) | requester.getId() == receptor.getId()) {
             // Is trying to talk with hiimself
            System.out.println(SELF_REFERENCE);
            requester.writeClientMessage(new Message(SELF_REFERENCE));
        } else {
            // To de emirsor
            Message waitingMsg = new Message(WAITING_FOR_PERMISSION, receptor.getNick(), requester.getNick());
            System.out.println(waitingMsg);
            requester.writeClientMessage(waitingMsg);
            // To the receptor
            Message permitChat = new Message(ASKED_FOR_PERMISSION, requester.getNick(), receptor.getNick());
            System.out.println(permitChat);
            receptor.writeClientMessage(permitChat);
        }
    }

     public void startSingle(ClientChannel requester, ClientChannel receptor) {
        Message forRequested = new Message(START_SINGLE, "SERVER", requester.getNick());
        Message forAsked = new Message(START_SINGLE, "SERVER", receptor.getNick());

        System.out.println("========COMFIRMATION MSGS====");
        System.out.println(forRequested);
        System.out.println(forAsked);
        System.out.println("=============================");

        requester.setChatting(true);
        requester.writeClientMessage(forRequested);
        receptor.setChatting(true);
        receptor.writeClientMessage(forAsked);
        

    }

     public void denySingle(ClientChannel emisor, ClientChannel requester) {
        Message denyForRequester = new Message(DENY, emisor.getNick(), requester.getNick(),
        emisor.getNick() + " no quiere chatear contigo");
                requester.setChatting(false);
        requester.writeClientMessage(denyForRequester);
    }

     public void sendDirectMessage(String emisorNick, ClientChannel receptor, String textMsg) {
        receptor.writeClientMessage(new Message(SEND_DIRECT_MSG, emisorNick,receptor.getNick(), textMsg));
    }

    //CHAT BEETWEEN 2 OR MORE USERS

}
