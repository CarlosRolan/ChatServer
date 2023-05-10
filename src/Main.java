import controller.Server;
import controller.connection.ClientChannel;

public class Main {

	public static void main(String[] args) {
		// Main Thread listens and registers connections
		ClientChannel cc;
		while (true) {
			cc = new ClientChannel(Server.getInstance().listenConnections());
			cc.start();
			Server.getInstance().registerConnection(cc);
		}
	}

}