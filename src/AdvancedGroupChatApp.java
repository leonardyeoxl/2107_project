import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;

public class AdvancedGroupChatApp extends JFrame {

	private JPanel contentPane;
	private JTextField usernameTextField;
	private JTextField friendtextField;
	private JTextField grouptextField;
	private JTextField displayCurrentGrouptextField;
	private JTextField postMessagetextField;
	
	JButton sendMessageButton, leaveCurrentGroupButton;
	
	JTextArea messageListtextArea;

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
		
		JButton registerFriendButton = new JButton("Register");
		registerFriendButton.setBounds(189, 7, 89, 23);
		contentPane.add(registerFriendButton);
		
		JButton addFriendButton = new JButton("Add");
		addFriendButton.setBounds(189, 32, 89, 23);
		contentPane.add(addFriendButton);
		
		JButton addGroupButton = new JButton("Add");
		addGroupButton.setBounds(189, 57, 89, 23);
		contentPane.add(addGroupButton);
		
		JButton deleteFriendButton = new JButton("Delete");
		deleteFriendButton.setBounds(291, 32, 89, 23);
		contentPane.add(deleteFriendButton);
		
		JButton editGroupButton = new JButton("Edit");
		editGroupButton.setBounds(288, 57, 89, 23);
		contentPane.add(editGroupButton);
		
		JLabel lblFriendList = new JLabel("Friend List");
		lblFriendList.setBounds(10, 110, 78, 14);
		contentPane.add(lblFriendList);
		
		JTextArea friendListTextArea = new JTextArea();
		friendListTextArea.setBounds(10, 130, 65, 151);
		contentPane.add(friendListTextArea);
		
		JLabel lblGroupList = new JLabel("Group List");
		lblGroupList.setBounds(102, 110, 70, 14);
		contentPane.add(lblGroupList);
		
		JTextArea groupListTextArea = new JTextArea();
		groupListTextArea.setBounds(97, 130, 65, 151);
		contentPane.add(groupListTextArea);
		
		displayCurrentGrouptextField = new JTextField();
		displayCurrentGrouptextField.setBounds(189, 107, 86, 20);
		contentPane.add(displayCurrentGrouptextField);
		displayCurrentGrouptextField.setColumns(10);
		
		JButton joinCurrentGroupButton = new JButton("Join");
		joinCurrentGroupButton.setBounds(288, 106, 89, 23);
		contentPane.add(joinCurrentGroupButton);
		
		leaveCurrentGroupButton = new JButton("Leave");
		leaveCurrentGroupButton.setBounds(385, 106, 89, 23);
		contentPane.add(leaveCurrentGroupButton);
		
		messageListtextArea = new JTextArea();
		messageListtextArea.setBounds(189, 130, 285, 151);
		contentPane.add(messageListtextArea);
		
		JLabel lblMessage = new JLabel("Message");
		lblMessage.setBounds(10, 292, 46, 14);
		contentPane.add(lblMessage);
		
		postMessagetextField = new JTextField();
		postMessagetextField.setBounds(66, 289, 276, 20);
		contentPane.add(postMessagetextField);
		postMessagetextField.setColumns(10);
		
		sendMessageButton = new JButton("Send");
		sendMessageButton.setBounds(362, 288, 89, 23);
		contentPane.add(sendMessageButton);
		
		
		
		
	}
}
