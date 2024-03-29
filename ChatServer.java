

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
    private ChatFilter fil;

    private ChatServer(int port) {
        this.port = port;
    }

    private ChatServer(int port, String bad) {
        this.port = port;
        this.badWords = bad;
        fil = new ChatFilter(badWords);
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
        /*boolean flag = true;
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
            */
        //if (args == null) {
        // ChatServer  server = new ChatServer(1500);
        //server.start();
        // } else {
        //*may be not a port number
        if (args.length == 2) {
            ChatServer server = new ChatServer(Integer.parseInt(args[0], 10), args[1]);
            server.start();
        } else {
            ChatServer server = new ChatServer(1500, "badwords.txt");
            server.start();
        }
        // }

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

        public int getId() {
            return this.id;
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            Boolean flag = true;
            //System.out.println(username + ": Ping");
            SimpleDateFormat hh = new SimpleDateFormat("HH:mm:ss");
            Date now = new Date();
            System.out.println(hh.format(now) + " " + username + " just connected.");
            System.out.println(hh.format(now) + ": Sever waiting for Clients on Port " + port + ".");
            try {
                while (flag) {
                    cm = (ChatMessage) sInput.readObject();
                    if (cm.getType() == 1) {
                        close();
                        clients.remove(id);
                        flag = false;
                    } else if (cm.getType() == 0) {
                        broadcast(username + ": " + cm.getMessage());
                    } else if (cm.getType() == 2) {
                        directMessage(hh.format(now) + " " + username + " -> " + cm.getRecipient() +
                                              ": " + cm.getMessage(), cm.getRecipient());
                        System.out.println(hh.format(now) + " " + username + " -> " + cm.getRecipient() + ": " + cm.getMessage());
                        writeMessage(hh.format(now) + " " + username + " -> " + cm.getRecipient() + ": " + cm.getMessage());
                    } else if (cm.getType() == 4) {
                        for (int i = 0; i < clients.size(); i++) {
                            if (clients.get(i).id != id) {
                                writeMessage(clients.get(i).username);
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private synchronized void directMessage(String message, String username)  {
            
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).username.equals(username)) {
                    try {
                        clients.get(i).sOutput.writeObject(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private boolean writeMessage(String msg) {
            if (clients.size() == 0) {
                return false;
            } else {
                try {
                    sOutput.writeObject(fil.filter(msg));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }

        private synchronized void broadcast(String msg) {
            SimpleDateFormat hh = new SimpleDateFormat("HH:mm:ss");
            Date now = new Date();
            for (int i = 0; i < clients.size(); i++) {
                //clients.get(i).writeMessage(clients.get(i).cm.getMessage());
                clients.get(i).writeMessage(hh.format(now) + " " + msg);
            }
            System.out.println(fil.filter(hh.format(now) + " " + msg));
        }

        private void close() {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).username.equals(username)) {
                    try {
                        clients.get(i).sOutput.close();
                        clients.get(i).sInput.close();
                        clients.get(i).socket.close();
                    } catch (IOException e) {
                        System.out.println(username + " disconnected with a LOGOUT message.");
                        i = clients.size();
                    }
                }
            }
        }

    }


    private synchronized void remove(int id) {
        int a = 0;
        int c = 0;
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getId() == id) {
                c = 1;
                a = i;
            }

        }
        if (c == 1) {
            clients.remove(a);
        }
    }


}
