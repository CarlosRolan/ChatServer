package chat;

import java.io.IOException;
import java.util.ArrayList;

import connection.ClientChannel;

public class ChatGroup {

    private String chatName;
    private String chatID;
    private ClientChannel owner;
    private ArrayList<ClientChannel> members;

    public ChatGroup(String chatName, ClientChannel owner, ArrayList<ClientChannel> members) {
        this.owner = owner;
        this.members = members;
    }

    public ChatGroup() {
        
    }

    public void send(String msg) {
        for (ClientChannel iterable : this.members) {
            try {
                iterable.writeClient(msg);
            } catch (IOException e) {
                System.out.println(
                        "Could not send msg to chat member [" + iterable.getId() + "] (" + iterable.getNick() + ")");
            }

        }
    }

}
