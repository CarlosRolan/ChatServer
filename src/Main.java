import api.RequestHandler;
import controller.ClientChannel;
import controller.Server;

public class Main {

	static Server server = Server.getInstance();

	public static void main(String[] args) {
		// Main Thread listens and registers connections
		RequestHandler.newRequest("simpleMethod");
		ClientChannel cc;
		while (true) {
			cc = server.initChannel(server.listenConnections());
			cc.start();
		}
	}

}