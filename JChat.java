 
import java.awt.EventQueue;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Date;
import java.text.SimpleDateFormat;

import java.net.*;
import java.io.*;

import org.json.simple.*;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import java.util.Timer;
import java.util.TimerTask;


public class JChat {

	private JFrame frame;
    private JTextField userName;
    private JTextField message;
    private JTextArea chats;
    private String dayte;
    private JScrollPane pane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
        
        EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JChat window = new JChat();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public JChat() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

        Date datetime = new Date();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.dayte = DATE_FORMAT.format(datetime);

		frame = new JFrame();
		frame.setBounds(100, 200, 730, 489);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
        
        // User Name
        JLabel lblName = new JLabel("Name:");
		lblName.setBounds(65, 30, 65, 14);
		frame.getContentPane().add(lblName);
        
        userName = new JTextField();
		userName.setBounds(128, 30, 150, 20);
		frame.getContentPane().add(userName);
		userName.setColumns(10);
        
        // chats
		JLabel lblchats = new JLabel("Chats:");
		lblchats.setBounds(65, 55, 65, 14);
		frame.getContentPane().add(lblchats);
				
		chats = new JTextArea();
        chats.setBounds(130, 55, 400, 200);
        chats.setLineWrap(true);
        chats.setEditable(false); // set textArea non-editable
        pane = new JScrollPane(chats, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setBounds(130, 55, 400, 200);
        frame.getContentPane().add(pane);
        
		// Message
        JLabel lblmessage = new JLabel("Message:");
		lblmessage.setBounds(65, 260, 65, 14);
		frame.getContentPane().add(lblmessage);
        
        message = new JTextField();
        message.setBounds(128, 260, 340, 20);
		frame.getContentPane().add(message);
        message.setColumns(10);
		
		JButton btnSubmit = new JButton("submit");
		
		btnSubmit.setBackground(Color.BLUE);
		btnSubmit.setForeground(Color.MAGENTA);
		btnSubmit.setBounds(465, 260, 70, 23);
		frame.getContentPane().add(btnSubmit);
        
        /**
         * Send the chat message
         */
        SendChatListener sendChatListener = new SendChatListener();
        btnSubmit.addActionListener(sendChatListener);
        message.addActionListener(sendChatListener);
        
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
               // System.out.println("Inside Timer Task" + System.currentTimeMillis());
                getChats();
            }
        };
        timer.schedule(task, 2000,2000);

    }

    private class SendChatListener implements ActionListener {
       
        public void actionPerformed(ActionEvent arg0) {
                
            // if the user is not populated, then prompt the user
            // and set the focus on the username textfield
            if(userName.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "UserName required!");
                userName.requestFocus();
                return;
            }

            // ensure there is a message to send  otherwise
            // keep the focus on the message testfield
            if(message.getText().isEmpty()) {
                message.requestFocus();
                return;
            }

            BufferedReader sendChatResponse;

            try {
                URL sendMessageURL = new URL("http://dbachor.com/NCC/post.php?user=" + userName.getText() + "&message=" + message.getText());

                URLConnection chatConnection = sendMessageURL.openConnection();
                sendChatResponse = new BufferedReader(
                                        new InputStreamReader(
                                            chatConnection.getInputStream()));
                sendChatResponse.close();
                // System.out.println(sendChatResponse);
            } catch(IOException ioex) {
                System.out.println("error sending chat message...");
            } 
     
            getChats();
            message.setText("");
            message.requestFocus();
        }
    } // end sendChatListener
    
    /**
     * retrieve the chat from the web service
     */
    private void getChats() 
    {        
        try {
            URL getChatMessages = new URL("http://dbachor.com/NCC/getChatLogJSON.php?startDateTime=" + URLEncoder.encode(this.dayte, "UTF-8"));
            
            //System.out.println(dayte);

            URLConnection messageConnection = getChatMessages.openConnection();
            
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                        messageConnection.getInputStream()));
            
            String inputLine;
            inputLine = in.readLine();
            
            JSONParser parser = new JSONParser();
            Object allChats = parser.parse(inputLine);
            
            JSONObject chatsObjects = (JSONObject)allChats;
            JSONArray chatsArray = (JSONArray) chatsObjects.get("chats");
            
            String chatsString = "";
            for(Object chat : chatsArray) {
                JSONObject achat = (JSONObject) chat;
                chatsString += "(" + achat.get("dayte") + ") " + achat.get("user") + ": " + achat.get("message") + "\n";
                //System.out.println(chatsString);
            }   
            
            chats.setText(chatsString);   
            in.close();
        } catch(IOException ioex) {
            System.out.println("error retrievings chat message...");
        } catch(ParseException pe){
            System.out.println("position: " + pe.getPosition());
            System.out.println(pe);
         }
    }
}
