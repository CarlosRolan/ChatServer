package chats;

import connection.ClientChannel;

public class Chat {

    ClientChannel cEmisor;
    ClientChannel cReceptor;

    public Chat(ClientChannel emisor, ClientChannel receptor) {
        cEmisor = emisor;
        cReceptor = receptor;
    }

}
