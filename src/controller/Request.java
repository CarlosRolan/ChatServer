package controller;


import connection.ClientChannel;

public class Request {

    public static final String REQUEST_RESPONSE = "REQUEST_RESPOND";
    public static final String PRESENTATION = "PRESENTATION";
    public static final String SHOW_ALL_ONLINE = "SHOW_ALL_ONLINE";

    public String showOnlineUsers() {
        String nickNames = "";
        for (int i = 0; i < Server.getInstance().getOnlineChannels().size(); i++) {
            ClientChannel current = Server.getInstance().getOnlineChannels().get(i);
            nickNames += current.getId() + current.getNick() + ",";
        }
        System.out.println("COMMAND " + SHOW_ALL_ONLINE + " OUTPUT:[" + nickNames +"]");
        return nickNames;
    }

   
}
