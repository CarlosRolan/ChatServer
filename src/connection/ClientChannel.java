package connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import controller.Server;
import controller.Message;
import controller.Request;


public class ClientChannel extends Thread implements ConStatusCodes {

	private Socket pSocket = null;
	private String nick;

	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	public String getNick() {
		return nick;
	}

	// Constructor
	public ClientChannel(Socket socket) {

			pSocket = socket;

			try {
				oos = new ObjectOutputStream(pSocket.getOutputStream());
				ois = new ObjectInputStream(pSocket.getInputStream());

				if (presenting()) {
					sendComfirmation();
				} else {
					System.out.println(PRESENTATION_FAIL);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				
			}
			
	}

	private boolean presenting() {
		Message presentation = readClientMessage();
		if (presentation.getAction().equals(Request.PRESENTATION)) {
			nick = presentation.getEmisor();
			return true;
		} else {
			return false;
		}
	}

	private void sendComfirmation() {
		writeClientMessage(new Message(PRESENTATION_SUCCES));
	}

	public void writeClientMessage(Message msg) {
		try {
			oos.writeObject(msg);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Message readClientMessage() {
		try {
			return (Message) ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	

	

	@Override
	public void run() {
		while (true) {
			Message msg = readClientMessage();
			try {

			} catch (NullPointerException e) {
				System.out.println(
						CONNECTION_CLOSED + " [" + nick + "]" + "CLOSED");
				Server.getInstance().deleteConnection(this);
				break;
			}
		}

	}
}
