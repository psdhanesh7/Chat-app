package chatclient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Created by jim on 4/21/17.
 */
public class MessagePane extends JPanel implements MessageListener {

    private final ChatClient client;
    private final String login;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();

    public MessagePane(ChatClient client, String login) {
        this.client = client;
        this.login = login;

        client.addMessageListner(this);

        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText();
                // if(text.equalsIgnoreCase("Join Group Message")) {

                // } else {
                    client.msg(login, text);
                    listModel.addElement("You: " + text);
                    inputField.setText("");
                // }
                
            }
        });
    }

    @Override
    public void onMessage(String fromLogin, String msgBody) {
        if (login.equalsIgnoreCase(fromLogin)) {
            
            if(fromLogin.equalsIgnoreCase("#Group")) {
                String [] line;
                line = msgBody.split(":", 2);
                String user = line[0];
                if(!user.equalsIgnoreCase(this.client.getUserName())) {
                    listModel.addElement(msgBody);
                }  
            } else {
                String line = fromLogin + ": " + msgBody;
                listModel.addElement(line);                
            }
            
        }
    }
}
