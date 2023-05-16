package controller.chat;

import java.util.ArrayList;

import controller.connection.ClientChannel;

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

    public ArrayList<ClientChannel> getAllParticipants() {
        return participantsRefList;
    }

    public ChatReference(long id, String name, String desc, ArrayList<ClientChannel> members) {
        chatID = id;
        chatName = name;
        chatDesc = desc;
        participantsRefList = members;
    }

    public ChatReference(long id, String name, String desc, ClientChannel cc) {
        chatID = id;
        chatName = name;
        chatDesc = desc;
        participantsRefList.add(cc);
    }

    public void addParticipant(ClientChannel cc) {
        participantsRefList.add(cc);
    }

    public String listParticipantsNames() {
        String names = "";
        for (ClientChannel iter : participantsRefList) {
            names += iter.getNick() + "\n";
        }
        return names;
    }

    @Override
    public String toString() {
        return "[" + chatID + "]" + chatName + "{" + chatDesc + "}" + "\n"
                + listParticipantsNames();
    }

}
