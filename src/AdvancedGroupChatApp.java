import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTextArea;

public class AdvancedGroupChatApp extends JFrame {

	private JPanel contentPane;
	private JTextField usernameTextField;
	private JTextField friendtextField;
	private JTextField grouptextField;
	private JTextField displayCurrentGrouptextField;
	private JTextField postMessagetextField;
	
	JButton sendMessageButton, leaveCurrentGroupButton, joinCurrentGroupButton, addFriendButton;
	JButton addGroupButton, registerFriendButton, deleteFriendButton, editGroupButton;
	
	JTextArea messageListtextArea;
	
	JList<String> friendList, groupList;
	
	MulticastSocket multicastSocket_Common = null;
	InetAddress multicastGroup_Common = null;
	MulticastSocket multicastSocket_Group = null;
	InetAddress multicastGroup_Group = null;
	
	ArrayList<String> groupArray = new ArrayList<String>();
	
	DefaultListModel<String> model;

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

	/**
	 * Create the frame.
	 */
	public AdvancedGroupChatApp() {
		
		model = new DefaultListModel<>();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 521, 362);
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
		
		addFriendButton = new JButton("Add");
		addFriendButton.setBounds(189, 32, 89, 23);
		contentPane.add(addFriendButton);
		
		addGroupButton = new JButton("Add");
		addGroupButton.setBounds(189, 57, 89, 23);
		contentPane.add(addGroupButton);
		
		deleteFriendButton = new JButton("Delete");
		deleteFriendButton.setBounds(291, 32, 89, 23);
		contentPane.add(deleteFriendButton);
		
		editGroupButton = new JButton("Edit");
		editGroupButton.setBounds(288, 57, 89, 23);
		contentPane.add(editGroupButton);
		
		JLabel lblFriendList = new JLabel("Friend List");
		lblFriendList.setBounds(10, 110, 78, 14);
		contentPane.add(lblFriendList);
		
		friendList = new JList();
		friendList.setBounds(10, 130, 65, 151);
		contentPane.add(friendList);
		
		JLabel lblGroupList = new JLabel("Group List");
		lblGroupList.setBounds(102, 110, 70, 14);
		contentPane.add(lblGroupList);
		
		groupList = new JList<String>(model);
		groupList.setBounds(97, 130, 65, 151);
		contentPane.add(groupList);
		
		displayCurrentGrouptextField = new JTextField();
		displayCurrentGrouptextField.setBounds(189, 107, 86, 20);
		contentPane.add(displayCurrentGrouptextField);
		displayCurrentGrouptextField.setColumns(10);
		
		joinCurrentGroupButton = new JButton("Join");
		joinCurrentGroupButton.setBounds(288, 106, 89, 23);
		contentPane.add(joinCurrentGroupButton);
		
		leaveCurrentGroupButton = new JButton("Leave");
		leaveCurrentGroupButton.setBounds(385, 106, 89, 23);
		contentPane.add(leaveCurrentGroupButton);
		
		messageListtextArea = new JTextArea();
		messageListtextArea.setBounds(189, 130, 285, 151);
		contentPane.add(messageListtextArea);
		
		JLabel lblMessage = new JLabel("Message");
		lblMessage.setBounds(10, 292, 65, 14);
		contentPane.add(lblMessage);
		
		postMessagetextField = new JTextField();
		postMessagetextField.setBounds(66, 289, 276, 20);
		contentPane.add(postMessagetextField);
		postMessagetextField.setColumns(10);
		
		sendMessageButton = new JButton("Send");
		sendMessageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					/*String msg = textField.getText();
					msg = userName + ": " + msg;
					byte[] buf = msg.getBytes();
					DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroupGroup, 6789);
					multicastSocketGroup.send(dgpSend);
					Thread.sleep(100);
					textField.setText("");*/
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		sendMessageButton.setBounds(362, 288, 89, 23);
		contentPane.add(sendMessageButton);
		
		try{
			
			multicastGroup_Common = InetAddress.getByName("235.1.1.1");
			multicastSocket_Common = new MulticastSocket(6789);
			// join
			multicastSocket_Common.joinGroup(multicastGroup_Common);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		Testing_addGroup();
		
		
	}
	
	public void Testing_addGroup(){
		String group1 = "GROUP-"+"2107-"+"228.1.2.3";
		String group2 = "GROUP-"+"2108-"+"228.1.2.4";
		String group3 = "GROUP-"+"2109-"+"228.1.2.5";
		String group4 = "GROUP-"+"2110-"+"228.1.2.6";
		
		groupArray.add(group1);
		groupArray.add(group2);
		groupArray.add(group3);
		groupArray.add(group4);
		
		for(int i=0; i<groupArray.size(); i++){
			
			String someString  = groupArray.get(i);
			String[] splittedArray = someString.split("-");
			
			model.addElement(splittedArray[1]);
			
		}
	}
}
