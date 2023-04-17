package controller;

import chats.Chat;
import connection.ClientChannel;
import connection.ClientStatusCodes;


public class Request implements ClientStatusCodes {

    public static final String PRESENTATION = "PRESENTATION";
    public static final String SHOW_ALL_ONLINE = "SHOW_ALL_ONLINE";

    public static final String CHAT_REQUESTED = "CHAT_REQUESTED";
    public static final String START_CHAT = "START_CHAT";
    public static final String ACCEPT_CHAT = "ACCEPT_CHAT";
    public static final String REJECT_CHAT = "REJECT_CHAT";
    public static final String TO_CHAT = "TO_CHAT";

    public final static String SELECT_USER_BY_ID = "Select an ID of the user you want to chat with";
    public final static String SELECT_USER_BY_NICKNAME = "Select an NICK of the user you want to chat with";

    public void showOnlineUsers(ClientChannel requester) {
        String nickNames = "";
        for (int i = 0; i < Server.getInstance().getOnlineChannels().size(); i++) {
            ClientChannel current = Server.getInstance().getOnlineChannels().get(i);
            nickNames += current.getId() + current.getNick() + ",";
        }
        System.out.println("__RESULT__[" + nickNames + "] ==> " + requester.getNick());
        requester.writeClientMessage(new Message(SHOW_ALL_ONLINE, "SERVER", requester.getNick(), nickNames));
    }

    public void requestChatting(ClientChannel requester, ClientChannel receptor) {

        // If we found it (not null) and is not trying to establish a chat with itself
        // we request chatting
        if (receptor == null) {
            System.out.println(CLIENT_NOT_FOUND);
            requester.writeClientMessage(new Message(CLIENT_NOT_FOUND));
            // Is trying to talk with hiimself

        } else if (requester.getNick().equals(receptor.getNick()) | requester.getId() == receptor.getId()) {
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

    public void startChat(ClientChannel requester, ClientChannel receptor) {

        Message chatStartedForRequester = new Message(START_CHAT, "SERVER", requester.getNick());
        Message chatStartedForASked = new Message(START_CHAT, "SERVER", receptor.getNick());

        System.out.println("========COMFIRMATION MSGS====");
        System.out.println(chatStartedForRequester);
        System.out.println(chatStartedForASked);

        requester.writeClientMessage(chatStartedForRequester);
        requester.setChatting(true);
        receptor.writeClientMessage(chatStartedForASked);
        receptor.setChatting(true);

    }

    public void rejectChat(ClientChannel requester, ClientChannel receptor) {
        Message msgForRequester = new Message(REJECT_CHAT, receptor.getNick(), requester.getNick(),
                receptor.getNick() + " no quiere chatear contigo");
        requester.writeClientMessage(msgForRequester);
    }

    public void sendMsgToChat(Chat chat, ClientChannel sender, String textMsg) {
        
    }

    public void sendDirectMessage(String emisorNick, ClientChannel receptor, String textMsg) {
        receptor.writeClientMessage(new Message(TO_CHAT, emisorNick,receptor.getNick(), textMsg));
    }

}
