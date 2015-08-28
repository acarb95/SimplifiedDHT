package cs455.overlay.node;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPConnectionsCache;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.InteractiveCommandParser;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.NodeReportsOverlaySetupStatus;
import cs455.overlay.wireformats.OverlayNodeReportsTaskFinished;
import cs455.overlay.wireformats.OverlayNodeReportsTrafficSummary;
import cs455.overlay.wireformats.OverlayNodeSendsData;
import cs455.overlay.wireformats.OverlayNodeSendsDeregistration;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.RegistryReportsDeregistrationStatus;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;
import cs455.overlay.wireformats.RegistryRequestsTaskInitiate;
import cs455.overlay.wireformats.RegistrySendsNodeManifest;

/***
 * This represents the client node of the overlay. It implements the 
 * onEvent method from Node. When called it must be given the hostname of the 
 * server and the port the server resides on.
 * 
 * Immediately after it is called it will attempt to create a connection with the
 * server and register itself in the overlay.
 * 
 * Usage:
 * 
 * java cs455.overlay.node.MessagingNode <server_host> <server_port>
 * 
 * Server_host is the hostname where the server resides
 * Server_port is the port in which the registry's server thread is listening
 * 
 * Messaging nodes must be started AFTER the registry
 * 
 * Date: 1/22/15
 * @author acarbona
 *
 */

public class MessagingNode extends Thread implements Node {

	// All class variables are volatile to ensure that when multiple threads are accessing
	// the variables they will be pulling the correct value.
	private int sendTracker, receiveTracker, relayTracker = 0;
	private long sendSummation, receiveSummation = 0;
	private TCPConnection serverConnection;
	private Socket socket;
	private int id;
	private RoutingTable table;
	private TCPServerThread listener; // Needed to listen for nodes trying to connection to it in P2P fashion
	private volatile TCPConnectionsCache nodeConnections;
	private volatile ArrayList<Integer> allNodes;
	private int messagesToSend;
	private Queue<OverlayNodeSendsData> relayQueue = new LinkedList<OverlayNodeSendsData>();
	
	/***
	 * The constructor for the messaging node provides all the setup requirements.
	 * It creates and initializes the necessary connections and threads for the node
	 * to become a part of the overlay.
	 * @param serverHost - The host name of the server
	 * @param serverPort - The port in which the server is listening
	 */
	public MessagingNode(String serverHost, int serverPort) {
		try {
			// Initial setup of the node
			
			// Create a socket and connection with the server host
			socket = new Socket(serverHost, serverPort);
			serverConnection = new TCPConnection(socket, this);
			
			// Start receiving data from the server
			serverConnection.readData();
			
			// Create a server thread that will listen for other messaging nodes
			// to create connections for the overlay
			listener = new TCPServerThread(0, this);
			listener.start();
			
			// Create a connections cache that will keep track of all the
			// connecting nodes for the overlay
			nodeConnections = new TCPConnectionsCache();
						
			// Send the registration request. It sends the server the port of it's server thread because
			// that port number will be distributed to all the nodes attempting to connect to it for the overlay
//			synchronized (this) {
				OverlayNodeSendsRegistration event = new OverlayNodeSendsRegistration(Protocol.OVERLAY_NODE_SENDS_REGISTRATION, socket.getLocalAddress().getAddress(), listener.getPort());
				serverConnection.sendData(event.getBytes());
//			}
		} catch (IOException e) {
			System.out.println("Error in creating messaging node: " + e.getMessage());
			e.printStackTrace(); 
		}
	}
	
	@Override
	public void onEvent(Event e) {
		// Switch on the type of event
		switch(e.getType()) {
			case Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS:
				// Use the wireformat to parse out the event
				RegistryReportsRegistrationStatus event = (RegistryReportsRegistrationStatus) e;
				
				// Grab the information from the event
				id = event.getID();
				String info = event.getMessage();
				
				// Check to see the registration status
				if (id == -1) {
					System.out.println("Registration failed!");
					System.out.println("Reason: " + info);
				} else {
					System.out.println(info);
				}
				break;
			case Protocol.REGISTRY_SENDS_NODE_MANIFEST:
//				System.out.println("Node: " + id + ". Got manifest!");
				// Create a new routing table
				table = new RoutingTable(id);
				
				try {
					
					// Have the wireformat parse out the event
					RegistrySendsNodeManifest nodeManifest = (RegistrySendsNodeManifest) e;
					
					// Grab the routing table information from the event
					int size = nodeManifest.getSize();
					ArrayList<Integer> ids = nodeManifest.getIDs();
					ArrayList<byte[]> ips = nodeManifest.getIPs();
					ArrayList<Integer> ports = nodeManifest.getPorts();
	
					// Iterate through arraylists and create routing table connections
					for (int i = 0; i < size; i++) {
						int hopID = ids.get(i);
						byte[] hopIP = ips.get(i);
						int hopPort = ports.get(i);
						
						Socket newSocket = new Socket(InetAddress.getByAddress(hopIP), hopPort);
						
						TCPConnection connect = new TCPConnection(newSocket, this);
//						System.out.println("Node (" + id+ ") connecting to " + hopID);
						
						// Add the connections to the connection cache and add the entry to the routing table
						nodeConnections.add(hopID, connect);
						table.add(hopID, (int) Math.pow(2, i));
					}
					
					// Get number of nodes
	
					allNodes = new ArrayList<Integer>();
					
					// get nodes
					int[] nodes = nodeManifest.getNodes();
					for (int i = 0; i < nodes.length; i++) {
						allNodes.add(nodes[i]);
					}
					
					// Populate the node list in the routing table (used for routing calculations)
					table.populateNodeList(allNodes);
					
					// Send overlay status message
					int success = id;
					
					// Send the overlay status to the registry
					NodeReportsOverlaySetupStatus setupStatus = new NodeReportsOverlaySetupStatus(Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS, success, "Setup successful");
					serverConnection.sendData(setupStatus.getBytes());
				} catch (IOException e1) {
					System.out.println("Error in recieving node manifest" + e1.getMessage());
					e1.printStackTrace();
				}
				
				break;
			case Protocol.REGISTRY_REQUESTS_TASK_INITIATE:
//				System.out.println(id + ": Got task initiate message!");
				try {
					
					// Parse the task initiate message using the wireformat
					RegistryRequestsTaskInitiate taskInitiate = (RegistryRequestsTaskInitiate) e;
					
					// This must be synchonized because multiple threads could be attempting to 
					// update this variable, thus creating a race condition
					messagesToSend = taskInitiate.getNumPackets();
					
					Random generator = new Random();

					// Create n messages
					for (int i = 0; i < messagesToSend; i++) {
						// Generate a random node to send the message to
						int randIndex = generator.nextInt(allNodes.size());
						while (allNodes.get(randIndex) == id) {
							randIndex = generator.nextInt(allNodes.size());
						}
						
						// Get the destination id of the random node
						int destID = allNodes.get(randIndex);
						
						// Use the routing table to determine the next node to send it to
						int sendTo = table.getDest(destID);
						
						// Generate a random payload
						int payload = generator.nextInt();
												
						// Add the id to the hop trace
						ArrayList<Integer> trace = new ArrayList<Integer>();
						trace.add(id);
						
//						System.out.println(id + ": Sending data to " + sendTo);
						
						// Send the data to the correct node
						OverlayNodeSendsData sendData = new OverlayNodeSendsData(Protocol.OVERLAY_NODE_SENDS_DATA, destID, id, payload, trace);
						nodeConnections.getConnection(sendTo).sendData(sendData.getBytes());
						
						// Add to the send tracker and send summation variables. This must be synchronized to avoid 
						// any race conditions
						synchronized (this) {
							sendTracker++;
							sendSummation += payload;
						}
						
						//System.out.println("[INFO]: " + id + " done sending packet #" +(i+1));
					}
					
					// Send the task complete message once n nodes are sent
//					synchronized (this) {
					OverlayNodeReportsTaskFinished taskFinished = new OverlayNodeReportsTaskFinished(Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED, socket.getLocalAddress().getAddress(), socket.getPort(), id);
					serverConnection.sendData(taskFinished.getBytes());
//					}
//					System.out.println(id + ": Done initiating task");
				} catch (IOException e1) {
					System.out.println("Task Initiate error: " + e1.getMessage());
					e1.printStackTrace();
				}
				
				break;
			case Protocol.OVERLAY_NODE_SENDS_DATA:
				// Parse out the event
				OverlayNodeSendsData sentData = (OverlayNodeSendsData) e;
				
				// Grab the destination and payload from the event
				int destID = sentData.getDestID();
	//					int srcID = sentData.getSrcID();
				int payload = sentData.getPayload();
				
	//					System.out.println("[INFO]: " + id + " got a packet from " + srcID + " going to " + destID + " with payload " + payload);
				
				// Get the hop trace and add id
				ArrayList<Integer> hopTrace = sentData.getHopTrace();
				
				hopTrace.add(id);
				
	//					System.out.println("Hop trace size: " + hopTrace.size());
				
				// If this is the destination
				if (destID == id) {
					// Increment counters
					synchronized (this) {
						receiveTracker++;
						receiveSummation+= payload;
					}
				} else {
					synchronized (relayQueue) {
						relayQueue.add(sentData);
					}
				}
				break;
			case Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
				// Generate traffic summary then send message back to registry
				try {
//					System.out.println(id + ": Received traffic summary request. Sending information and clearing data.");

					// Clear all variables to ensure the program can be run multiple times
					synchronized (this) {
						OverlayNodeReportsTrafficSummary trafficSummary = new OverlayNodeReportsTrafficSummary(Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY, id, sendTracker, relayTracker, sendSummation, receiveTracker, receiveSummation);
						sendTracker = 0;
						receiveTracker = 0;
						relayTracker = 0;
						sendSummation = 0;
						receiveSummation = 0;
						synchronized (relayQueue) {
							relayQueue.clear();
						}
						serverConnection.sendData(trafficSummary.getBytes());
					}
				} catch (IOException e1) {
					System.out.println("Error in Messaging Node trying to send traffic summary: " + e1.getMessage());
					e1.printStackTrace();
				}
				break;
			case Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS:
				RegistryReportsDeregistrationStatus dereg = (RegistryReportsDeregistrationStatus) e;
				if (dereg.getSuccess() == -1) {
					System.out.println(dereg.getMessage());
				}
				break;
			default:
				System.out.println("Error in MessagingNode onEvent. Event " + e.getType() + " not recognized");
		}
		
		//System.out.println("Exiting from onEvent");
	}
	
	/***
	 * Generates the statistics to be printed
	 * 
	 */
	public void printCountersAndStats() {
		System.out.println("Messages sent: " + sendTracker);
		System.out.println("Messages received: " + receiveTracker);
		System.out.println("Messages relayed: " + relayTracker);
		System.out.println("Sent summation: " + sendSummation);
		System.out.println("Receive summation: " + receiveSummation);
	}
	
	/***
	 * Helper method that sends the deregistration request
	 */
	public void deregister() {
		try {
			synchronized (this) {
				OverlayNodeSendsDeregistration event = new OverlayNodeSendsDeregistration(Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION, socket.getLocalAddress().getAddress(), socket.getLocalPort(), id);
				serverConnection.sendData(event.getBytes());
			}
		} catch (IOException e) {
			System.out.println("Error with sending overlay deregistration request.");
			e.printStackTrace();
		}
	}
	
	public void run() {
		while(true){
			OverlayNodeSendsData relayMsg;
			synchronized (relayQueue){
				relayMsg = relayQueue.poll();
			}
			 
			if (relayMsg != null){
				try {
					//System.out.println("Got a message to send!");
					int sendTo = table.getDest(relayMsg.getDestID());
					byte[] bytesToSend = relayMsg.getBytes();
					TCPConnection connectionToUse = nodeConnections.getConnection(sendTo);
					connectionToUse.sendData(bytesToSend);
					
					synchronized(relayQueue) {
						System.out.println(relayQueue.size());
					}
					//System.out.println("Sent message, incrementing tracker");
					// Increment the relaytracker variable
					synchronized (this) {
						relayTracker++;
					}
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		}
	}
	
	public static void main(String args[]) {
		if (args.length != 2) {
			// print usage
		} else {
			MessagingNode newNode = new MessagingNode(args[0], Integer.parseInt(args[1]));
			Thread sender = new Thread(newNode);
			sender.start();
			Scanner input = new Scanner(System.in);
			
			String command = "";
			
			int[] commandArray;
			
			InteractiveCommandParser parser = new InteractiveCommandParser("messaging-node");
			
			while (true) {
				command = input.nextLine();
				if (command.equalsIgnoreCase("exit")) {
					break;
				} else {
					commandArray = parser.parse(command);
					switch(commandArray[0]) {
						case 1:
							// Print the counters and diagnostics
							newNode.printCountersAndStats();
							break;
						case 2:
							// Exit the overlay
							newNode.deregister();
							break;
						default: 
							System.out.println("Unknown command. Known commands are: \n\tprint-counters-and-diagnostics\n\texit-overlay");
					}
				}
			}
			
			input.close();
		}
		
	}

}
