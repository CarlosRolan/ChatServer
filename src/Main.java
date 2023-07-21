import api.RequestHandler;
import controller.Server;
import controller.connection.ClientChannel;

public class Main {

	static Server server = Server.getInstance();

	public static void main(String[] args) {
		// Main Thread listens and registers connections
		RequestHandler.newRequest("simpleMethod");
		ClientChannel cc;
		while (true) {
			cc = new ClientChannel(server.listenConnections());
			cc.start();
		}
	}

}