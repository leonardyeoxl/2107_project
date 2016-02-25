import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

public class AdvancedGroupChatApp extends JFrame {

	private JPanel contentPane;
	private JTextField usernameTextField;
	private JTextField friendtextField;
	private JTextField grouptextField;
	private JTextField postMessagetextField;

	private JLabel nameOfChatText; // dynamic

	JButton sendMessageButton, addFriendButton, inviteButton;
	JButton createGroupButton, registerFriendButton, deleteFriendButton;
	JToggleButton tglbtnStatus, tglbtnDisconnectconnect; // dynamic

	JTextArea messageListtextArea;

	private String wellKnownIP = "235.1.1.1";
	private int wellKnownPort = 1199;

	MulticastSocket multicastSocketMain = null;
	InetAddress multicastGroupMain = null;

	InetAddress multicastGroup_Group = null;
	MulticastSocket multicastSocket_Group = null;

	private ArrayList<User> listOfMyFriends = new ArrayList<User>();
	//private ArrayList<String> listOfMyFriendsPort = new ArrayList<String>();
	private ArrayList<User> fullListOfMyFriends = new ArrayList<User>();

	private ArrayList<Group> listOfMyGroups = new ArrayList<Group>();
	//private ArrayList<String> listOfMyGroupsIP = new ArrayList<String>();

	private String myTempUsername = "";
	private int generatePort = 0;
	Timer timer;

	JList<String> friendList, groupList;

	DefaultListModel<String> modelGroup;
	DefaultListModel<String> modelMultipleFriends;
	DefaultListModel<String> modelFriend;

	DatagramSocket myUnicastDS;
	DatagramPacket myUnicastDP;

	boolean checkAcceptOrReject = true; 

	ActionListener actionListener;

	boolean selected = true;

	final User user = new User();
	
	boolean isInGroupChat = false;

	private int userCount = 1;
	
	private int responseCount = 0;
	
	boolean groupNameExisted;
	
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

	public void establishMainConnection() {
		System.out.println("Establishing Main Connection");
		try {
			multicastGroupMain = InetAddress.getByName(wellKnownIP);
			multicastSocketMain = new MulticastSocket(wellKnownPort);
			// join
			multicastSocketMain.joinGroup(multicastGroupMain);
			
			sendMessageButton.setEnabled(false);
			tglbtnDisconnectconnect.setEnabled(false);

			new Thread(new Runnable() {

				@Override
				public void run() {
					byte buf1[] = new byte[1000];
					DatagramPacket dgpReceived = new DatagramPacket(buf1, buf1.length);
					while (true) {
						try {
							multicastSocketMain.receive(dgpReceived);
							byte[] receivedData = dgpReceived.getData();
							int length = dgpReceived.getLength();

							String msg = new String(receivedData, 0, length);
							//debugMsg(msg);

							
							mainValidateAction(msg);

						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}
			}).start();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void debugMsg(String msg)// Purpose is to help you view msg easier by
									// appending it to the chat group/s msg
	{
		messageListtextArea.append("Console Msg " + msg + "\n");
	}

	public void mainValidateAction(String msg) {
		String[] parts = msg.split("/");// splitting by "/"
		switch (parts[0]) {
		case "U?": // MSG "U? {{username}} {{port}}"
			checkDuplicateUserOrPort(parts[1], Integer.valueOf(parts[2]));

			break;
		case "U!": // MSG "U! {{username}} {{port}}"
			// if myusername = username then call the dialog
			String tempUsername = parts[1];
			if (myTempUsername.equals(tempUsername) && Integer.toString(generatePort).equals(parts[2])) {

				if (myTempUsername.equals(tempUsername) && user.getName().equals("")) {
					timer.cancel();
					if (parts[4].equals("1") && parts[3].equals("0"))// port
																		// error
					{
						// generate new port and ask everybody again.
						userValidation(myTempUsername);
					} else {
						// notify user to enter new username
						JOptionPane.showMessageDialog(null, "Username taken!");
						myTempUsername = "";
						configureAllButton(true);
					}
				}
			}
			if (!user.getName().equals("") && parts[1].equals(myTempUsername)) {
				if (parts[4].equals("1")) {
					// need to stop thread that is creating user name and ip
					// after 5 secs
					timer.cancel();
					// generate new port and ask everybody again.
					userValidation(myTempUsername);
				} else if (parts[3].equals("1")) {
					// need to stop thread that is creating user name and ip
					// after 5 secs
					timer.cancel();
					configureAllButton(true);
					JOptionPane.showMessageDialog(null, "Username taken!");
					myTempUsername = "";
				}
			}

			break;
		// for friend requests
		case "A?":
			// A/ stands for add friend command "A?/friendName/myName/myPort"
			if (parts[1].equals(user.getName())) {
				String replyRequest;
				int answer = JOptionPane.showConfirmDialog(null, "Do you want to want accept friend invitation from "+parts[2],
						"Hi "+user.getName(), JOptionPane.YES_NO_OPTION);
				if (answer == 0) {
					replyRequest = "Accepted";
					User tempUser = new User();
					tempUser.setName(parts[2]);
					tempUser.setPort(parts[3]);
					listOfMyFriends.add(tempUser);
					fullListOfMyFriends.add(tempUser);
					int pos = modelFriend.getSize();
					modelFriend.add(pos, tempUser.getName());
				} else {
					replyRequest = "Rejected";
				}
				replyRequest = "A!/" + replyRequest + "/" + user.getName() + "/" + user.getPort() + "/" + parts[2];
				performSendToMain(replyRequest);
			}
			break;
		case "A!":
			// reply format "A!/acceptOrReject/FriendName/userPort/myName"
			if (parts[4].equals(user.getName())) {
				switch (parts[1]) {
				case "Accepted":
					// if accepted, add to list. Not appended straight to text
					// area
					// to prevent cases of friends in the middle quitting
					User tempUser = new User();
					tempUser.setName(parts[2]);
					tempUser.setPort(parts[3]);
					listOfMyFriends.add(tempUser);
					fullListOfMyFriends.add(tempUser);
					int pos = modelFriend.getSize();
					modelFriend.add(pos, tempUser.getName());
					configureAllButton(true);
					break;
				case "Rejected":
					JOptionPane.showMessageDialog(null, "Your friend Request Was Rejected!", "Hi "+user.getName(),
							JOptionPane.PLAIN_MESSAGE);
					configureAllButton(true);
					break;
				}
			}
			break;
		case "R?":
			// format is "R?/userName"
			System.out.println(parts);
			if (verifyIfUserExistInMyFriendList(parts[1])) {
				int index = findIndexOfFriend(parts[1]);
				// different index in online list as sequence might be different
				int onlineListIndex = findIndexOfFriendInOnlineList(parts[1]);
				if (onlineListIndex > -1) {
					listOfMyFriends.remove(onlineListIndex);
				}
				fullListOfMyFriends.remove(index);
				modelFriend.removeElement(parts[1]);
			}
			break;
		// cmd is "D", format is "D?/deletedFriendName/myName"
		case "D?":
			if (parts[1].equals(user.getName())) {
				int index = findIndexOfFriend(parts[2]);
				// different index in online list as sequence might be different
				int onlineListIndex = findIndexOfFriendInOnlineList(parts[2]);
				if (onlineListIndex > -1) {
					listOfMyFriends.remove(onlineListIndex);
				}
				fullListOfMyFriends.remove(index);
				modelFriend.removeElement(parts[2]);
			}
			break;
		case "O?":
			// format "O?/OnlineOrOffline/myName/myPortNo"
			if (parts[1].equals("Offline") && verifyIfUserExistInMyFriendList(parts[2])) {
				int index = findIndexOfFriendInOnlineList(parts[2]);
				if (index > -1) {
					listOfMyFriends.remove(index);
				}
				modelFriend.removeElement(parts[2]);
			} else if (parts[1].equals("Online") && verifyIfUserExistInMyFriendList(parts[2])) {
				User tempUser = new User(parts[2], parts[3]);
				listOfMyFriends.add(tempUser);
				int pos = modelFriend.getSize();
				modelFriend.add(pos, tempUser.getName());
			}
			break;
		case "C?":
			// "C?/myOldName/myNewName/myNewPort"
			if (verifyIfUserExistInMyFriendList(parts[1])) {
				int index = findIndexOfFriend(parts[1]);
				// different index in online list as sequence might be different
				int onlineListIndex = findIndexOfFriendInOnlineList(parts[1]);
				User tempUser = new User(parts[2], parts[3]);
				if (onlineListIndex > -1) {
					listOfMyFriends.set(onlineListIndex, tempUser);
				}
				fullListOfMyFriends.set(index, tempUser);
				// remove old name from friend list and add new name
				modelFriend.removeElement(parts[1]);
				modelFriend.addElement(parts[2]);
			}
			break;
		case "GC!":
			
			String GC_GroupIP = parts[2];
			String GC_GroupName = parts[1];
			
			try {
				
					multicastGroup_Group = InetAddress.getByName(GC_GroupIP);
					multicastSocket_Group = new MulticastSocket(wellKnownPort);
					multicastSocket_Group.joinGroup(multicastGroup_Group);
					nameOfChatText.setText(GC_GroupName);
					
					isInGroupChat = true;
					
					tglbtnDisconnectconnect.setSelected(isEnabled());
					
					user.setCurrentGroupIP(GC_GroupIP);
					user.setCurrentGroupName(GC_GroupName);
				
				
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
							//createAFriend_AndAddtoFriendList(senderName, grpName, grpIP);
							
							confirmationToListenToGroupChat();
							
							isInGroupChat = true;
							
							tglbtnDisconnectconnect.setSelected(isEnabled());
							
						}
						
						recievingMessages_Thread();
					
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
			String SMF_SenderName = parts[4]; //remove as not needed
			
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
					
					confirmationToListenToGroupChat();
					
					isInGroupChat = true;
					
				}
				
				recievingMessages_Thread();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			break;
		case "FIG!":
			//"FIG!"+"/"+friendName+"/"+grpIP+"/"+grpName+"/"+senderName;
			
			String frdName = parts[1];
			String gIP = parts[2]; //remove as not needed
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
					responseCount++;
					boolean check = checkDuplicateGroupNameOrIP_Host(gName, gIPAddress); //check host arraylist of groups for duplicate
					
					if((check == false) && (responseCount == userCount)){
						Group newGroup = new Group(gName,gIPAddress);
						
						listOfMyGroups.add(newGroup);
						
						modelGroup.removeAllElements();
						
						for(int i=0; i<listOfMyGroups.size(); i++){
							
							String someString  = listOfMyGroups.get(i).getName();
							
							modelGroup.addElement(someString);
							
						}
						
						inviteButton.setEnabled(true);
						
						
						//dialog box will pop up
						//select multiple friends
						//click add
						responseCount = 0;
						doAddMultipleFriendsIntoGroup(newGroup); //membership of group
						
						hostUserJoinGroupAfterCreateGroup(gName,gIPAddress);
						
					}
					
				}else{
					if(!groupNameExisted){
						JOptionPane.showMessageDialog(null, user.getName()+" The group slot might have been taken. Please enter another group name",
								"Message Dialog", JOptionPane.PLAIN_MESSAGE);
						groupNameExisted = true;
					}
				}
				
			}
			
			
			break;
		case "SC?": 
			//"SC!"+"/"+multicastGroup_Group.getHostAddress()+"/"+ msg;
			String IpAddr= parts[1]; //remove as not needed
			String userMessage= parts[2];
			
			System.out.println("ip and msg: "+multicastGroup_Group.getHostAddress()+" "+userMessage);
			
			sendMessageInGroup(userMessage);
			
			recievingMessages_Thread();
			
			break;
		// for incrementing counter. This is sent everytime a new user is registered
			// format will be S?/processID
		case "S?":
			if(!ManagementFactory.getRuntimeMXBean().getName().equals(parts[1]) && (!user.getName().equals(""))){
				userCount++;
				System.out.println("incrementing Counter");
				String syncCounter = "S!/" + "sync" + "/" + userCount + "/" + user.getName();
				performSendToMain(syncCounter);
			}
			break;
		//format will be "S!/sync/userCount/senderName"
		case "S!":
			System.out.println("----------" + user.getName() + "-------------" + parts[1]);
			if(!user.getName().equals("")){
				switch(parts[1]){
				case "sync":
					if((userCount < Integer.parseInt(parts[2])) && (!user.getName().equals(parts[3]))){
						userCount = Integer.parseInt(parts[2]);
						System.out.println(user.getName() + " current user count: " + userCount);
					}
					break;
				case "reduce":
					userCount--;
					System.out.println(user.getName() + " reduce current user count: " + userCount);
					break;
				}
				
			}
			break;
		}
	}
	
	public void confirmationToListenToGroupChat(){
		int answer = JOptionPane.showConfirmDialog(null, user.getName()+", do you want to listen to our chat?",
	            "Confirm Dialog", JOptionPane.YES_NO_OPTION);
		
		switch (answer){
			case JOptionPane.YES_OPTION:
				checkAcceptOrReject = true; //user wants to hear convo in the group
				tglbtnDisconnectconnect.setSelected(true); //toggle button "pop in"
				break;
			case JOptionPane.NO_OPTION:
				checkAcceptOrReject = false; //user does not want to hear convo in the group
				tglbtnDisconnectconnect.setSelected(false); //toggle button "pop out"
				break;
		}
	}
	
	public void hostUserJoinGroupAfterCreateGroup(String gName, String gIPAddress){
		String receivedMsg = "GC!/"+gName+"/"+gIPAddress;
		mainValidateAction(receivedMsg);
		
		String message = user.getName()+" joined GROUP " + gName + "\n";
		
		sendMessageInGroup(message);
	}
	
	//membership of group
	public void doAddMultipleFriendsIntoGroup(Group newGroup){
		
		//modelMultipleFriends.removeAllElements();
		
		for(int i=0; i<listOfMyFriends.size(); i++){
			modelMultipleFriends.addElement(listOfMyFriends.get(i).getName());
		}
		
		JList list = new JList(modelMultipleFriends);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		JOptionPane.showMessageDialog(
		  null, list, "Select friends to chat in "+newGroup.getName(), JOptionPane.PLAIN_MESSAGE);
		System.out.println(Arrays.toString(list.getSelectedIndices()));
		
		int[] selectedFriendsArray = list.getSelectedIndices();
		
		ArrayList<String> listOfSelectedFriends = new ArrayList<String>();
		
		for(int i=0; i<selectedFriendsArray.length; i++){
			listOfSelectedFriends.add(modelMultipleFriends.getElementAt(i));
		}
		
		System.out.println(Arrays.toString(listOfSelectedFriends.toArray()));
		
		modelMultipleFriends.clear();
		doSendToMultipleFriends(newGroup, listOfSelectedFriends);
		
	}
	
	//membership of group
		public void doSendToMultipleFriends(Group newGroup, ArrayList<String> listOfSelectedFriends){
			
			try {
				
				String groupName = newGroup.getName();
				String groupIP = newGroup.getIP();
				
				for(int i=0; i<listOfSelectedFriends.size(); i++){
					
					String friendName = listOfSelectedFriends.get(i);
					
					String msg = "SMF?/"+friendName+"/"+groupIP+"/"+groupName+"/"+user.getName();
					performSendToMain(msg);
					
				}
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
	}
		
	// Send a join message
	public void sendMessageInGroup(String message){
		
		try {
			byte[] buf = message.getBytes();
			DatagramPacket dgpConnected = new DatagramPacket(buf, buf.length, multicastGroup_Group, wellKnownPort);
			multicastSocket_Group.send(dgpConnected);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void createAGroup_AndAddtoGroupList(String groupName, String groupIP){
		
		Group newGroup = new Group(groupName,groupIP);
		
		listOfMyGroups.add(newGroup);
		
		modelGroup.removeAllElements();
		
		for(int i=0; i<listOfMyGroups.size(); i++){
			
			String someString  = listOfMyGroups.get(i).getName();
			
			modelGroup.addElement(someString);
			
		}
		
	}
	
	public void createAFriend_AndAddtoFriendList(String friendName, String groupName, String groupIP){
		
		User newUser = new User();
		newUser.setName(friendName);
		newUser.setCurrentGroupName(groupName);
		newUser.setCurrentGroupIP(groupIP);
		
		listOfMyFriends.add(newUser);
		
		modelFriend.removeAllElements();
		
		for(int i=0; i<listOfMyFriends.size(); i++){
			
			String someString  = listOfMyFriends.get(i).getName();
			
			modelFriend.addElement(someString);
			
		}
		
	}
	
	public boolean checkIfFriendHasJoinedGroup(String friendName, String groupName, String groupIP){
		
		System.out.println("friendName, groupName, groupIP: "+friendName+" "+groupName+" "+groupIP);
		System.out.println("user.getName(), user.getCurrentGroupName(), user.getCurrentGroupIP(): "+user.getName()+" "+user.getCurrentGroupName()+" "+user.getCurrentGroupIP());
		
		if(user.getName().equals(friendName) && user.getCurrentGroupName().equals("") && user.getCurrentGroupIP().equals("")){
			return false;
		}else if(user.getName().equals(friendName) && user.getCurrentGroupName().equals(groupName) && user.getCurrentGroupIP().equals(groupIP)){
			return true;
		}
		
		return false;
		
	}
	
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
	
	public void createUsername(String username, int port) {

		// if user changes name, need to update his friend's friend list and
		// close existing unicast channel
		// to create new channel with new port number
		if (!user.getName().equals("")) {
			myUnicastDS.close();

			// format for change of username would be
			// "C?/myOldName/myNewName/myNewPort"
			String msg = "C?";
			msg = msg + "/" + user.getName() + "/" + username + "/" + port;
			performSendToMain(msg);
		}
		user.setName(username);
		user.setPort(Integer.toString(port));
		myTempUsername = "";
		messageListtextArea.append(username + " has joined the chat \n");
		String welcomeMsg = "S?/" + ManagementFactory.getRuntimeMXBean().getName();
		System.out.println("my process id" + ManagementFactory.getRuntimeMXBean().getName());
		performSendToMain(welcomeMsg);
		configureAllButton(true);
		establishUnicastCommunication();
		JOptionPane.showMessageDialog(null, "You have been registered!");
		
		user.setCurrentGroupIP(wellKnownIP);
		user.setCurrentGroupName("Main");
		
		sendMessageButton.setEnabled(true);
		tglbtnDisconnectconnect.setEnabled(true);
		
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
	
	public void userValidation(String username) {
		// user.setName("");
		Random r = new Random();
		generatePort = r.nextInt(60000 - 2000) + 2000;

		myTempUsername = username;
		if (username.equals("")) {
			JOptionPane.showMessageDialog(null, "Please enter a username!");
			configureAllButton(true);
		} else {
			if (user.getName().equals(username)) {
				JOptionPane.showMessageDialog(null, "You are already registered as " + user.getName() + "!");
				configureAllButton(true);
			} else {

				String checkUsername = "U?/" + username + "/" + String.valueOf(generatePort);
				performSendToMain(checkUsername);
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						createUsername(myTempUsername, generatePort);
					}
				}, 3000);
			}
		}

	}

	public void checkDuplicateUserOrPort(String username, int port) {

		if (myTempUsername.equals(username) && generatePort == port) {
			return;
		} else {
			int usernameFail = 0;
			int portFail = 0;
			if (user.getName().equals(username) || myTempUsername.equals(username)) {
				usernameFail = 1;

			}
			if (Integer.parseInt(user.getPort()) == port) {
				portFail = 1;
			}
			if ((usernameFail + portFail) > 0)// there is a duplicate
			{
				String msg = "U!/" + username + "/" + port + "/" + Integer.toString(usernameFail) + "/"
						+ Integer.toString(portFail);
				performSendToMain(msg);
			}
		}
	}

	public void performSendToMain(String msg) {
		try {
			byte[] buf = msg.getBytes();
			DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroupMain, wellKnownPort);
			multicastSocketMain.send(dgpSend);
			;

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public String generateRandomIP() {
		Random randomGenerator = new Random();

		int first = 239;
		int second = randomGenerator.nextInt(255);
		int third = randomGenerator.nextInt(255);
		int forth = randomGenerator.nextInt(255);
		return first + "." + second + "." + third + "." + forth;
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
						
						if(checkAcceptOrReject == true){ //user wants to hear convo in the group
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

		user.setName("");
		user.setPort("0");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 562, 389);
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
				// Read the string command from the user
				configureAllButton(false);
				userValidation((usernameTextField.getText().toString()).trim());
			}

		});

		registerFriendButton.setBounds(189, 7, 89, 23);
		contentPane.add(registerFriendButton);

		addFriendButton = new JButton("Add Friend");
		addFriendButton.setIcon(new ImageIcon(AdvancedGroupChatApp.class.getResource("/images/add-friend.png")));
		addFriendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg = friendtextField.getText().trim();
				configureAllButton(false);
				if (verifyIfUserExistInMyFriendList(msg)) {
					JOptionPane.showMessageDialog(null, "Already added as friend!");
					configureAllButton(true);
				} else if (msg.equals(user.getName())) {
					JOptionPane.showMessageDialog(null, "You can't add yourself!");
					configureAllButton(true);
				} else {
					// A/ stands for add friend command
					// "A/friendName/myName/myPort"
					msg = "A?/" + msg + "/" + user.getName() + "/" + user.getPort();
					performSendToMain(msg);
					Timer buttonTimer = new Timer();
					buttonTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							configureAllButton(true);
						}
					}, 3000);
				}
				
			}
		});
		addFriendButton.setBounds(189, 32, 120, 23);
		contentPane.add(addFriendButton);

		createGroupButton = new JButton("Create");
		createGroupButton.setIcon(new ImageIcon(AdvancedGroupChatApp.class.getResource("/images/group-of-people.png")));
		createGroupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String groupName = grouptextField.getText().trim();
				String groupIpAddress = generateRandomGroupIP();
				groupNameExisted = false;
				if(!groupName.isEmpty()){
					String msg = "G?/"+groupName+"/"+groupIpAddress+"/"+user.getName();
					System.out.println("Attempting to create group: "+msg);
					performSendToMain(msg);
				}else{
					JOptionPane.showMessageDialog(null, "Please enter your group name",
				            "Hi "+user.getName(), JOptionPane.PLAIN_MESSAGE);
				}
			}
		});
		createGroupButton.setBounds(189, 57, 120, 23);
		contentPane.add(createGroupButton);

		deleteFriendButton = new JButton("Delete");
		deleteFriendButton.setIcon(new ImageIcon(AdvancedGroupChatApp.class.getResource("/images/delete-user.png")));
		deleteFriendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (friendList.getSelectedValue() == null) {
					JOptionPane.showMessageDialog(null, "Please select a friend from friend list to delete",
							"Message Dialog", JOptionPane.PLAIN_MESSAGE);
				} else {
					String msg = "D?";
					// to delete from user's name from the friend list of the
					// deleted friend
					// cmd is "D", format is "D/deletedFriendName/myName"
					msg = msg + "/" + friendList.getSelectedValue() + "/" + user.getName();
					performSendToMain(msg);
					int index = findIndexOfFriend(friendList.getSelectedValue());
					int onlineListIndex = findIndexOfFriendInOnlineList(friendList.getSelectedValue());
					if (onlineListIndex > -1) {
						listOfMyFriends.remove(onlineListIndex);
					}
					System.out.println(friendList.getSelectedValue());
					System.out.println("index of friend is" + onlineListIndex);
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
		JScrollPane friendListScr = new JScrollPane(friendList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		friendListScr.setBounds(10, 130, 104, 151);
		contentPane.add(friendListScr);
		// friendList.setBounds(10, 130, 104, 151);
		// contentPane.add(friendList);
		friendList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				JList list = (JList) evt.getSource(); //remove as not needed
				if (evt.getClickCount() == 2 && friendList.getSelectedValue() != null) {
					// Double-click detected
					int index = list.locationToIndex(evt.getPoint()); //remove as not needed
					postMessagetextField.setText("/m " + friendList.getSelectedValue().toString() + " ");
				}
			}
		});

		JLabel lblGroupList = new JLabel("Group List");
		lblGroupList.setBounds(129, 110, 70, 14);
		contentPane.add(lblGroupList);

		groupList = new JList<String>(modelGroup);
		JScrollPane groupListScr = new JScrollPane(groupList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		groupListScr.setBounds(124, 130, 117, 151);
		contentPane.add(groupListScr);
		// groupList.setBounds(124, 130, 117, 151);
		// contentPane.add(groupList);

		messageListtextArea = new JTextArea();
		//JScrollPane scr = new JScrollPane(messageListtextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		//		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		messageListtextArea.setBounds(251, 130, 285, 151);
		contentPane.add(messageListtextArea);
		
		JScrollPane scroll_Chat = new JScrollPane(messageListtextArea);
		scroll_Chat.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
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
		sendMessageButton.setIcon(new ImageIcon(AdvancedGroupChatApp.class.getResource("/images/send-button.png")));
		sendMessageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg = postMessagetextField.getText();
				msg = ManagementFactory.getRuntimeMXBean().getName() + ": " + msg;
				sendMessage(postMessagetextField.getText());
			}
		});
		sendMessageButton.setBounds(439, 288, 95, 23);
		contentPane.add(sendMessageButton);

		tglbtnStatus = new JToggleButton("Status: online/offline");
		tglbtnStatus.setBounds(415, 7, 121, 23);
		contentPane.add(tglbtnStatus);
		tglbtnStatus.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					tglbtnStatus.setText("Offline");
					tglbtnStatus.setIcon(new ImageIcon(AdvancedGroupChatApp.class.getResource("/images/offline.png")));
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
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					tglbtnStatus.setText("Online");
					tglbtnStatus.setIcon(new ImageIcon(AdvancedGroupChatApp.class.getResource("/images/online.png")));
					sendMessageButton.setEnabled(true);
					addFriendButton.setEnabled(true);
					inviteButton.setEnabled(true);
					createGroupButton.setEnabled(true);
					registerFriendButton.setEnabled(true);
					deleteFriendButton.setEnabled(true);
					tglbtnDisconnectconnect.setEnabled(true);
					// format "O/OnlineOrOffline/myName/myPort"
					String msg = "O?";
					msg = msg + "/" + "Online" + "/" + user.getName() + "/" + user.getPort();
					performSendToMain(msg);
				}

			}

		});

		inviteButton = new JButton("Invite");
		inviteButton.setIcon(new ImageIcon(AdvancedGroupChatApp.class.getResource("/images/add-user.png")));
		inviteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(modelFriend.getSize() > 0 && modelGroup.getSize() > 0){
				
					try {
						
						String friendName = friendList.getSelectedValue();
						String groupName = groupList.getSelectedValue();
						// Send joined message
						//String message = user.getName()+" GROUP " + groupName + " joined";
						//performSendToMain(message);
						
						if(friendName != null && groupName != null){
						
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
						
						}else if(friendName == null){
							
							JOptionPane.showMessageDialog(null, "Please select a friend from the Friend List",
									"Message Dialog", JOptionPane.PLAIN_MESSAGE);
							
						}else if(groupName == null){
							
							JOptionPane.showMessageDialog(null, "Please select a group from the Group List",
									"Message Dialog", JOptionPane.PLAIN_MESSAGE);
							
						}else{
							
							JOptionPane.showMessageDialog(null, "Please select a friend and a group from the Friend and Group List",
									"Message Dialog", JOptionPane.PLAIN_MESSAGE);
							
						}
						
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}else if(modelFriend.getSize() == 0){
					
					JOptionPane.showMessageDialog(null, "Please add friends before inviting anyone",
							"Message Dialog", JOptionPane.PLAIN_MESSAGE);
					
				}else if(modelGroup.getSize() == 0){
					
					JOptionPane.showMessageDialog(null, "Please add groups before inviting anyone",
							"Message Dialog", JOptionPane.PLAIN_MESSAGE);
					
				}else{
					
					JOptionPane.showMessageDialog(null, "Please add friends and groups before inviting anyone",
							"Message Dialog", JOptionPane.PLAIN_MESSAGE);
					
				}
				
			}
		});
		inviteButton.setBounds(10, 315, 104, 23);
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
			    	  
			    	  tglbtnDisconnectconnect.setText("Connect");
			    	  tglbtnDisconnectconnect.setIcon(new ImageIcon(AdvancedGroupChatApp.class.getResource("/images/connected.png")));
			    	  
			    	  checkAcceptOrReject = true; //user wants to hear convo in the group
			    	  
			    	  if(isInGroupChat == false){ //if user is not in any group chat
				    	  
				    	  try {
				    		  
								String groupName = groupList.getSelectedValue();
								
								sendMessageButton.setEnabled(true);
								
								if(groupName != null){
								
									System.out.println("groupName: "+groupName);
								
									String message = user.getName()+" joined GROUP " + groupName + "\n";
									performSendToMain(message); // Send joined message
									
									String selectedGroupIPAddr = "";
									
									for(int i=0; i<listOfMyGroups.size(); i++){
										if(listOfMyGroups.get(i).getName().equals(groupName)){
											selectedGroupIPAddr = listOfMyGroups.get(i).getIP();
										}
									}
									
									String receivedMsg = "GC!/"+groupName+"/"+selectedGroupIPAddr;
									
									mainValidateAction(receivedMsg); //join group
									
									// Send a join message
									sendMessageInGroup(message);
									
								}else{
									
									String receivedMsg = "GC!/"+user.getCurrentGroupName()+"/"+user.getCurrentGroupIP();
									
									mainValidateAction(receivedMsg);
									
									String message = user.getName()+" joined GROUP " + user.getCurrentGroupName() + "\n";
									
									// Send a join message
									sendMessageInGroup(message);
									
								}
								
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
									
				    	  recievingMessages_Thread();
				    	  
			    	  }
			    	  
			      } else if(ev.getStateChange()==ItemEvent.DESELECTED){ //when "disconnect/connect" button is lifted
			    	  
			    	  tglbtnDisconnectconnect.setText("Disconnect");
			    	  tglbtnDisconnectconnect.setIcon(new ImageIcon(AdvancedGroupChatApp.class.getResource("/images/disconnected.png")));
			    	  
			    		  try {
			    			  
			    			  	tglbtnDisconnectconnect.setSelected(false);
			    			  	isInGroupChat = false;
			    			  	sendMessageButton.setEnabled(false);
			    			  
								String  msg = user.getName()+": is leaving group "+user.getCurrentGroupName()+"\n";
								sendMessageInGroup(msg);
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
				// broadcast to everyone who has this username in their friend's
				// list to remove
				// format is "R?/userName"
				if (!user.getName().equals("")) {
					String msg = "R?";
					msg = msg + "/" + user.getName();
					performSendToMain(msg);
					String partingMsg = "S!/" + "reduce";
					performSendToMain(partingMsg);
				}
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
				if ((e.getClickCount()) == 1 && (groupList.getSelectedValue() != null)) {
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
		
		establishMainConnection();
	}

	public void establishUnicastCommunication() {
		System.out.println("Establishing Unicast Connection");
		try {
			myUnicastDS = new DatagramSocket(Integer.parseInt(user.getPort()));
		} catch (Exception e) {
			System.out.println(e);
		}
		new UnicastSystem();

	}

	class UnicastSystem extends Thread {
		UnicastSystem() {
			start();
		}

		public void run() {
			while (true) {
				try {

					byte b[] = new byte[100];
					DatagramPacket myRecivedDP = new DatagramPacket(b, b.length);

					myUnicastDS.receive(myRecivedDP);
					System.out.println(
							"RECEIVED UNICAST :" + new String(myRecivedDP.getData(), 0, myRecivedDP.getLength()));
					String senderID = getUserID(myRecivedDP.getPort());
					appendTextBox(
							senderID + " Whispers : " + new String(myRecivedDP.getData(), 0, myRecivedDP.getLength()));

				} catch (Exception e) {

				}
			}

		}
	}

	public void sendMessage(String msg) {
		String[] parts = msg.split(" ");// splitting by "/"
		if (parts[0].equals("/m")) {
			String receiverUsername = parts[1];
			if (verifyIfUserExistInMyFriendList(receiverUsername)) {
				int receiverPort = getUserPort(receiverUsername);

				// removing command and username to get actual msg
				String newMsg = msg.replaceAll(parts[0] + " " + parts[1] + " ", "");
				sendPrivateMsg(receiverUsername, receiverPort, newMsg);
				appendTextBox("You whispered " + receiverUsername + ":" + newMsg);
			} else {
				appendTextBox("No Such User :" + receiverUsername);
			}
		} else {
			sendMsgToCurrentGroup(msg);
		}
		clearSendMsgTextField();
	}

	public void sendPrivateMsg(String username, int port, String msg) {
		byte b[] = msg.getBytes();
		try {
			myUnicastDP = new DatagramPacket(b, b.length, InetAddress.getLocalHost(), port);
			myUnicastDS.send(myUnicastDP);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendMsgToCurrentGroup(String msg) {// ATTN Leonard
		try {
			
			msg = user.getName() + ":" +postMessagetextField.getText().toString()+"\n";
			
			if(user.getCurrentGroupIP().equals(wellKnownIP)){
				
				performSendToMain(msg);
				
			}else{
			
				msg = "SC?"+"/"+multicastGroup_Group.getHostAddress()+"/"+ msg;
				mainValidateAction(msg);
				
			}
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void appendTextBox(String msg) {
		messageListtextArea.append(msg + "\n");
	}

	public void clearSendMsgTextField() {
		postMessagetextField.setText("");
	}

	public boolean verifyIfUserExistInMyFriendList(String username) {

		for (int i = 0; i < fullListOfMyFriends.size(); i++) {
			if (fullListOfMyFriends.get(i).getName().equals(username)) {
				return true;
			}
		}
		return false;

	}

	public void configureAllButton(boolean visibility) {
		sendMessageButton.setEnabled(visibility);
		addFriendButton.setEnabled(visibility);
		inviteButton.setEnabled(visibility);
		createGroupButton.setEnabled(visibility);
		registerFriendButton.setEnabled(visibility);
		deleteFriendButton.setEnabled(visibility);
		tglbtnDisconnectconnect.setEnabled(visibility);
		tglbtnStatus.setEnabled(visibility);
	}

	public int findIndexOfFriend(String username) {

		for (int i = 0; i < fullListOfMyFriends.size(); i++) {
			if (fullListOfMyFriends.get(i).getName().equals(username)) {
				return i;
			}
		}
		return -1;

	}

	public int findIndexOfFriendInOnlineList(String username) {

		for (int i = 0; i < listOfMyFriends.size(); i++) {
			if (listOfMyFriends.get(i).getName().equals(username)) {
				return i;
			}
		}
		return -1;

	}

	public int getUserPort(String username) {
		for (int i = 0; i < fullListOfMyFriends.size(); i++) {
			if (fullListOfMyFriends.get(i).getName().equals(username)) {
				return Integer.parseInt(fullListOfMyFriends.get(i).getPort());
			}
		}
		return -1;// ATTN Nic
	}

	public String getUserID(int port) {
		for (int i = 0; i < fullListOfMyFriends.size(); i++) {
			if (Integer.parseInt(fullListOfMyFriends.get(i).getPort()) == port) {
				return fullListOfMyFriends.get(i).getName();
			}
		}
		return "potato";// ATTN Nic
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
