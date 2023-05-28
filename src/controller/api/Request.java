package controller.api;

import controller.Message;
import controller.Message.MsgType;
import controller.Server;
import controller.chat.ChatReference;
import controller.connection.ClientChannel;

public class Request implements RequestCodes {

    public Server server = Server.getInstance();

    // SERVER ACTIONS
    public void showOnlineUsers(ClientChannel requester) {
        String[] nickNames = new String[server.getNumberOfOnlineUsers() - 1];
        int i = 0;

        for (ClientChannel iter : server.getOnlineChannels()) {
            String userID = String.valueOf(iter.getId());
            if (!requester.getNick().equals(iter.getNick())) {
                nickNames[i++] = userID + "_" + iter.getNick();
            }

        }
        requester.writeClientMessage(
                new Message(MsgType.REQUEST, SHOW_ALL_ONLINE, "SERVER", requester.getNick(), nickNames));
    }

    public void showAllChatsForUser(ClientChannel requester) {
        String[] chatOption = new String[server.getChatRefs().size()];
        int i = 0;

        for (ChatReference iter : server.getChatRefs()) {
            System.out.println(iter.toString());
            chatOption[i] = iter.toString();
            i++;
        }
        requester
                .writeClientMessage(new Message(MsgType.REQUEST, SHOW_ALL_CHATS, chatOption));
    }

    // SINGLE CONVERSATION BEETWEEN 2 USERS ONLY
    public void requestSingle(ClientChannel requester, ClientChannel receptor) {

        if (receptor == null) {
            // CLient not foud
            System.out.println(CLIENT_NOT_FOUND);
            requester.writeClientMessage(new Message(MsgType.ERROR, CLIENT_NOT_FOUND));
        } else if (requester.getNick().equals(receptor.getNick()) | requester.getId() == receptor.getId()) {
            // Is trying to talk with hiimself
            System.out.println(SELF_REFERENCE);
            requester.writeClientMessage(new Message(MsgType.ERROR, SELF_REFERENCE));
        } else {
            // To de emirsor
            Message waitingMsg = new Message(MsgType.REQUEST, WAITING_FOR_PERMISSION, receptor.getNick(),
                    requester.getNick());
            System.out.println(waitingMsg);
            requester.writeClientMessage(waitingMsg);
            // To the receptor
            Message permitChat = new Message(MsgType.REQUEST, ASKED_FOR_PERMISSION, requester.getNick(),
                    receptor.getNick());
            System.out.println(permitChat);
            receptor.writeClientMessage(permitChat);
        }
    }

    public void startSingle(ClientChannel requester, ClientChannel receptor) {
        Message forRequested = new Message(MsgType.REQUEST, START_SINGLE, "SERVER", requester.getNick());
        Message forAsked = new Message(MsgType.REQUEST, START_SINGLE, "SERVER", receptor.getNick());

        System.out.println("========COMFIRMATION MSGS====");
        System.out.println(forRequested);
        System.out.println(forAsked);
        System.out.println("=============================");

        requester.setChatting(true);
        requester.writeClientMessage(forRequested);
        receptor.setChatting(true);
        receptor.writeClientMessage(forAsked);

    }

    public void denySingle(ClientChannel emisor, ClientChannel requester) {
        Message denyForRequester = new Message(MsgType.REQUEST, DENY, emisor.getNick(), requester.getNick(),
                emisor.getNick() + " no quiere chatear contigo");
        requester.setChatting(false);
        requester.writeClientMessage(denyForRequester);
    }

    public void sendDirectMessage(String emisorNick, ClientChannel receptor, String textMsg) {
        receptor.writeClientMessage(
                new Message(MsgType.MESSAGE, SEND_DIRECT_MSG, emisorNick, receptor.getNick(), textMsg));
    }

    // CHATS
    public void openChat(ClientChannel requester, ChatReference chatRef) {
        if (chatRef == null) {
            requester.writeClientMessage(new Message(MsgType.ERROR, CHAT_NOT_FOUND));
        } else {
            requester.writeClientMessage(new Message(MsgType.REQUEST, START_CHAT, String.valueOf(chatRef.getChatID())));
        }
    }

    public void registerChat(ClientChannel requester, ChatReference chatRef) {
        requester.writeClientMessage(new Message(MsgType.REQUEST, CHAT_REGISTERED, String.valueOf(chatRef.getChatID()),
                chatRef.getChatName(), chatRef.getChatDesc()));

    }

    public void sendToChat(ClientChannel emisor, ChatReference chatRef, String text) {
        for (ClientChannel iter : chatRef.getAllParticipants()) {
            iter.writeClientMessage(
                    new Message(MsgType.MESSAGE, FROM_CHAT, emisor.getNick(), String.valueOf(chatRef.getChatID()),
                            text));
        }
    }

    public void showAllMembers(ClientChannel requester, ChatReference ref) {
        String[] members = new String[ref.getAllParticipants().size() - 1];
        int i = 0;
        for (ClientChannel iter : ref.getAllParticipants()) {
            String userID = String.valueOf(iter.getId());
            if (!requester.getNick().equals(iter.getNick())) {
                members[i++] = userID + "_" + iter.getNick();
            }
        }
        requester.writeClientMessage(new Message(MsgType.REQUEST, SHOW_ALL_MEMBERS, members));
    }

    public void addMember(ClientChannel admin, ClientChannel newMember, ChatReference chatRef) {
        chatRef.addParticipant(newMember);
        server.updateChat(chatRef);
        admin.writeClientMessage(
                new Message(MsgType.REQUEST, ADD_MEMBER, String.valueOf(chatRef.getChatID()), newMember.getNick()));
        newMember.writeClientMessage(
                new Message(MsgType.REQUEST, ADD_MEMBER, String.valueOf(chatRef.getChatID()), newMember.getNick()));

    }
}
