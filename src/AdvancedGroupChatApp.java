import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

public class AdvancedGroupChatApp extends JFrame {

	private JPanel contentPane;
	private JTextField usernameTextField;
	private JTextField friendtextField;
	private JTextField grouptextField;
	private JTextField postMessagetextField;
	
	private JLabel nameOfChatText; //dynamic
	
	JButton sendMessageButton, addFriendButton, inviteButton;
	JButton createGroupButton, registerFriendButton, deleteFriendButton;
	JToggleButton tglbtnStatus, tglbtnDisconnectconnect; //dynamic
	
	JTextArea messageListtextArea;
	
	private String wellKnownIP = "235.1.1.1";
	private int wellKnownPort = 1199;

	MulticastSocket multicastSocketMain = null;
	InetAddress multicastGroupMain = null;

	private ArrayList<String> listOfMyFriends = new ArrayList<String>();
	private ArrayList<String> listOfMyFriendsPort = new ArrayList<String>();

	private ArrayList<String> listOfMyGroups = new ArrayList<String>();
	private ArrayList<String> listOfMyGroupsIP = new ArrayList<String>();

	private String myUserName = "Anon";
	private int myPort = 0; 
	
	JList<String> friendList, groupList;
	
	ArrayList<Group> groupArray = new ArrayList<Group>();
	ArrayList<User> friendArray = new ArrayList<User>();
	
	DefaultListModel<String> modelGroup;
	DefaultListModel<String> modelFriend;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AdvancedGroupChatApp frame = new AdvancedGroupChatApp();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void establishMainConnection()
	{
		System.out.println("Establishing Main Connection");
		try {
			multicastGroupMain = InetAddress.getByName(wellKnownIP);
			multicastSocketMain = new MulticastSocket(wellKnownPort);
			//join
			multicastSocketMain.joinGroup(multicastGroupMain);
			
			new Thread(new Runnable(){
				
				@Override
				public void run(){
					byte buf1[] = new byte[1000];
					DatagramPacket dgpReceived = new DatagramPacket(buf1, buf1.length);
					
					while (true){
						try{
							multicastSocketMain.receive(dgpReceived);
							byte[] receivedData = dgpReceived.getData();
							int length = dgpReceived.getLength();
							
							String msg = new String(receivedData,0,length);
							debugMsg(msg);
							
							mainValidateAction(msg);
							
						}catch(IOException ex)
						{
							ex.printStackTrace();
						}
					}	
				}	
			}).start(); 	
		}catch (IOException ex){
			ex.printStackTrace();
		}
	}
	public void debugMsg(String msg)//Purpose is to help you view msg easier by appending it to the chat group/s msg
	{
		System.out.println("msg");
		messageListtextArea.append("Console Msg "+msg+"\n");
	}

	public void mainValidateAction(String msg)
	{
		String[] parts = msg.split("/");//splitting by "/"
		switch(parts[0]){
			case "U?": // MSG "U? {{username}} {{port}}"
				checkDuplicateUserOrPort(parts[1],parts[2]);
	
				break;
			case "U!": // MSG "U! {{username}} {{port}}"
				
				break;
		}
	}
	
	public void checkDuplicateUserOrPort(String username,String parts)
	{
		if(myUserName.equals(username))
		{
			//perform send that is duplicate
		}
		else if(myPort == Integer.parseInt(parts))
		{
			//perform send that is duplicate
		}
	}
	
	public void performSendToMain(String msg)
	{
		try{
			byte[] buf = msg.getBytes();
			DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroupMain, 6789);
			multicastSocketMain.send(dgpSend);;
			
		}catch (IOException ex ){
			ex.printStackTrace();
		}
	}

	public String generateRandomIP()
	{
		Random randomGenerator = new Random();
		
		int first = 239;
		int second = randomGenerator.nextInt(255);
		int third = randomGenerator.nextInt(255);
		int forth = randomGenerator.nextInt(255);
		return first+"."+second+"."+third+"."+forth;
	}

	/**
	 * Create the frame.
	 */
	public AdvancedGroupChatApp() {
		
		modelGroup = new DefaultListModel<>();
		modelFriend = new DefaultListModel<>();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 562, 391);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblUserName = new JLabel("User Name");
		lblUserName.setBounds(10, 11, 78, 14);
		contentPane.add(lblUserName);
		
		JLabel lblFriend = new JLabel("Friend");
		lblFriend.setBounds(10, 36, 46, 14);
		contentPane.add(lblFriend);
		
		JLabel lblGroup = new JLabel("Group");
		lblGroup.setBounds(10, 61, 46, 14);
		contentPane.add(lblGroup);
		
		usernameTextField = new JTextField();
		usernameTextField.setBounds(86, 8, 86, 20);
		contentPane.add(usernameTextField);
		usernameTextField.setColumns(10);
		
		friendtextField = new JTextField();
		friendtextField.setBounds(86, 33, 86, 20);
		contentPane.add(friendtextField);
		friendtextField.setColumns(10);
		
		grouptextField = new JTextField();
		grouptextField.setBounds(86, 58, 86, 20);
		contentPane.add(grouptextField);
		grouptextField.setColumns(10);
		
		registerFriendButton = new JButton("Register");
		registerFriendButton.setBounds(189, 7, 89, 23);
		contentPane.add(registerFriendButton);
		
		addFriendButton = new JButton("Add Friend");
		addFriendButton.setBounds(189, 32, 89, 23);
		contentPane.add(addFriendButton);
		
		createGroupButton = new JButton("Create");
		createGroupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		createGroupButton.setBounds(189, 57, 89, 23);
		contentPane.add(createGroupButton);
		
		deleteFriendButton = new JButton("Delete");
		deleteFriendButton.setBounds(10, 288, 104, 23);
		contentPane.add(deleteFriendButton);
		
		JLabel lblFriendList = new JLabel("Friend List");
		lblFriendList.setBounds(10, 110, 78, 14);
		contentPane.add(lblFriendList);
		
		friendList = new JList<String>(modelFriend);
		friendList.setBounds(10, 130, 104, 151);
		contentPane.add(friendList);
		
		JLabel lblGroupList = new JLabel("Group List");
		lblGroupList.setBounds(129, 110, 70, 14);
		contentPane.add(lblGroupList);
		
		groupList = new JList<String>(modelGroup);
		groupList.setBounds(124, 130, 117, 151);
		contentPane.add(groupList);
		
		messageListtextArea = new JTextArea();
		messageListtextArea.setBounds(251, 130, 285, 151);
		contentPane.add(messageListtextArea);
		
		JLabel lblMessage = new JLabel("Message");
		lblMessage.setBounds(251, 292, 46, 14);
		contentPane.add(lblMessage);
		
		postMessagetextField = new JTextField();
		postMessagetextField.setBounds(298, 289, 171, 20);
		contentPane.add(postMessagetextField);
		postMessagetextField.setColumns(10);
		
		sendMessageButton = new JButton("Send");
		sendMessageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		sendMessageButton.setBounds(479, 288, 57, 23);
		contentPane.add(sendMessageButton);
		
		tglbtnStatus = new JToggleButton("Status: online/offline");
		tglbtnStatus.setBounds(415, 7, 121, 23);
		contentPane.add(tglbtnStatus);
		
		inviteButton = new JButton("invite to ");
		inviteButton.setBounds(10, 319, 104, 23);
		contentPane.add(inviteButton);
		
		JLabel currentChatText = new JLabel("Current Chat:");
		currentChatText.setBounds(251, 110, 78, 14);
		contentPane.add(currentChatText);
		
		nameOfChatText = new JLabel("\"name of chat\"");
		nameOfChatText.setBounds(330, 110, 107, 14);
		contentPane.add(nameOfChatText);
		
		tglbtnDisconnectconnect = new JToggleButton("Disconnect/Connect");
		tglbtnDisconnectconnect.setBounds(415, 101, 121, 23);
		contentPane.add(tglbtnDisconnectconnect);
		
	}
	
	
}

class Group {
	
	private String IP;
	private String Name;
	
	public Group(String Name, String IP){
		this.IP = IP;
		this.Name = Name;
	}
	
	public String getName() {
		return Name;
	}

	public void setName(String Name) {
		this.Name = Name;
	}
	
	public String getIP() {
		return IP;
	}

	public void setIP(String IP) {
		this.IP = IP;
	}
}

class User{
	
	private String Port;
	private String Name;
	
	public User(String Name, String Port){
		this.Port = Port;
		this.Name = Name;
	}
	
	public String getName() {
		return Name;
	}

	public void setName(String Name) {
		this.Name = Name;
	}
	
	public String getPort() {
		return Port;
	}

	public void setPort(String Port) {
		this.Port = Port;
	}

}
