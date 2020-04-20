package chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {

    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private PrintWriter writer;
    private BufferedReader bufferedIn;
    private String userName;

    private ArrayList<UserStatusListener> userStatusListners = new ArrayList<>();
    private ArrayList<MessageListener> messageListners = new ArrayList<>();

    public String getUserName() {
        return this.userName;
    }

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public void addMessageListner(MessageListener listner) {
        messageListners.add(listner);
    }

    public void removeMessageListner(MessageListener listner) {
        messageListners.remove(listner);
    }

    public void addUserStatusListner(UserStatusListener listner) {
        userStatusListners.add(listner);
    }

    public void removeUserStatusListner(UserStatusListener listner) {
        userStatusListners.remove(listner);
    }

    public boolean connect() {
        try {
            this.socket = new Socket(this.serverName, this.serverPort);
            this.serverIn = socket.getInputStream();
            this.serverOut = socket.getOutputStream();

            this.bufferedIn = new BufferedReader(new InputStreamReader(this.serverIn));
            this.writer = new PrintWriter(serverOut, true);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;   
    }

    private void startMessageReader() {
        Thread t = new Thread() {
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void handleOnline(String [] tokens) {
        String login = tokens[1];
        for(UserStatusListener listner : userStatusListners) {
            listner.online(login);
        }
    }

    private void handleOffline(String [] tokens) {
        String login = tokens[1];
        for(UserStatusListener listner : userStatusListners) {
            listner.offline(login);
        }
    }

    private void handleMessage(String [] tokens) {
        String login = tokens[1];
        String msgBody = "";
        for(int i = 2; i < tokens.length; i++) {
            msgBody += tokens[i] + " ";
        }

        for(MessageListener messageListner : messageListners) {
            messageListner.onMessage(login, msgBody);
        }
    }

    private void readMessageLoop() {
        
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
                String [] tokens = line.split(" ");
                if(tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if(cmd.equalsIgnoreCase("online")) {
                        handleOnline(tokens);
                    } else if (cmd.equalsIgnoreCase("offline")) {
                        handleOffline(tokens);
                    } else if (cmd.equalsIgnoreCase("msg")) {
                        handleMessage(tokens);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password;
        writer.println(cmd);

        String response = bufferedIn.readLine();
        System.out.println(response);
        System.out.println("Response Line : " + response);

        if(response.equalsIgnoreCase("login ok")) {
            this.userName = login;
            startMessageReader();
            return true;
        } else {
            return false;
        }
    }

    public void logoff() {
        String cmd = "quit\n";
        writer.println(cmd);
    }

    public void msg(String sendTo, String msgBody) {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        writer.println(cmd);
    }

    public void joinGroup(String group) {
        String cmd = "join " + group + "\n";
        writer.println(cmd);
    }
    
    public static void main(String[] args) throws IOException {
        // ChatClient client = new ChatClient("localhost", 8818);

        // client.addUserStatusListner(new UserStatusListener(){
        //     @Override
        //     public void online(String login) {
        //         System.out.println("ONLINE : " + login + "\n");
        //     }
        
        //     @Override
        //     public void offline(String login) {
        //         System.out.println("OFFLINE : " + login + "\n");
        //     }
        // });

        // client.addMessageListner(new MessageListener(){
        
        //     @Override
        //     public void onMessage(String fromLogin, String msgBody) {
        //         System.out.println("You got a message from " + fromLogin + "====> " + msgBody + "\n");
        //     }
        // });

        // if (!client.connect()) {
        //     System.err.println("Connection failed");
        // } else {
        //     System.out.println("Connected to server successfully");
        //     if (client.login("dhanesh", "dhanesh")) {
        //         System.out.println("Login Succesful");

        //         // client.msg("guest", "Hello world");
        //     } else {
        //         System.err.println("Login failed");
        //     }
        // }

        // client.logoff();
    }
}