package api;

import controller.Server;
import controller.connection.ClientConnection;

public class RequestRespond implements RequestCodes {

    public String[] showOnlineUsers(ClientConnection cc) {
        String[] allOnline = new String[Server.getInstance().getNumberOfOnlineUsers() - 1];
        int i = 0;

        for (ClientConnection iter : Server.getInstance().getOnlineCon()) {
            if (iter.getId() != cc.getId() && iter.getNick() != cc.getNick()) {
                allOnline[i] = iter.getId() + iter.getNick();
                i++;
            }

        }
        return allOnline;

    }

}
