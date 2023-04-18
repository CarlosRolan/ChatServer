import connection.ClientChannel;
import controller.Server;

public class Main {

	public static void main(String[] args) {
		//Main Thread listens and registers connections 
		while (true) {
			ClientChannel cc = new ClientChannel(Server.getInstance().listenConnections());
			cc.start();
			Server.getInstance().registerConnection(cc);
		}
	}

}