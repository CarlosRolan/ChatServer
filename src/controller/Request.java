package controller;

import java.io.IOException;

import connection.ClientChannel;

public final class Request {

    public static final String SHOW_ONLINE = "SHOW_ONLINE_USER";
    public static final String NEW_CHAT = "NEW_CHAT";
    public static final String PUBLISH_TO_SERVER = "PUBLISH_TO_SERVER";

    public static String showOnlineUsers(ClientChannel receptor) {
        String nickNames = "";
        for (int i = 0; i < Server.getInstance().getChannels().size(); i++) {
            ClientChannel current = Server.getInstance().getChannels().get(i);
            nickNames += current.getId() + current.getNick();
        }
        System.out.println("COMMAND " + SHOW_ONLINE + " OUTPUT:[" + nickNames +"]");
        return nickNames;
    }

    public static void chatWith(ClientChannel receptor) {
        
    }

}
