import controller.Server;
import controller.connection.ClientConnection;

public class Main {

	static Server server = Server.getInstance();

	public static void main(String[] args) {
		// Main Thread listens and registers connections
		ClientConnection cc;
		while (true) {
			cc = new ClientConnection(server.listenConnections());
			cc.start();
		}
	}

}