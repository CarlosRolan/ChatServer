package controller;

import connection.ClientChannel;
import connection.ConStatusCodes;

public class Request implements ConStatusCodes {

    public static final String PRESENTATION = "PRESENTATION";
    public static final String SHOW_ALL_ONLINE = "SHOW_ALL_ONLINE";
    public static final String CHAT_REQUESTED = "CHAT_REQUESTED";

    public static final String START_CHAT = "START_CHAT";

    public static final String ACCEPT_CHAT = "ACCEPT_CHAT";
    public  static final String REJECT_CHAT = "REJECT_CHAT";
    
    public final static String SELECT_USER_BY_ID = "Select an ID of the user you want to chat with";
    public final static String SELECT_USER_BY_NICKNAME = "Select an NICK of the user you want to chat with";

    public String showOnlineUsers() {
        String nickNames = "";
        for (int i = 0; i < Server.getInstance().getOnlineChannels().size(); i++) {
            ClientChannel current = Server.getInstance().getOnlineChannels().get(i);
            nickNames += current.getId() + current.getNick() + ",";
        }
        return nickNames;
    }

    public void requestChatting(ClientChannel emisor, ClientChannel receptor) {

        //If we found it (not null) and is not trying to establish a chat with itself we request chatting	
        if (receptor == null) {
            System.out.println(CLIENT_NOT_FOUND);
            emisor.writeClientMessage(new Message(CLIENT_NOT_FOUND));
            //Is trying to talk with hiimself

        } else if (emisor.getNick().equals(receptor.getNick()) | emisor.getId() == receptor.getId()) {
            System.out.println(SELF_REFERENCE);
            emisor.writeClientMessage(new Message(SELF_REFERENCE));
        }
            
        // To de emirsor
        Message waitingMsg = new Message(WAITING_FOR_PERMISSION, receptor.getNick(), emisor.getNick());
        System.out.println(waitingMsg);
        emisor.writeClientMessage(waitingMsg);
        // To the receptor
        Message permitChat = new Message(ASKING_PERMISSION, emisor.getNick(), receptor.getNick());
        System.out.println(permitChat);
        receptor.writeClientMessage(permitChat);
    }

    public void startChat(ClientChannel emisor, ClientChannel receptor) {

        emisor.writeClientMessage(new Message( "\t~~~~~CHAT"+ receptor.getNick() +"~~~~~~"));
                     
    }

    public void rejectChat(Message msg) {
    
    }

}
