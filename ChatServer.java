

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private String badWords;


    private ChatServer(int port) {
        this.port = port;
    }

    private ChatServer(int port, String bad) {
        this.port = port;
        this.badWords = bad;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("Banned Words File: " + badWords);
                System.out.println("Banned Words:");
                String f = ChatServer.class.getResource("badWords.txt").getPath();
                File bad = new File(f);
                BufferedReader in = new BufferedReader(new FileReader(bad));
                String read = in.readLine();
                while (read != null) {
                    System.out.println(read);
                    read = in.readLine();
                }
                System.out.println();
                SimpleDateFormat hh = new SimpleDateFormat("HH:mm:ss");
                Date now = new Date();
                System.out.println(hh.format(now) + ": Sever waiting for Clients on Port " + port + ".");
                while (true) {
                    Socket socket = serverSocket.accept();
                    Runnable r = new ClientThread(socket, uniqueId++);
                    Thread t = new Thread(r);
                    clients.add((ClientThread) r);
                    t.start();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        boolean flag = true;
        while (flag) {
            Scanner scan = new Scanner(System.in);
            String a = scan.nextLine();
            String[] b = a.split(" ");
            if (b.length == 4) {
                flag = false;
                ChatServer server = new ChatServer(Integer.parseInt(b[2], 10), b[3]);
                server.start();
            } else if (b.length == 3) {
                flag = false;
                ChatServer server = new ChatServer(Integer.parseInt(b[2], 10));
                server.start();
            } else if (b.length == 2) {
                flag = false;
                ChatServer server = new ChatServer(1500);
                server.start();
            } else {
                flag = true;
            }
        }

    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            //System.out.println(username + ": Ping");
            SimpleDateFormat hh = new SimpleDateFormat("HH:mm:ss");
            Date now = new Date();
            System.out.println(hh.format(now) + " " + username + " just connected.");
            System.out.println(hh.format(now) + ": Sever waiting for Clients on Port " + port + ".");
            try {
                cm = (ChatMessage) sInput.readObject();
                while (cm.getType() != 1) {
                    broadcast(username + ": " + cm.getMessage());
                    cm = (ChatMessage) sInput.readObject();
                }
                close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private boolean writeMessage(String msg) {
            if (clients.size() == 0) {
                return false;
            } else {
                try {
                    sOutput.writeObject(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }

        private synchronized void broadcast(String msg) {
            for (int i = 0; i < clients.size(); i++) {
                //clients.get(i).writeMessage(clients.get(i).cm.getMessage());
                SimpleDateFormat hh = new SimpleDateFormat("HH:mm:ss");
                Date now = new Date();
                clients.get(i).writeMessage(hh.format(now) + " " + msg);
                System.out.println(hh.format(now) + " " + msg);
            }
        }

        private void close() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private synchronized void remove(int id) {
        clients.remove(id);
    }


}
