package controller;

import java.io.IOException;

import connection.ClientChannel;

public final class Request {

    public static final String SHOW_ONLINE = "SHOW_ONLINE_USER";
    public static final String NEW_CHAT = "NEW_CHAT";
    public static final String CHAT_REQUESTED = "CHAT_REQUESTED";
    public static final String PUBLISH_TO_SERVER = "PUBLISH_TO_SERVER";
    public static final String ASK_PERMISSION = " wants to talk with you. Do you ALLOW(Y/N)";
    public static final String SELECT_USER = "Select a user you wanna talk WITH:";

    public static String showOnlineUsers() {
        String nickNames = "";
        for (int i = 0; i < Server.getInstance().getOnlineChannels().size(); i++) {
            ClientChannel current = Server.getInstance().getOnlineChannels().get(i);
            nickNames += current.getId() + current.getNick();
        }
        System.out.println("COMMAND " + SHOW_ONLINE + " OUTPUT:[" + nickNames +"]");
        return nickNames;
    }

    public static void chatWith(ClientChannel receptor) throws IOException {
        receptor.writeClient(ASK_PERMISSION);
        
    }

    public static void allowChatting(String emisorNick, ClientChannel receptor) throws IOException {
        receptor.writeClient(ASK_PERMISSION);
        receptor.writeClient(emisorNick);
    }

}
