import java.awt.BorderLayout;

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
	
	InetAddress multicastGroup_Group = null;
	MulticastSocket multicastSocket_Group = null;

	private ArrayList<User> listOfMyFriends = new ArrayList<User>();
	private ArrayList<String> listOfMyFriendsPort = new ArrayList<String>();
	private ArrayList<User> fullListOfMyFriends = new ArrayList<User>();

	private ArrayList<Group> listOfMyGroups = new ArrayList<Group>();
	private ArrayList<String> listOfMyGroupsIP = new ArrayList<String>();

	private String myUserName = "Anon";
	private int myPort = 5000; 
	
	JList<String> friendList, groupList;
	
	DefaultListModel<String> modelGroup;
	DefaultListModel<String> modelMultipleFriends;
	DefaultListModel<String> modelFriend;
	
	boolean checkAcceptOfReject = true;
	
	ActionListener actionListener;
	
	boolean selected = true;
	
	//String user = "";
	
	final User user = new User();
	
	DatagramSocket myUnicastDS; 
	DatagramPacket myUnicastDP;
	
	boolean isInGroupChat = false;

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

	public void mainValidateAction(String msg)
	{
		String[] parts = msg.split("/");//splitting by "/"
		switch(parts[0]){
			case "U?": // MSG "U? {{username}} {{port}}"
				checkDuplicateUserOrPort(parts[1],parts[2]);
	
				break;
			case "U!": // MSG "U! {{username}} {{port}}"
				
				break;
				// for friend requests
			case"A?":
				// A/ stands for add friend command "A?/friendName/myName/myPort"
				if(parts[1].equals(user.getName())){
					String replyRequest;
					int answer = JOptionPane.showConfirmDialog(null, "Ask for confirmation (returns an int)",
				            "Confirm Dialog", JOptionPane.YES_NO_OPTION);
					if(answer == 0){
						replyRequest = "Accepted";
						User tempUser = new User();
						tempUser.setName(parts[2]);
						tempUser.setPort(parts[3]);
						listOfMyFriends.add(tempUser);
						fullListOfMyFriends.add(tempUser);
						int pos = modelFriend.getSize();
						modelFriend.add(pos, tempUser.getName());
					}
					else{
						replyRequest = "Rejected";
					}
					replyRequest = "A!/" + replyRequest + "/" + user.getName() + "/" + user.getPort() + "/" + parts[2];
					performSendToMain(replyRequest);
				}
				break;
			case"A!":
				// reply format "A!/acceptOrReject/FriendName/userPort/myName"
				if(parts[4].equals(user.getName())){
				switch(parts[1]){
				case"Accepted":
					// if accepted, add to list. Not appended straight to text area
					// to prevent cases of friends in the middle quitting
					User tempUser = new User();
					tempUser.setName(parts[2]);
					tempUser.setPort(parts[3]);
					listOfMyFriends.add(tempUser);
					fullListOfMyFriends.add(tempUser);
					int pos = modelFriend.getSize();
					modelFriend.add(pos, tempUser.getName());
					break;
				case"Rejected":
					JOptionPane.showMessageDialog(null, "Friend Request Was Rejected!",
				            "Message Dialog", JOptionPane.PLAIN_MESSAGE);
					break;
				}
				}
				break;
			case"R?":
				if(verifyIfUserExistInMyFriendList(parts[1])){
					int index = findIndexOfFriend(parts[1]);
					listOfMyFriends.remove(index);
					fullListOfMyFriends.remove(index);
					modelFriend.removeElement(parts[1]);
				}
				break;
				//cmd is "D", format is "D?/deletedFriendName/myName"
			case"D?":
				if(parts[1].equals(user.getName())){
					int index = findIndexOfFriend(parts[2]);
					listOfMyFriends.remove(index);
					fullListOfMyFriends.remove(index);
					modelFriend.removeElement(parts[2]);
				}
				break;
			case"O?":
				// format "O?/OnlineOrOffline/myName/myPortNo"
				if(parts[1].equals("Offline") && verifyIfUserExistInMyFriendList(parts[2])){
					int index = findIndexOfFriend(parts[2]);
					listOfMyFriends.remove(index);
					modelFriend.removeElement(parts[2]);
				}else if(parts[1].equals("Online") && verifyIfUserExistInMyFriendList(parts[2])){
					User tempUser = new User(parts[2], parts[3]);
					listOfMyFriends.add(tempUser);
					int pos = modelFriend.getSize();
					modelFriend.add(pos, tempUser.getName());
				}
				break;
			case "GC!":
				
				try {
					
						multicastGroup_Group = InetAddress.getByName(parts[2]);
						multicastSocket_Group = new MulticastSocket(wellKnownPort);
						multicastSocket_Group.joinGroup(multicastGroup_Group);
						nameOfChatText.setText(parts[1]);
						
						isInGroupChat = true;
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				break;
			case "IFTG?": //invite friend to group
				
				//"IFTG?/"+friendName+"/"+grpIP+"/"+grpName+"/"+user.getName();
				
				
					
					String grpName = parts[3];
					String grpIP = parts[2];
					String friendName = parts[1];
					String senderName = parts[4];
					
					System.out.println("invited friendname grpIP grpName sender: "+friendName+" "+grpIP+" "+grpName+" "+senderName);
					System.out.println("invited user: "+user.getName());
					
					boolean friendIsInGroup = checkIfFriendHasJoinedGroup(friendName, grpName, grpIP); //check if the selected friend is the selected group
					
					if(friendIsInGroup == true){
						
						String message = "FIG!"+"/"+friendName+"/"+grpIP+"/"+grpName+"/"+senderName; //message to tell sender that selected friend is in selected group
						performSendToMain(message);
						
					}else{
						
						try {
					
						
						
						if(friendName.contains(user.getName())){
							
							System.out.println("friendName: "+friendName);
						
							multicastGroup_Group = InetAddress.getByName(grpIP);
							multicastSocket_Group = new MulticastSocket(wellKnownPort);
							multicastSocket_Group.joinGroup(multicastGroup_Group);
							nameOfChatText.setText(grpName);
							
							user.setCurrentGroupIP(grpIP);
							user.setCurrentGroupName(grpName);
							
							createAGroup_AndAddtoGroupList(grpName, grpIP);
							createAFriend_AndAddtoFriendList(senderName, grpName, grpIP);
							
							int answer = JOptionPane.showConfirmDialog(null, user.getName()+"Do u want to listen to our convo or not?",
						            "Confirm Dialog", JOptionPane.YES_NO_OPTION);
							
							switch (answer){
								case JOptionPane.YES_OPTION:
									checkAcceptOfReject = true;
									break;
								case JOptionPane.NO_OPTION:
									checkAcceptOfReject = false;
									break;
							}
							
							
							isInGroupChat = true;
							
							tglbtnDisconnectconnect.setSelected(isEnabled());
							
							recievingMessages_Thread();
							
							/*if(checkAcceptOfReject == true){
								messageListtextArea.append(msg+"\n");
							}*/
						
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				break;
				
			case "SMF?": //membership of group
				
				//"SMF?/"+friendName+"/"+groupIP+"/"+groupName+"/"+user.getName();
				String SMF_GrpName = parts[3];
				String SMF_GrpIP = parts[2];
				String SMF_FriendName = parts[1];
				String SMF_SenderName = parts[4];
				
				try {
					
					
					
					if(SMF_FriendName.contains(user.getName())){
						
						System.out.println("friendName: "+SMF_FriendName);
					
						multicastGroup_Group = InetAddress.getByName(SMF_GrpIP);
						multicastSocket_Group = new MulticastSocket(wellKnownPort);
						multicastSocket_Group.joinGroup(multicastGroup_Group);
						nameOfChatText.setText(SMF_GrpName);
						
						user.setCurrentGroupIP(SMF_GrpIP);
						user.setCurrentGroupName(SMF_GrpName);
						
						createAGroup_AndAddtoGroupList(SMF_GrpName, SMF_GrpIP);
						//createAFriend_AndAddtoFriendList(SMF_SenderName, SMF_GrpName, SMF_GrpIP);
						
						int answer = JOptionPane.showConfirmDialog(null, user.getName()+"Do u want to listen to our convo or not?",
					            "Confirm Dialog", JOptionPane.YES_NO_OPTION);
						
						switch (answer){
							case JOptionPane.YES_OPTION:
								checkAcceptOfReject = true;
								break;
							case JOptionPane.NO_OPTION:
								checkAcceptOfReject = false;
								break;
						}
						
						
						isInGroupChat = true;
						
						tglbtnDisconnectconnect.setSelected(isEnabled());
						
						recievingMessages_Thread();
						
						/*if(checkAcceptOfReject == true){
							messageListtextArea.append(msg+"\n");
						}*/
					
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				break;
			case "FIG!":
				//"FIG!"+"/"+friendName+"/"+grpIP+"/"+grpName+"/"+senderName;
				
				String frdName = parts[1];
				String gIP = parts[2];
				String group_Name = parts[3];
				String sender_Name = parts[4];
				
				if(user.getName().equals(sender_Name)){
					
					JOptionPane.showMessageDialog(null, "Hi "+sender_Name+", Your friend "+frdName+" has joined the group "+group_Name,
				            "Message Dialog", JOptionPane.PLAIN_MESSAGE);
					
				}
				
				break;
			case "G?": // MSG "G? {{Group name}} {{IP}}"
				
				//"G?/"+groupName+"/"+groupIpAddress+"/"+user.getName()
				
				String userName = parts[3];
				String groupName = parts[1];
				String groupIPAddress = parts[2];
				
				//if(!user.getName().equals(userName)){
				
					boolean duplicateGroupNameOrIPCondition_Host = false;
					String message;
					
					duplicateGroupNameOrIPCondition_Host = checkDuplicateGroupNameOrIP_Host(groupName, groupIPAddress);
					
					System.out.println("duplicate?: "+duplicateGroupNameOrIPCondition_Host);
					
					if(duplicateGroupNameOrIPCondition_Host == true){ //already have duplicate group name and ip
						
						message = "G!"+"/"+"Existed"+"/"+userName+"/"+groupName+"/"+groupIPAddress;
						System.out.println("Sending: "+message);
						
					}else{
						
						message = "G!"+"/"+"NotExisted"+"/"+userName+"/"+groupName+"/"+groupIPAddress;
						System.out.println("Sending: "+message);
						
					}
					
					performSendToMain(message);
					
				//}
				
				
				break;
			case "G!": // MSG "G? {{Group name}} {{IP}}"
				
				
				String uName = parts[2];
				String returnedStatus = parts[1];
				String gName = parts[3];
				String gIPAddress = parts[4];
				
				System.out.println("user.getName(): "+user.getName()+" uName: "+uName+" returnedStatus: "+returnedStatus);
				
				if(user.getName().equals(uName)){ //current app user is equal to 
					
					if(returnedStatus.equals("NotExisted")){
						
						boolean check = checkDuplicateGroupNameOrIP_Host(gName, gIPAddress); //check host arraylist of groups for duplicate
						
						//System.out.println("user.getName(): "+user.getName()+" uName: "+uName+" check: "+check);
						
						if(check == false){
							
							Group newGroup = new Group(gName,gIPAddress);
							
							listOfMyGroups.add(newGroup);
							
							modelGroup.removeAllElements();
							
							for(int i=0; i<listOfMyGroups.size(); i++){
								
								String someString  = listOfMyGroups.get(i).getName();
								
								modelGroup.addElement(someString);
								
							}
							
							
							//dialog box will pop up
							//select multiple friends
							//click add
							doAddMultipleFriendsIntoGroup(newGroup); //membership of group
							
						}
						
					}else{
						
						JOptionPane.showMessageDialog(null, user.getName()+" The group slot might have been taken. Please enter another group name",
					            "Message Dialog", JOptionPane.PLAIN_MESSAGE);
						
					}
					
				}
				
				
				break;
			case "SC?": 
				//"SC!"+"/"+multicastGroup_Group.getHostAddress()+"/"+ msg;
				String IpAddr= parts[1];
				String userMessage= parts[2];
				
				System.out.println("ip and msg: "+multicastGroup_Group.getHostAddress()+" "+userMessage);
				
				try {
					//message = "SC!"+"/"+message+"\n";
					byte[] buf = userMessage.getBytes();
					DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroup_Group, wellKnownPort);
					multicastSocket_Group.send(dgpSend);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				recievingMessages_Thread();
				
				break;
		}
	}
	
	//membership of group
	public void doAddMultipleFriendsIntoGroup(Group newGroup){
	
	//membership of group
	public void doSendToMultipleFriends(Group newGroup, ArrayList<String> listOfSelectedFriends){
	
	public void createAGroup_AndAddtoGroupList(String groupName, String groupIP){
	
	public void createAFriend_AndAddtoFriendList(String friendName, String groupName, String groupIP){
	
	public boolean checkIfFriendHasJoinedGroup(String friendName, String groupName, String groupIP){
	
	public boolean checkDuplicateGroupNameOrIP_Host(String groupName, String groupIPAddress){
		
		//"G?/"+groupName+"/"+groupIpAddress
		
		if(!listOfMyGroups.isEmpty()){
			
			int count = 0;
			
			for(int i=0; i<listOfMyGroups.size(); i++){
				if(listOfMyGroups.get(i).getName().equals(groupName) || listOfMyGroups.get(i).getIP().equals(groupIPAddress)){
					count++;
				}
			}
			
			if(count > 0){ //got duplicate
				return true;
			}else{ //no duplicate
				return false;
			}
		
		}else{
			return false;
		}
	}
	
	public String generateRandomGroupIP()
	{
		Random randomGenerator = new Random();
		
		int first = 235;
		int second = 1;
		int third = randomGenerator.nextInt(255);
		int forth = randomGenerator.nextInt(255);
		return first+"."+second+"."+third+"."+forth;
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
			DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroupMain, wellKnownPort);
			multicastSocketMain.send(dgpSend);
			
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
	
	public void recievingMessages_Thread(){
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				byte buf1[] = new byte[1000];
				DatagramPacket dgpReceived = new DatagramPacket(buf1, buf1.length);
				while (true) {
					try {
						multicastSocket_Group.receive(dgpReceived);
						byte[] receivedData = dgpReceived.getData();
						int length = dgpReceived.getLength();
						// Assumed we received string
						String msg = new String(receivedData, 0, length);
						//debugMsg(msg);
						//mainValidateAction(msg);
						//messageListtextArea.append("thread: "+msg);
						
						if(checkAcceptOfReject == true){
							messageListtextArea.append(msg);
						}
						
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();
		
	}
	
	/**
	 * Create the frame.
	 */
	public AdvancedGroupChatApp() {
		
		modelGroup = new DefaultListModel<>();
		modelFriend = new DefaultListModel<>();
		modelMultipleFriends = new DefaultListModel<>();
		
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
		registerFriendButton.addActionListener(new ActionListener() {
			@SuppressWarnings("null")
			public void actionPerformed(ActionEvent e) {
				//User user = new User(usernameTextField.getText(), "1000");
				user.setName(usernameTextField.getText());
				user.setPort(Integer.toString(myPort));
			}
			
		});
		registerFriendButton.setBounds(189, 7, 89, 23);
		contentPane.add(registerFriendButton);
		
		addFriendButton = new JButton("Add Friend");
		addFriendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg = friendtextField.getText();
				
				if(verifyIfUserExistInMyFriendList(msg)){
					JOptionPane.showMessageDialog(null, "Already added as friend!");
				}
				else if(msg.equals(user.getName())){
					JOptionPane.showMessageDialog(null, "You can't add yourself!");
				}
				else{
					// A/ stands for add friend command "A/friendName/myName/myPort"
					msg = "A!/" + msg + "/" + user.getName() + "/" + user.getPort();
					performSendToMain(msg);
				}
			}
		});
		addFriendButton.setBounds(189, 32, 89, 23);
		contentPane.add(addFriendButton);
		
		createGroupButton = new JButton("Create");
		createGroupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String groupName = grouptextField.getText();
				String groupIpAddress = generateRandomGroupIP();
				
				if(!groupName.isEmpty()){
					String msg = "G?/"+groupName+"/"+groupIpAddress+"/"+user.getName();
					System.out.println("Attempting to create group: "+msg);
					performSendToMain(msg);
				}else{
					JOptionPane.showMessageDialog(null, "Please enter your group name",
				            "Message Dialog", JOptionPane.PLAIN_MESSAGE);
				}
			}
		});
		createGroupButton.setBounds(189, 57, 89, 23);
		contentPane.add(createGroupButton);
		
		deleteFriendButton = new JButton("Delete");
		deleteFriendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(friendList.getSelectedValue() == null){
					JOptionPane.showMessageDialog(null, "Please select a friend from friend list to delete",
				            "Message Dialog", JOptionPane.PLAIN_MESSAGE);
				}else{
					String msg = "D?";
					// to delete from user's name from the friend list of the deleted friend
					// cmd is "D", format is "D/deletedFriendName/myName"
					msg = msg + "/" + friendList.getSelectedValue() + "/" + user.getName();
					performSendToMain(msg);
					int index = findIndexOfFriend(friendList.getSelectedValue());
					listOfMyFriends.remove(index);
					fullListOfMyFriends.remove(index);
					modelFriend.removeElement(friendList.getSelectedValue());
				}
			}
		});
		deleteFriendButton.setBounds(10, 288, 104, 23);
		contentPane.add(deleteFriendButton);
		
		JLabel lblFriendList = new JLabel("Friend List");
		lblFriendList.setBounds(10, 110, 78, 14);
		contentPane.add(lblFriendList);
		
		friendList = new JList<String>(modelFriend);
		friendList.addMouseListener(new MouseAdapter() {
 		    public void mouseClicked(MouseEvent evt) {
 		        //JList list = (JList)evt.getSource();
 		        if (evt.getClickCount() == 2 && friendList.getSelectedValue() != null) {
 		            // Double-click detected
 		            //int index = list.locationToIndex(evt.getPoint());
 		            postMessagetextField.setText("/m "+friendList.getSelectedValue().toString()+" ");
 		        } 
 		    }
 		});
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
		
		JScrollPane scroll_Chat = new JScrollPane(messageListtextArea);
		scroll_Chat.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll_Chat.setBounds(251, 130, 285, 151);
		DefaultCaret caret = (DefaultCaret)messageListtextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		contentPane.add(scroll_Chat);
		
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
				try {
					String msg = user.getName() + ":" +postMessagetextField.getText().toString()+"\n";
					msg = "SC?"+"/"+multicastGroup_Group.getHostAddress()+"/"+ msg;
					
					mainValidateAction(msg);
					
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		sendMessageButton.setBounds(479, 288, 57, 23);
		contentPane.add(sendMessageButton);
		
		tglbtnStatus = new JToggleButton("Status: online/offline");
		tglbtnStatus.setBounds(415, 7, 121, 23);
		contentPane.add(tglbtnStatus);
		tglbtnStatus.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED){
					sendMessageButton.setEnabled(false);
					addFriendButton.setEnabled(false);
					inviteButton.setEnabled(false);
					createGroupButton.setEnabled(false);
					registerFriendButton.setEnabled(false);
					deleteFriendButton.setEnabled(false);
					tglbtnDisconnectconnect.setEnabled(false);
					// format "O/OnlineOrOffline/myName/myPort"
					String msg = "O?";
					msg = msg + "/" + "Offline" + "/" + user.getName() + "/" + user.getPort();
					performSendToMain(msg);
				}else if(e.getStateChange()==ItemEvent.DESELECTED){
					sendMessageButton.setEnabled(true);
					addFriendButton.setEnabled(true);
					inviteButton.setEnabled(true);
					createGroupButton.setEnabled(true);
					registerFriendButton.setEnabled(true);
					deleteFriendButton.setEnabled(true);
					tglbtnDisconnectconnect.setEnabled(true);
					// format "O/OnlineOrOffline/myName/myPort"
					String msg = "O?";
					msg = msg + "/" + "Online" + "/" + user.getName() + "/" +user.getPort();
					performSendToMain(msg);
				}
				
			}
				
		});
		
		inviteButton = new JButton("invite to ");
		inviteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					
					String friendName = friendList.getSelectedValue();
					String groupName = groupList.getSelectedValue();
					// Send joined message
					//String message = user.getName()+" GROUP " + groupName + " joined";
					//performSendToMain(message);
					
					System.out.println("friendName:  "+friendName);
					
					//"IFTG?/"+friendName+"/"+grpIP+"/"+grpName+"/"+user.getName();
					
					String grpIP="", grpName="";
					
					for(int i=0; i<listOfMyGroups.size(); i++){
						if(listOfMyGroups.get(i).getName().equals(groupName)){
							grpName = listOfMyGroups.get(i).getName();
							grpIP = listOfMyGroups.get(i).getIP();
						}
					}
					
					String msg = "IFTG?/"+friendName+"/"+grpIP+"/"+grpName+"/"+user.getName();
					performSendToMain(msg);
					//mainValidateAction(msg);
					
					recievingMessages_Thread();
					
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		inviteButton.setBounds(10, 319, 104, 23);
		contentPane.add(inviteButton);
		
		JLabel currentChatText = new JLabel("Current Chat:");
		currentChatText.setBounds(251, 110, 78, 14);
		contentPane.add(currentChatText);
		
		nameOfChatText = new JLabel("\"name of chat\"");
		nameOfChatText.setBounds(330, 110, 107, 14);
		contentPane.add(nameOfChatText);
		
		tglbtnDisconnectconnect = new JToggleButton("Disconnect/Connect");
		
		tglbtnDisconnectconnect.addItemListener(new ItemListener() {
			   public void itemStateChanged(ItemEvent ev) {
			      if(ev.getStateChange()==ItemEvent.SELECTED){ //when "disconnect/connect" button is depressed
			    	  
			    	  if(isInGroupChat == false){ //if user is not in any group chat
				    	  //join group
				    	  try {
								String groupName = groupList.getSelectedValue();
								// Send joined message
								
								if(groupName != null){
								
									System.out.println("groupName: "+groupName);
								
									String message = user.getName()+" joined GROUP " + groupName + "\n";
									performSendToMain(message);
									
									String selectedGroupIPAddr = "";
									
									for(int i=0; i<listOfMyGroups.size(); i++){
										if(listOfMyGroups.get(i).getName().equals(groupName)){
											selectedGroupIPAddr = listOfMyGroups.get(i).getIP();
										}
									}
									
									String receivedMsg = "GC!/"+groupName+"/"+selectedGroupIPAddr;
									
									mainValidateAction(receivedMsg);
									
									// Send a join message
									byte[] buf = message.getBytes();
									DatagramPacket dgpConnected = new DatagramPacket(buf, buf.length, multicastGroup_Group, wellKnownPort);
									multicastSocket_Group.send(dgpConnected);
									
								}else{
									
									
									
									String receivedMsg = "GC!/"+user.getCurrentGroupName()+"/"+user.getCurrentGroupIP();
									
									mainValidateAction(receivedMsg);
									
									String message = user.getName()+" joined GROUP " + user.getCurrentGroupName() + "\n";
									
									// Send a join message
									byte[] buf = message.getBytes();
									DatagramPacket dgpConnected = new DatagramPacket(buf, buf.length, multicastGroup_Group, wellKnownPort);
									multicastSocket_Group.send(dgpConnected);
									
								}
								
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
									
				    	  recievingMessages_Thread();
				    	  
			    	  }
			    	  
			      } else if(ev.getStateChange()==ItemEvent.DESELECTED){ //when "disconnect/connect" button is lifted
			    	  
			    		  try {
			    			  
			    			  	tglbtnDisconnectconnect.setSelected(false);
			    			  	isInGroupChat = false;
			    			  
								String  msg = user+": is leaving group"+"\n";
								byte[] buf = msg.getBytes();
								DatagramPacket dgpSend = 
										new DatagramPacket(buf, buf.length, multicastGroup_Group, wellKnownPort);
								multicastSocket_Group.send(dgpSend);
								multicastSocket_Group.leaveGroup(multicastGroup_Group);
								
								
								
							}catch (IOException ex){
								ex.printStackTrace();
							}
			    		  
			      }
			   }
			});
		
		tglbtnDisconnectconnect.setBounds(415, 101, 121, 23);
		contentPane.add(tglbtnDisconnectconnect);
		
		// event triggered when closing Jframe
		addWindowListener(new java.awt.event.WindowAdapter() {
		            public void windowClosing(java.awt.event.WindowEvent e) {
		            	// broadcast to everyone who has this username in their friend's list to remove
						String msg = "R?";
						msg = msg + "/" + user.getName();
						performSendToMain(msg);
		                System.exit(0);
		            }
		});
		
		
		
		
		MouseListener mouseListener = new MouseListener() {
			
			@Override
		    public void mouseClicked(MouseEvent e) {
		        
		    }

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.getClickCount() == 1) {
			           String selectedItem = (String) groupList.getSelectedValue();
			           inviteButton.setText("invite to "+selectedItem);
			    }
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		groupList.addMouseListener(mouseListener);
		
		Testing_addFriend();
		//Testing_addGroup();
		
		establishMainConnection();
		establishUnicastCommunication();
		
	}
	
	public void establishUnicastCommunication()
	
	class UnicastSystem extends Thread{
	
	public void sendMessage(String msg){
 		System.out.println("msg is : "+msg);
 		String[] parts = msg.split(" ");//splitting by "/"
 		if(parts[0].equals("/m"))
 		{
 			String receiverUsername = parts[1];
 			System.out.println("comes here first");
 			if (verifyIfUserExistInMyFriendList(receiverUsername))
 			{
 				System.out.println("comes here ");
 				int receiverPort = getUserPort(receiverUsername);
 				String newMsg = msg.replaceAll(parts[0] +" " + parts[1]+ " ", "");//removing command and username to get actual msg
 				sendPrivateMsg(receiverUsername , receiverPort, newMsg);
 				appendTextBox("You whispered "+receiverUsername+":"+ newMsg);
 			}
 			else
 			{
 				appendTextBox("No Such User :"+ receiverUsername);
 			}
		}
 		else
 		{
 			sendMsgToCurrentGroup(msg);
 		}
 		clearSendMsgTextField();
 	}
	
	public void sendPrivateMsg(String username, int port,String msg)
	{
 		byte b[]=msg.getBytes();  
 		try {
 			myUnicastDP=new DatagramPacket(b,b.length,InetAddress.getLocalHost(),port);
 			myUnicastDS.send(myUnicastDP);
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public void sendMsgToCurrentGroup(String msg){//ATTN Leonard
 		System.out.println("sending to group :"+msg);
 		
 	}
 	
 	public void appendTextBox(String msg){
 		messageListtextArea.append(msg+"\n");
 	}
 	public void clearSendMsgTextField()
 	{
 		postMessagetextField.setText("");
 	}
 	
 	public boolean verifyIfUserExistInMyFriendList(String username)
 	{
 		
 		for(int i = 0; i< fullListOfMyFriends.size(); i++){
 			if(fullListOfMyFriends.get(i).getName().equals(username)){
 				return true;
 			}
 		}
 		return false;
 		
 	}
 	
	public int findIndexOfFriend(String username)
 	{
 		
 		for(int i = 0; i< fullListOfMyFriends.size(); i++){
 			if(fullListOfMyFriends.get(i).getName().equals(username)){
 				return i;
 			}
 		}
 		return -1;
 		
 	}
 	
 	public int getUserPort(String username)

 	{
 		for(int i = 0; i< fullListOfMyFriends.size(); i++){
 			if(fullListOfMyFriends.get(i).getName().equals(username)){
 				return Integer.parseInt(fullListOfMyFriends.get(i).getPort());
 			}
 		}
 		return -1;//ATTN Nic
 	}
 	
 	public String getUserID(int port)
 	{
 		for(int i = 0; i< fullListOfMyFriends.size(); i++){
 			if(Integer.parseInt(fullListOfMyFriends.get(i).getPort())== port){
 				return fullListOfMyFriends.get(i).getName();
 			}
 		}
 		return "potato";//ATTN Nic
 	}
	
	public void Testing_addGroup(){
		
		ArrayList<String> friendArray = new ArrayList<String>();
		friendArray.add("friend1");
		friendArray.add("friend2");
		
		ArrayList<String> friendArray2 = new ArrayList<String>();
		friendArray2.add("friend3");
		friendArray2.add("friend4");
		
		ArrayList<String> friendArray3 = new ArrayList<String>();
		friendArray3.add("friend5");
		friendArray3.add("friend6");
		
		Group group = new Group("2107","235.1.2.3");
		Group group2 = new Group("2108","235.1.2.4");
		Group group3 = new Group("2109","235.1.2.5");
		
		listOfMyGroups.add(group);
		listOfMyGroups.add(group2);
		listOfMyGroups.add(group3);
		
		for(int i=0; i<listOfMyGroups.size(); i++){
			
			String someString  = listOfMyGroups.get(i).getName();
			
			modelGroup.addElement(someString);
			
		}
	}
	
	public void Testing_addFriend(){
		
		User user1 = new User("user1","2001");
		User user2 = new User("user2","2002");
		User user3 = new User("user3","2003");
		
		User user4 = new User("user4","2001");
		User user5 = new User("user5","2002");
		User user6 = new User("user6","2003");
		
		listOfMyFriends.add(user1);
		listOfMyFriends.add(user2);
		listOfMyFriends.add(user3);
		
		listOfMyFriends.add(user4);
		listOfMyFriends.add(user5);
		listOfMyFriends.add(user6);
		
		for(int i=0; i<listOfMyFriends.size(); i++){
			
			String someString  = listOfMyFriends.get(i).getName();
			
			modelFriend.addElement(someString);
			
		}
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
	private String CurrentGroupName = "";
	private String CurrentGroupIP = "";
	
	public User(String Name, String Port){
		this.Port = Port;
		this.Name = Name;
	}
	
	public User(){
		
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

	public String getCurrentGroupName() {
		return CurrentGroupName;
	}

	public void setCurrentGroupName(String currentGroupName) {
		CurrentGroupName = currentGroupName;
	}

	public String getCurrentGroupIP() {
		return CurrentGroupIP;
	}

	public void setCurrentGroupIP(String currentGroupIP) {
		CurrentGroupIP = currentGroupIP;
	}

}
