package chatserver;

import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.*;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private String login;
    private Server server;
    // private DataOutputStream outputStream;
    private InputStream inputStream;
    private BufferedReader reader;
    private OutputStream outputStream;
    private PrintWriter writer;
    private HashSet<String> topicSet = new HashSet<>();

    public ServerWorker(Server server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;
        try {
            this.outputStream = clientSocket.getOutputStream();
            this.writer = new PrintWriter(outputStream, true);

            this.inputStream = clientSocket.getInputStream();
            this.reader = new BufferedReader(new InputStreamReader(inputStream));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLogin() {
        return this.login;
    }

    private void sendMsg(String msg) throws IOException {
        outputStream.write(msg.getBytes());
    }

    private void handleLogin (OutputStream outputStream, String [] tokens) throws IOException { // Note : outputStream should be of datatype DataOutputStream if the other way around

        if(tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            if((login.equals("guest") && password.equals("guest")) || (login.equals("dhanesh") && password.equals("dhanesh")) || (login.equals("abc") && password.equals("abc"))) {
                String msg = "Login ok\n";
                // outputStream.write(msg.getBytes());
                // outputStream.writeUTF(msg);
                writer.println(msg);
                this.login = login;
                System.out.println("User logged in succesfully : " + login + "\n");

                ArrayList<ServerWorker> workers = server.getWorkerList();

                // Send the current user the list of all other online users
                for(ServerWorker worker : workers) {
                    if (worker.getLogin() != null) {
                        if(!worker.getLogin().equals(this.login)) {
                            String onlineMsg = "Online " + worker.getLogin() + "\n";
                            sendMsg(onlineMsg);
                        }
                    }
                    
                }
                // Send other users the information that the current user is online
                String onlineMsg = "Online " + this.login + "\n";
                for(ServerWorker worker : workers) {
                    if( worker.getLogin() != null) {
                        if(!worker.getLogin().equals(this.login)) {
                            worker.sendMsg(onlineMsg);
                        } 
                    }                                       
                }

            } else {
                String msg = "Login error\n";
                // outputStream.write(msg.getBytes());
                writer.println(msg);
                System.out.println("Login failed");
            }
        }
    }

    private void handleLogoff() throws IOException {

        server.removeWorker(this);
        ArrayList<ServerWorker> workers = server.getWorkerList();

        String onlineMsg = "Offline " + this.login + "\n";
        for(ServerWorker worker : workers) {
            if( worker.getLogin() != null) {
                if(!worker.getLogin().equals(this.login)) {
                    worker.sendMsg(onlineMsg);
                } 
            }                                       
        }

        clientSocket.close();
    }

    private void handleMessage(String [] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = "";
        for(int i = 2; i < tokens.length; i++) {
            body += tokens[i] + " ";
        }

        boolean isTopic = sendTo.charAt(0) == '#';

        ArrayList<ServerWorker> workers = server.getWorkerList();
        for(ServerWorker worker : workers) {
            if(isTopic) {
                if(worker.isMemberOfTopic(sendTo)) {
                    String outMsg = "msg " + sendTo + " " + login + ":" + body + "\n";
                    worker.sendMsg(outMsg);
                }
            } else {
                if(worker.getLogin().equals(sendTo)) {
                    String outMsg = "msg " + this.login + " " + body + "\n";
                    worker.sendMsg(outMsg);
                }
            }
            
        }


    }

    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    private void handleJoin(String [] tokens) {
        if(tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    private void handleLeave(String [] tokens) {
        if(tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    private  void handleClientSocket() throws IOException, InterruptedException {

        // InputStream inputStream = clientSocket.getInputStream();
        // BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // DataInputStream input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

        String line;
        // while((line = input.readUTF()) != null) {
        while((line = reader.readLine()) != null) {

            System.out.println(line);
            String [] tokens = line.split(" ");
            String msg;

            if(tokens != null && tokens.length > 0) {

                String cmd = tokens[0];
                if (cmd.equalsIgnoreCase("quit")) {
                    handleLogoff();
                    break;
                } else if (cmd.equalsIgnoreCase("login")) {
                    handleLogin(outputStream, tokens);
                } else if (cmd.equalsIgnoreCase("msg")) {
                    handleMessage(tokens);
                } else if(cmd.equalsIgnoreCase("join")) {
                    handleJoin(tokens);
                } else if (cmd.equalsIgnoreCase("leave")) {
                    handleLeave(tokens);
                } else{
                    msg = "Command not recognised" + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
    }

    public void run() {
        try {
            handleClientSocket();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}