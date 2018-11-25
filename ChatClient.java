import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
        ChatClient client;
        boolean flag = true;
        // Create your client and start it

        if (args.length == 3) {
            client = new ChatClient(args[2], Integer.parseInt(args[1]), args[0]);
        } else if (args.length == 2) {
            client = new ChatClient("localhost", Integer.parseInt(args[1]), args[0]);
        } else if (args.length == 1) {
            client = new ChatClient("localhost", 1500, args[0]);
        } else {
            client = new ChatClient("localhost", 1500, "Anonymous");
        }
        client.start();

        System.out.println("Connection accepted " + client.socket.getInetAddress().getHostName() + "/" +
                client.socket.getInetAddress().getHostAddress() + ":" + client.socket.getPort());

            Scanner scan = new Scanner(System.in);
        while (flag) {
            String text = scan.nextLine();

            if (text.equals("/list")) {
                client.sendMessage(new ChatMessage(4, "/list"));
            } else if (text.toLowerCase().equals("/logout")) {
                flag = false;
                try {
                    client.sendMessage(new ChatMessage(1, client.username));
                    client.sInput.close();
                    client.sOutput.close();
                    client.socket.close();
                    //System.out.println("Server has closed the connection");
                } catch (Exception e) {
                    System.out.println("Server has closed the connection");
                }

            } else if (text.length() > 3 && text.substring(0,4).equals("/msg")) {
                String[] wordsArr = text.split(" ");
                String mess = "";

                for (int i = 2; i < wordsArr.length ; i++) {
                    mess += wordsArr[i];
                    if (i < wordsArr.length - 1) {
                        mess += " ";
                    }
                }
                client.sendMessage(new ChatMessage(2, mess + '\n', wordsArr[1]));
            }  else {
                client.sendMessage(new ChatMessage(0, text + '\n'));
            }
        }

        // Send an empty message to the server
    }


    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            try {
                while (true) {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Server has closed the connection");
            }
        }
    }
}
