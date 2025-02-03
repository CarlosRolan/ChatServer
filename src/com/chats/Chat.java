package com.chats;

import java.util.ArrayList;

import com.connection.ClientChannel;
import com.comunication.*;

public class Chat {

    private int cID;
    private String cTag;
    private ArrayList<ClientChannel> mParticipants;

    public int getcID() {
        return cID;
    }

    public String getChatString() {
        return cTag;
    }

    public Chat(int chatID, String chatTag, ClientChannel... participant) {
        cID = chatID;
        cTag = chatTag;
        mParticipants = new ArrayList<>();

        for (int index = 0; index < participant.length; index++) {
            mParticipants.add(participant[index]);
        }

    }

    public Chat(int chatID, String chatTag, ArrayList<ClientChannel> participants) {
        cID = chatID;
        cTag = chatTag;
        mParticipants = participants;

    }

    public void sendMessage(ClientChannel sender, Message msgToChat) {
        for (ClientChannel itera : mParticipants) {
            if (!sender.equals(sender)) {
                itera.writeClientMessage(msgToChat);
            }
        }
    }
}
