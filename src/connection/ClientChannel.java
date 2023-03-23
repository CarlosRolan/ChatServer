package connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import controller.Server;
import controller.Request;
import controller.Router;

public class ClientChannel extends Thread implements ConStatusCodes {

	private Socket socket;
	private String nick;
	private BufferedReader br;
	private BufferedWriter bw;

	public String getNick() {
		return this.nick;
	}

	// Constructor
	public ClientChannel(Socket socket) {
		try {
			this.socket = socket;
			this.br = new BufferedReader(
					new InputStreamReader(this.socket.getInputStream()));
			this.bw = new BufferedWriter(
					new OutputStreamWriter(this.socket.getOutputStream()));
			this.nick = this.recieveNick();
			if (this.nick != null) {
				sendComfirmation();
			}
		} catch (IOException e) {
			System.out.println(ERROR_CHANNEL_INIT + this.nick);
		}
		System.out
				.println(SUCCESS_SHOW_CREDENTIALS + ":[" + this.getId() + "]-[" + this.nick + "]");
	}

	private void sendComfirmation() throws IOException {
		synchronized (this) {
			writeClient(COMFRIMATION_SUCCESS);
		}
	}

	private String recieveNick() {
		String clientNick = null;
		try {
			clientNick = this.br.readLine();
		} catch (IOException e) {
			System.out.println("Could not Recieved the client nick");
		}
		return clientNick;
	}

	private String readClient() throws IOException {
		return this.br.readLine();
	}

	public void writeClient(String msgToClient) throws IOException {
		System.out.println(msgToClient + "-->[" + this.nick + "]");
		this.bw.write(msgToClient);
		this.bw.newLine();
		this.bw.flush();
	}

	@Override
	public void run() {
		while (true) {
			String lineIn = "lineIn empty";
			try {
				lineIn = readClient();
				// Thread.sleep(1500);
				System.out.println("[" + this.nick + "]-->" + lineIn);
			} catch (IOException e) {
				System.out.println(
						"CONNECTION WITH [" + this.nick + "]" + "CLOSED");
				Server.getInstance().deleteConnection(this);
				break;
			}

			switch (lineIn) {
				case Request.SHOW_ONLINE:
					try {
						writeClient(Request.showOnlineUsers(this));
					} catch (IOException e) {

						e.printStackTrace();
					}
					break;

				case Request.NEW_CHAT:
					try {
						writeClient("Select user you wish to chat with");
						String nick = readClient();
						writeClient("Send msg to [" + nick +"]");
						String msg = readClient();
						Router.sendMsgto(nick, msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					break;
			}
		}

	}
}
