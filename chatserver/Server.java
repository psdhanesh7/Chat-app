package chatserver;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class Server extends Thread {

    private int serverPort;
    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    public ArrayList<ServerWorker> getWorkerList() {
        return this.workerList;
    }

    public Server(int port) {
        this.serverPort = port;
    }

    public void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println("Server is running");

            while(true) {

                System.out.println("About to accept client connection....");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);

                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);

                worker.start();

            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}