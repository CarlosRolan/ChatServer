package controller;

import java.io.IOException;

import connection.ClientChannel;

public final class Router {

		public static void sendMsgto(String nick, String msg) {

			for (ClientChannel iterable : Server.getInstance().getChannels()) {
				if (nick.equals(iterable.getNick())) {
                    try {
                        iterable.writeClient(msg);
                    } catch (IOException e) {
                        System.out.println("Could not send msg to " + iterable.getNick());
                    }
                }
			}
			
		}

		public static void sendMsgToClient(long id) {
		
		}
    }
	