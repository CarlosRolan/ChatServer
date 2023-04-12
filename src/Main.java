import connection.ClientChannel;
import controller.Server;

public class Main {

	public static void main(String[] args) {
		//ACEPTING CONNECTIONS
		while (true) {
			ClientChannel cc = new ClientChannel(Server.getInstance().listenConnections());
			cc.start();
			Server.getInstance().registerConnection(cc);
		}
	}

}