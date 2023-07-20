import api.RequestHandler;
import controller.Server;
import controller.connection.ClientConnection;

public class Main {

	static Server server = Server.getInstance();

	public static void main(String[] args) {
		// Main Thread listens and registers connections
		RequestHandler.newRequest("simpleMethod");
		ClientConnection cc;
		while (true) {
			cc = new ClientConnection(server.listenConnections());
			cc.start();
		}
	}

}