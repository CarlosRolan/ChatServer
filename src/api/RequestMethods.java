package api;

import controller.chat.ChatReference;
import controller.connection.ClientChannel;

public interface RequestMethods {

    public void showOnlineUsers();

    public void showAllChatsForUser();

    public void requestSingle(ClientChannel requested);

    public void startSingle(ClientChannel requested);

    public void denySingle(ClientChannel requested);

    public void sendDirectMessage(ClientChannel receptor, String textMsg);

    public void openChat(ChatReference chatRef);

    public void registerChat(ClientChannel requester, ChatReference chatRef);

    public void sendToChat(ChatReference chatRef, String text);

    public void showAllMembers(ClientChannel requester, ChatReference ref);

    public void addMember(ClientChannel newMember, ChatReference chatRef);
}
