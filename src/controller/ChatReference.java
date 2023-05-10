package controller;

import java.util.ArrayList;

import controller.connection.ClientChannel;
import javafx.util.Pair;

public class ChatReference {


    private String chatName;
    private String chatDesc;
    private long chatID;

    private String history;

    private ArrayList<ClientChannel> participantsRefList = new ArrayList<>();

    public String getChatName() {
        return chatName;
    }
    public String getChatDesc() {
        return chatDesc;
    }
    public long getChatID() {
        return chatID;
    }

    public ChatReference(long id,String name, String desc, ArrayList<ClientChannel> members) {
        chatID = id;
        chatName = name;
        chatDesc = desc;
        participantsRefList = members;
    }

    public ChatReference(long id,String name, String desc, ClientChannel cc ) {
        chatID = id;
        chatName = name;
        chatDesc = desc;
        participantsRefList.add(cc);
    }

    public void addParticipant(ClientChannel cc) {
        participantsRefList.add(cc);
    }
}
