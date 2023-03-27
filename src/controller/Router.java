package controller;

import java.io.IOException;

import connection.ClientChannel;

public final class Router {

		public static void sendMsgto(String nick, String msg) {

			for (ClientChannel iterable : Server.getInstance().getOnlineChannels()) {
				if (nick.equals(iterable.getNick())) {
                    try {
                        iterable.writeClient(msg);
                    } catch (IOException e) {
                        System.out.println("Could not send msg to " + iterable.getNick());
                    }
                }
			}
		}

        
        public static ClientChannel getClient(long clientID) {
            for (ClientChannel iterable : Server.getInstance().getOnlineChannels()) {
                if (iterable.getId() == clientID) {
                    return iterable;
                }
            }
            return null;
        }

        public static ClientChannel getClient(String clientNick) {
            for (ClientChannel iterable : Server.getInstance().getOnlineChannels()) {
                if (iterable.getNick().equals(clientNick)) {
                    return iterable;
                }
            }
            return null;
        }

		public static void sendMsgToClient(long id) {
		
		}

        public static boolean isUserOnline(long selectedUserID) {
            for (ClientChannel iterable : Server.getInstance().getOnlineChannels()) {
                if (iterable.getId() == selectedUserID) {
                    return true;
                }
            }
    
            return false;
        }

        public static boolean isUserOnline(String selectedUserNick) {
            for (ClientChannel iterable : Server.getInstance().getOnlineChannels()) {
                if (iterable.getNick().equals(selectedUserNick)) {
                    return true;
                }
            }
    
            return false;
        }


        public static void sendMsgTo(String lineIn) {
        }
    }
	