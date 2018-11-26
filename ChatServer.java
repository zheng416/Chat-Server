

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private final String file;


    private ChatServer(int port, String filename) {
        this.port = port;
        this.file = filename;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
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
        ChatServer server;

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");


        if (args.length == 2) {
            server = new ChatServer(Integer.parseInt(args[0]), args[1]);
            File file = new File(args[1]);
            try {
                Scanner scan = new Scanner(file);
                System.out.println("Banned Words File: " + args[1]);
                System.out.println("Banned Words:");
                while (scan.hasNextLine()) {
                    System.out.println(scan.nextLine());
                }
                System.out.println();
                System.out.println(sdf.format(date) + " Server waiting for Clients on port " + args[0] +".");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            server = new ChatServer(1500, "badwords.txt");
            File file = new File("badwords.txt");
            try {
                Scanner scan = new Scanner(file);
                System.out.println("Banned Words File: badwords.txt");
                System.out.println("Banned Words:");
                while (scan.hasNextLine()) {
                    System.out.println(scan.nextLine());
                }
                System.out.println();
                System.out.println(sdf.format(date) + " Server waiting for Clients on port 1500." );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        server.start();


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
                try {
                    close();
                    System.out.println(username + " disconnected with a LOGOUT message.");
                } catch (Exception f) {
                    System.out.println(username + " disconnected with a LOGOUT message.");
                }
                for (int i = 0; i < clients.size() ; i++) {
                    if (clients.get(i).username.equals(username)) {
                        remove(clients.get(i).id);
                    }
                }
            }
        }

        /*
         * This is what the client thread actually runs.
         */

        Date date;
        SimpleDateFormat sdf;
        @Override
        public void run() {
            // Read the username sent to you by client
            date();
            System.out.println(sdf.format(date) + " " + username + " just connected.");
            System.out.println(sdf.format(date) + " Server waiting for Clients on port " + socket.getLocalPort());
            boolean flag = true;
            while (flag) {
                String censored = "";
                try {
                    cm = (ChatMessage) sInput.readObject();
                    ChatFilter filter = new ChatFilter(file);
                    censored = filter.filter(cm.getMessage());
                } catch (IOException | ClassNotFoundException e) {
                    try {
                        close();
                        System.out.println(username + " disconnected with a LOGOUT message.");
                    } catch (Exception f) {
                        System.out.println(username + " disconnected with a LOGOUT message.");
                    }
                    for (int i = 0; i < clients.size() ; i++) {
                        if (clients.get(i).id == id) {
                            remove(clients.get(i).id);
                        }
                    }
                    break;
                }
                date();

                //System.out.print(sdf.format(date) + " " + username + ": " + censored);
//                try {
//                    sOutput.writeObject(sdf.format(date) + " " + username + ": " + censored);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                if (cm.getType() == 0) { //group message
                    System.out.print(sdf.format(date) + " " + username + ": " + censored);
//                    try {
//                        sOutput.writeObject(sdf.format(date) + " " + username + ": " + censored);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    writeMessage(sdf.format(date) + " " + username + ": " + censored);
                    broadcast(sdf.format(date) + " " + username + ": " + censored);
                } else if (cm.getType() == 2) {  //direct message
                    if (username.equals(cm.getRecipient())) {
                        writeMessage(sdf.format(date) + " Error! You cannot direct message yourself.");
                    } else {
                        System.out.print(sdf.format(date) + " " + username + " -> " + cm.getRecipient() + ": " + censored);
//                    try {
//                        sOutput.writeObject(sdf.format(date) + " " + username + " -> " + cm.getRecipient() + ": " + censored);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                        writeMessage(sdf.format(date) + " " + username + " -> " + cm.getRecipient() + ": " + censored);
                        directMessage(sdf.format(date) + " " + username + " -> " + cm.getRecipient() +
                                ": " + censored, cm.getRecipient());
                    }
                } else if (cm.getType() == 1) { //logout message
                    try {
                        close();
                        System.out.println(username + " disconnected with a LOGOUT message.");
                    } catch (Exception e) {
                        System.out.println(username + " disconnected with a LOGOUT message.");
                    }
                    for (int i = 0; i < clients.size() ; i++) {
                        if (clients.get(i).id == id) {
                            remove(clients.get(i).id);
                        }
                    }
                    flag = false;
                } else if (cm.getType() == 4) { //list
                    for (int i = 0; i < clients.size() ; i++) {
                        if (clients.get(i).id != id) {
                            writeMessage(clients.get(i).username + "\n");
                        }
                    }
                }
            }

        }

        private boolean writeMessage(String msg) {
//            if (clients.size() == 0) {
//                return false;
//            } else {
                try {
                    sOutput.writeObject(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
//            }
        }

        private synchronized void broadcast(String message) {
            for (int i = 0; i < clients.size() ; i++) {
               // System.out.println(clients.size());
                //clients.get(i).writeMessage(clients.get(i).cm.getMessage());
                if (!clients.get(i).username.equals(username)) {
                    try {
                        clients.get(i).sOutput.writeObject(message);
                        //System.out.println(clients.get(i).username);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private synchronized void remove(int id) {
            clients.remove(id);
        }

        private void close() {
            for (int i = 0; i < clients.size() ; i++) {
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

        private synchronized void date(){
            date = new Date();
            sdf = new SimpleDateFormat("HH:mm:ss");
        }

        private synchronized void directMessage(String message, String username) {
            for (int i = 0; i < clients.size() ; i++) {
                // System.out.println(clients.size());
                //clients.get(i).writeMessage(clients.get(i).cm.getMessage());
                if (clients.get(i).username.equals(username)) {
                    try {
                        clients.get(i).sOutput.writeObject(message);
                        //System.out.println(clients.get(i).username);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
