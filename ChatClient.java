import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

final class ChatClient {
	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;
	private Socket socket;

	private final String server;
	private final String username;
	private final int port;

	private ChatClient(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}

	/*
	 * This starts the Chat Client
	 */
	private boolean start() {
		// Create a socket
		try {
			socket = new Socket(server, port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Create your input and output streams
		try {
			sInput = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// This thread will listen from the server for incoming messages
		Runnable r = new ListenFromServer();
		Thread t = new Thread(r);
		t.start();

		// After starting, send the clients username to the server.
		try {
			sOutput.writeObject(username);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}


	/*
	 * This method is used to send a ChatMessage Objects to the server
	 */
	private void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/*
	 * To start the Client use one of the following command
	 * > java ChatClient
	 * > java ChatClient username
	 * > java ChatClient username portNumber
	 * > java ChatClient username portNumber serverAddress
	 *
	 * If the portNumber is not specified 1500 should be used
	 * If the serverAddress is not specified "localHost" should be used
	 * If the username is not specified "Anonymous" should be used
	 */
	public static void main(String[] args) {
		// Get proper arguments and override defaults

		// Create your client and start it
		boolean flag = true;
		while (flag) {
			Scanner scan = new Scanner(System.in);
			ArrayList<String> userName = new ArrayList<>();
			String a = scan.nextLine();
			String[] b = a.split(" ");
			if (b.length == 5) {
				//ChatClient client = new ChatClient(userName.get(4), Integer.parseInt(userName.get(3)), userName.get(2));
				flag = false;
				ChatClient client = new ChatClient(b[4], Integer.parseInt(b[3]), b[2]);
				client.start();
				client.sendMessage(new ChatMessage());
			} else if (b.length == 4) {
				flag = false;
				ChatClient client = new ChatClient("localhost", Integer.parseInt(b[3]), b[2]);
				client.start();
				client.sendMessage(new ChatMessage());
			} else if (b.length == 3) {
				flag = false;
				ChatClient client = new ChatClient("localhost", 1500, b[2]);
				client.start();
				client.sendMessage(new ChatMessage());
			} else if (b.length == 2) {
				flag = false;
				ChatClient client = new ChatClient("localhost", 1500, "CS 180 Student");
				client.start();
				client.sendMessage(new ChatMessage());
			} else {
				flag = true;
				if (b.length == 1 & b[0].equals("logout")) {

				}
			}
		}
	}


	/*
	 * This is a private class inside of the ChatClient
	 * It will be responsible for listening for messages from the ChatServer.
	 * ie: When other clients send messages, the server will relay it to the client.
	 */
	private final class ListenFromServer implements Runnable {
		public void run() {
			while (true) {
				try {
					String msg = (String) sInput.readObject();
					System.out.print(msg);
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
