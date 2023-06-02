package controller.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import api.RequestCodes;
import api.RequestMethods;
import controller.Msg;
import controller.Msg.MsgType;
import controller.Server;
import controller.chat.ChatReference;
import log.ClientLog;

public class ClientChannel extends Thread implements RequestCodes, RequestMethods {

    public Server server = Server.getInstance();

    private Socket pSocket = null;
    private String nick;
    private ClientLog cLog;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private boolean chatting = false;

    public boolean isChatting() {
        return chatting;
    }

    public void setChatting(boolean isChatting) {
        chatting = isChatting;
    }

    public String getNick() {
        return nick;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public void setOis(ObjectInputStream newOis) {
        ois = newOis;
    }

    public void SetOos(ObjectOutputStream newOos) {
        oos = newOos;
    }

    // Constructor
    public ClientChannel(Socket socket) {

        pSocket = socket;

        try {
            oos = new ObjectOutputStream(pSocket.getOutputStream());
            ois = new ObjectInputStream(pSocket.getInputStream());

            if (presenting()) {
                sendComfirmation();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean presenting() {
        Msg presentation = readClientMessage();
        System.out.println("[" + presentation.getEmisor() + "] IS ACCEPTED/online");
        if (presentation.getAction().equals(PRESENT)) {
            nick = presentation.getEmisor();
            return true;
        } else {
            return false;
        }
    }

    private void sendComfirmation() {
        cLog = new ClientLog(this);
        Msg comfirmation = new Msg(MsgType.REQUEST);
        comfirmation.setAction(PRESENTATION_SUCCES);
        comfirmation.setReceptor(getNick());
        writeClientMessage(comfirmation);
        System.out.println("SENDING COMFIRMATION TO [" + comfirmation.getReceptor() + "]");
        cLog.logIn();
    }

    public void writeClientMessage(Msg msg) {
        cLog.log("OUT==>" + msg.toString());
        try {
            oos.writeObject(msg);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Msg readClientMessage() {
        try {
            Msg msg = (Msg) ois.readObject();
            return msg;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.err.println("[" + getNick() + "] HAS LEFT");
            return null;
        }
    }

    public void handleRequest(Msg msg) {

        ChatReference chatReference = null;
        ClientChannel requested = null;

        cLog.log(msg.toString());

        switch (msg.getAction()) {

            case SHOW_ALL_MEMBERS:
                ChatReference selectedChat = Server.getInstance().getChatReferenceByID(Long.valueOf(msg.getReceptor()));
                showAllMembers(this, selectedChat);
                break;
            case SHOW_ALL_ONLINE:
                showOnlineUsers();
                break;

            // REQUEST SINGLE CHAT AS A EMISOR
            // TODO refractor
            case SINGLE_REQUESTED:
                requested = null;
                // We find the picked user by the requester=emisor
                if (msg.getText().equals(BY_NICK)) {
                    requested = Server.getInstance().getOnlineUserByNick(msg.getReceptor());
                } else if (msg.getText().equals(BY_ID)) {
                    requested = Server.getInstance().getOnlineUserByID(msg.getReceptor());
                }

                requestSingle(requested);

                break;

            // REQUESTED CHAT AS A RECEPTOR = EMISOR
            case ALLOW:
                requested = Server.getInstance().getOnlineUserByNick(msg.getReceptor());
                System.out.println(nick + " accepted. Startting chat with " + requested.nick);
                startSingle(requested);
                break;

            case DENY:
                requested = Server.getInstance().getOnlineUserByNick(msg.getReceptor());
                denySingle(requested);
                break;

            case SEND_DIRECT_MSG:
                System.out.println(msg.toString());
                requested = Server.getInstance().getOnlineUserByNick(msg.getReceptor());
                sendDirectMessage(requested, msg.getText());
                break;

            case NEW_CHAT:
                long chatID = 100 + this.getId();
                String chatTitle = msg.getReceptor();
                String chatDesc = msg.getText();
                ChatReference ref = new ChatReference(chatID, chatTitle, chatDesc, this);
                Server.getInstance().registerChat(ref);
                registerChat(this, ref);
                break;

            case SHOW_ALL_CHATS:
                showAllChatsForUser();
                break;

            case START_CHAT:
                chatReference = Server.getInstance().getChatReferenceByID(Long.valueOf(msg.getEmisor()));
                openChat(chatReference);
                break;

            case ADD_MEMBER:
                chatReference = Server.getInstance().getChatReferenceByID(Long.valueOf(msg.getEmisor()));
                ClientChannel newMember = Server.getInstance().getOnlineUserByID(msg.getReceptor());
                addMember(newMember, chatReference);
                break;

            case TO_CHAT:
                ChatReference refs = Server.getInstance().getChatReferenceByID(Long.valueOf(msg.getEmisor()));
                sendToChat(refs, msg.getReceptor());
                break;
        }
    }

    // TODO When an user ledts one single chat, disconnets the other
    @Override
    public void run() {
        while (true) {
            Msg msg = readClientMessage();
            try {
                System.out.println(msg.toString());
                handleRequest(msg);
            } catch (NullPointerException e) {
                System.out.println(
                        CONNECTION_CLOSED + " [" + nick + "]");
                Server.getInstance().deleteConnection(this);
                break;
            }
        }

    }

    @Override
    public void showOnlineUsers() {
        String[] nickNames = new String[server.getNumberOfOnlineUsers() - 1];
        int i = 0;

        Msg showAllOnline = new Msg(MsgType.REQUEST);
        showAllOnline.setAction(SHOW_ALL_ONLINE);
        showAllOnline.setReceptor(getNick());

        for (ClientChannel iter : server.getOnlineChannels()) {
            String userID = String.valueOf(iter.getId());
            if (!getNick().equals(iter.getNick())) {
                nickNames[i++] = userID + "_" + iter.getNick();
                showAllOnline.setParameter(i, userID + "_" + iter.getNick());
            }

        }

        writeClientMessage(showAllOnline);
    }

    @Override
    public void showAllChatsForUser() {
        String[] chatOption = new String[server.getChatRefs().size()];
        int i = 0;

        Msg showAllChats = new Msg(MsgType.REQUEST);
        showAllChats.setAction(SHOW_ALL_CHATS);

        for (ChatReference iter : server.getChatRefs()) {
            System.out.println(iter.toString());
            chatOption[i] = iter.toString();
            showAllChats.setParameter(i, iter.toString());
            i++;
        }

        writeClientMessage(showAllChats);
    }

    @Override
    public void requestSingle(ClientChannel requested) {
        Msg errorMsg = new Msg(MsgType.ERROR);

        if (requested == null) {
            // CLient not foud
            System.out.println(CLIENT_NOT_FOUND);
            errorMsg.setAction(CLIENT_NOT_FOUND);
            writeClientMessage(errorMsg);
        } else if (getNick().equals(requested.getNick()) | getId() == requested.getId()) {
            // Is trying to talk with hiimself
            System.out.println(SELF_REFERENCE);
            errorMsg.setAction(SELF_REFERENCE);
            writeClientMessage(errorMsg);
        } else {
            // To de emirsor
            Msg waiting = new Msg(MsgType.REQUEST);
            waiting.setAction(WAITING_FOR_PERMISSION);
            waiting.setEmisor(requested.getNick());
            waiting.setReceptor(getNick());
            System.out.println(waiting);
            writeClientMessage(waiting);
            // To the receptor
            Msg asking = new Msg(MsgType.REQUEST);
            asking.setAction(ASKED_FOR_PERMISSION);
            asking.setEmisor(getNick());
            asking.setReceptor(requested.getNick());
            System.out.println(asking);
            requested.writeClientMessage(asking);
        }
    }

    @Override
    public void startSingle(ClientChannel requested) {
        Msg forRequested = new Msg(MsgType.REQUEST);
        forRequested.setAction(START_SINGLE);
        forRequested.setReceptor(getNick());

        Msg forAsked = new Msg(MsgType.REQUEST);
        forAsked.setAction(START_SINGLE);
        forAsked.setReceptor(requested.getNick());

        System.out.println("========COMFIRMATION MSGS====");
        System.out.println(forRequested);
        System.out.println(forAsked);
        System.out.println("=============================");

        setChatting(true);
        writeClientMessage(forRequested);
        requested.setChatting(true);
        requested.writeClientMessage(forAsked);
    }

    @Override
    public void denySingle(ClientChannel requested) {
        Msg deny = new Msg(MsgType.REQUEST);
        deny.setAction(DENY);
        deny.setEmisor(getNick());
        deny.setReceptor(requested.getNick());
        deny.setBody(getNick() + " no quiere chatear contigo");

        requested.setChatting(false);
        requested.writeClientMessage(deny);
    }

    @Override
    public void sendDirectMessage(ClientChannel requested, String textMsg) {
        Msg direct = new Msg(MsgType.MESSAGE);
        direct.setAction(SEND_DIRECT_MSG);
        direct.setEmisor(getNick());
        direct.setReceptor(requested.getNick());
        direct.setBody(textMsg);

        requested.writeClientMessage(direct);
    }

    @Override
    public void openChat(ChatReference chatRef) {
        if (chatRef == null) {
            Msg chatNotFound = new Msg(MsgType.ERROR);
            chatNotFound.setAction(CHAT_NOT_FOUND);
            writeClientMessage(chatNotFound);
        } else {
            Msg startChat = new Msg(MsgType.REQUEST);
            startChat.setAction(START_CHAT);
            startChat.setEmisor(String.valueOf(chatRef.getChatID()));
            writeClientMessage(startChat);
        }
    }

    @Override
    public void registerChat(ClientChannel requester, ChatReference chatRef) {
        Msg registerChat = new Msg(MsgType.REQUEST);
        registerChat.setAction(CHAT_REGISTERED);
        registerChat.setEmisor(String.valueOf(chatRef.getChatID()));
        registerChat.setReceptor(chatRef.getChatName());
        registerChat.setBody(chatRef.getChatDesc());

        requester.writeClientMessage(registerChat);
    }

    @Override
    public void sendToChat(ChatReference chatRef, String text) {
        for (ClientChannel iter : chatRef.getAllParticipants()) {
            Msg toChat = new Msg(MsgType.MESSAGE);
            toChat.setEmisor(getNick());
            toChat.setReceptor(String.valueOf(chatRef.getChatID()));
            toChat.setBody(text);

            iter.writeClientMessage(toChat);
        }
    }

    @Override
    public void showAllMembers(ClientChannel requester, ChatReference ref) {
        String[] members = new String[ref.getAllParticipants().size() - 1];
        int i = 0;

        Msg allMembers = new Msg(MsgType.REQUEST);
        allMembers.setAction(SHOW_ALL_ONLINE);

        for (ClientChannel iter : ref.getAllParticipants()) {
            String userID = String.valueOf(iter.getId());
            if (!requester.getNick().equals(iter.getNick())) {
                members[i++] = userID + "_" + iter.getNick();
                allMembers.setParameter(i++, userID + "_" + iter.getNick());
            }
        }
        requester.writeClientMessage(allMembers);
    }

    @Override
    public void addMember(ClientChannel newMember, ChatReference chatRef) {
        chatRef.addParticipant(newMember);
        server.updateChat(chatRef);

        Msg addMember = new Msg(MsgType.REQUEST);
        addMember.setAction(ADD_MEMBER);
        addMember.setEmisor(String.valueOf(chatRef.getChatID()));
        addMember.setReceptor(newMember.getNick());

        writeClientMessage(addMember);
        newMember.writeClientMessage(addMember);

    }

}
