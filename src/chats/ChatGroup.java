package chats;

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
  
        
    }

}
