package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPConnectionsCache;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.InteractiveCommandParser;
import cs455.overlay.util.StatisticsCollectorAndDisplay;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.NodeReportsOverlaySetupStatus;
import cs455.overlay.wireformats.OverlayNodeReportsTaskFinished;
import cs455.overlay.wireformats.OverlayNodeReportsTrafficSummary;
import cs455.overlay.wireformats.OverlayNodeSendsDeregistration;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.RegistryReportsDeregistrationStatus;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;
import cs455.overlay.wireformats.RegistryRequestsTaskInitiate;
import cs455.overlay.wireformats.RegistryRequestsTrafficSummary;
import cs455.overlay.wireformats.RegistrySendsNodeManifest;

/***
 * The registry manages all the nodes in the overlay. It collects and stores the connections
 * as well as generating the overlay. It will also tell the nodes when to initiate tasks.
 * 
 * Usage:
 * 
 * java cs455.overlay.node.Registry <port_num>
 * 
 * The port number needs to be open for the registry to work. You must start the registry before
 * starting any messaging nodes.
 * 
 * 
 * @author acarbona
 *
 */
public class Registry implements Node {

	private TCPServerThread server;
	private TCPConnectionsCache dataset;
	private HashMap<Integer, ArrayList<Integer>> overlay;
	private HashMap<Integer, Integer> nodePorts;
	private int numOfNodes = 0;
	private volatile int finished_nodes = 0;
	private volatile StatisticsCollectorAndDisplay stats =  new StatisticsCollectorAndDisplay();
	private volatile int numOfSetupNodes = 0;
	
	public Registry(int portNum) {
		try {
			nodePorts = new HashMap<Integer, Integer>();
			server = new TCPServerThread(portNum, this);
			dataset = new TCPConnectionsCache();
			Thread thread = new Thread(server);
			thread.start();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return server.getHostName();
	}
	
	@Override
	public void onEvent(Event e) {
		switch(e.getType()) {
			case Protocol.OVERLAY_NODE_SENDS_REGISTRATION:
				// Node registers
				try {
					String message = "";
					int id = -1;
					
					OverlayNodeSendsRegistration event = (OverlayNodeSendsRegistration) e;
					
					InetAddress addr = InetAddress.getByAddress(event.getIPAddress());
					
					TCPConnection connection = server.getUnNamedConnections().get(addr).get(0);
					
					if (connection.equals(null)) {
						message = ("Error: IP address given does not match any connections");
					} else if (dataset.containsValue(connection)) {
						message = ("Error: valid connection already exists");
					} else {
						server.removeConnection(addr, connection);
						Random generator = new Random();
						id = generator.nextInt(128);
						
						int timeout = 0;
						while (dataset.containsKey(id)) {
							id = generator.nextInt(128);
							timeout++;
							if (timeout == 128) {
								message = ("All keys taken");
								id = -1;
								break;
							}
						}
						dataset.add(id, connection);
						nodePorts.put(id, event.getPort());
						message = "Registration request successful. The number of messaging nodes currently consituting the overlay is (" + dataset.size() + ")";
						System.out.println(message);
					}
					
//					synchronized (this) {
						RegistryReportsRegistrationStatus registrationStatus = new RegistryReportsRegistrationStatus(Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS, id, message);
						connection.sendData(registrationStatus.getBytes());
//					}
					
				} catch (IOException e1) {
					System.out.println("Error in overlay node registration: " + e1.getMessage());
					e1.printStackTrace();
				}
				break;
			case Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS:
				NodeReportsOverlaySetupStatus overlaySetupStatus = (NodeReportsOverlaySetupStatus) e;
				String info = overlaySetupStatus.getMessage();
				int status = overlaySetupStatus.getID();
				
				if (status == -1) {
					System.out.println("Overlay setup failed: " + info);
				} else {
	//						System.out.println("Received node overlay setup status from: " + status);
				}
				
				synchronized (this) {
					numOfSetupNodes++;
				}
				
				if (numOfSetupNodes == dataset.size()) {
					System.out.println("Registry is ready to initiate tasks.");
				}
				break;
			case Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED:
				OverlayNodeReportsTaskFinished event = (OverlayNodeReportsTaskFinished) e;
				
				int nodeid = event.getID();
				
				synchronized (this) {
					finished_nodes++;
					System.out.println("Node " + nodeid + " reports task finished. A total of (" + this.finished_nodes + ") have finished");
				}
				
				// Check to see if all nodes have finished (if so, send traffic summary request)
				if (finished_nodes == dataset.size()) {
					try {
						// Sleep to make sure all messages have been passed
						Thread.sleep(20000);
					} catch (InterruptedException e2) {
						e2.printStackTrace();
					}
					finished_nodes = 0;
					
//					synchronized (this) {
						RegistryRequestsTrafficSummary trafficSummaryEvent = new RegistryRequestsTrafficSummary(Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY);
						Iterator<Integer> iter = dataset.getKeys().iterator();
						while (iter.hasNext()) {
							Integer id = iter.next();
							try {
								dataset.getConnection(id).sendData(trafficSummaryEvent.getBytes());
							} catch (IOException e1) {
								System.out.println("Error when sending traffic request in registry: " + e1.getMessage());
								e1.printStackTrace();
							}
						}
//					}
				}
				break;
			case Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
				OverlayNodeReportsTrafficSummary trafficSummary = (OverlayNodeReportsTrafficSummary) e;
				addStats(trafficSummary);
				break;
			case Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION:
				// Generate deregistration status
				try {
					OverlayNodeSendsDeregistration deReg = (OverlayNodeSendsDeregistration) e;
					int nodeID = deReg.getID();
					int nodePort = deReg.getPort();
					byte[] nodeIP = deReg.getIPAddress();
					
					String message;
					TCPConnection connection = null;
					boolean error = false;
					
					if (dataset.containsKey(nodeID)) {
						// Deregister node
						if (IPequals(nodeIP, dataset.getIPAddress(nodeID))) {
							connection = dataset.getConnection(nodeID);
							dataset.remove(nodeID);
							server.addConnection(connection, InetAddress.getByAddress(nodeIP));
							message = "Node has been deregistered. Number of nodes in overlay " + dataset.size();
						} else {
							error = false;
							message = "Node address does not align with submitted address";
							nodeID = -1;
							connection = dataset.getConnection(nodeID);
						}
					} else {
						if (server.getUnNamedConnections().containsKey(InetAddress.getByAddress(nodeIP))) {
							if (server.getUnNamedConnections().get(InetAddress.getByAddress(nodeIP)).size() != 0) {
								int numSearched = 0;
								for (int i = 0; i < server.getUnNamedConnections().get(InetAddress.getByAddress(nodeIP)).size(); i++){
									if (server.getUnNamedConnections().get(InetAddress.getByAddress(nodeIP)).get(i).getPort() == nodePort) {
										connection = server.getUnNamedConnections().get(InetAddress.getByAddress(nodeIP)).get(i);
										break;
									} else {
										numSearched++;
									}
								}
								
								if (numSearched == server.getUnNamedConnections().get(InetAddress.getByAddress(nodeIP)).size()) {
									error = true;
								}
							} else {
								error = true;
							}
						} else {
							error = true;
						}
						message = "Node not registered with overlay";
						nodeID = -1;
					}
					
					if (!error) {
//						synchronized (this) {
							RegistryReportsDeregistrationStatus deRegStatus = new RegistryReportsDeregistrationStatus(Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS, nodeID, message);
							connection.sendData(deRegStatus.getBytes());
						}
//					}
				} catch (IOException e1) {
					System.out.println("Error deregistering node: " + e1.getMessage());
					e1.printStackTrace();
				}
				break;
			default:
				System.out.println("Error in Registry onEvent. Event type " + e.getType() + " not recognized." );
		} 
	}
	
	// Synchronized so that the stats variable will not miss an update from one of the threads. (Comodification error)
	private synchronized void addStats(OverlayNodeReportsTrafficSummary event) {
		stats.addInformation(event.getID(), event.getTotalSent(), event.getTotalRelayed(), event.getTotalReceived(), event.getSumSent(), event.getSumReceived());
		int statsize = stats.getSize();
//		System.out.println("Overlay node (" + event.getID() + ") sent information. Total number of nodes reporting: " + statsize);
		if (statsize == numOfNodes) {
			stats.sumInformation();
			stats.printInformation();
			stats.clear();
		}
	}
	
	private boolean IPequals(byte[] orig, byte[] compare) {
		if (orig.length != compare.length) {
			return false;
		} else {
			for (int i = 0; i < orig.length; i++) {
				if (orig[i] != compare[i]) {
					return false;
				}
			}
			return true;
		}
	}
	
	public void generateOverlay(int numEntries) {
		overlay = new HashMap<Integer, ArrayList<Integer>>();
		
		Set<Integer> ids = dataset.getKeys();
		Integer[] idsSorted = new Integer[dataset.size()];
		ids.toArray(idsSorted);
		Arrays.sort(idsSorted);
		
		for (int numNodes = 0; numNodes < idsSorted.length; numNodes++) {
			ArrayList<Integer> connections = new ArrayList<Integer>();
			for (int i = 0; i < numEntries; i++) {
				int numHops = (int) Math.pow(2, i);
				
				int hopID = idsSorted[(numHops+numNodes)%idsSorted.length];
				
				connections.add(hopID);
				
			}
			
			overlay.put(idsSorted[numNodes], connections);
		}
		
		numOfNodes = overlay.size();
		
		sendOverlayMessages(numEntries);
		
	}
	
	private void sendOverlayMessages(int numEntries) {
		try {
			Iterator<Integer> iter = overlay.keySet().iterator();
			
			while (iter.hasNext()) {
			
				ArrayList<Integer> hids = new ArrayList<Integer>();
				ArrayList<byte[]> ips = new ArrayList<byte[]>();
				ArrayList<Integer> ports = new ArrayList<Integer>();
				
				Integer num = iter.next();
				for (int i = 0; i < overlay.get(num).size(); i++) {
					int nodeID = overlay.get(num).get(i);
					hids.add(nodeID);
					ips.add(dataset.getIPAddress(nodeID));
					ports.add(nodePorts.get(nodeID));
				}
				
				Set<Integer> ids = dataset.getKeys();
				Iterator<Integer> idIter = ids.iterator();
				
				int[] nodes = new int[overlay.size()];
				int count = 0;
				while (idIter.hasNext()) {
					nodes[count] = idIter.next();
					count++;
				}
				
//				synchronized (this) {
					RegistrySendsNodeManifest event = new RegistrySendsNodeManifest(Protocol.REGISTRY_SENDS_NODE_MANIFEST, numEntries, hids, ips, ports, nodes);
					dataset.getConnection(num).sendData(event.getBytes());
//				}
			}
		} catch (IOException e) {
			System.out.println("Error in sending node manifests: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public synchronized void sendTaskInitiateMessage(int numMessages) {
		try {
			RegistryRequestsTaskInitiate event = new RegistryRequestsTaskInitiate(Protocol.REGISTRY_REQUESTS_TASK_INITIATE, numMessages);
			byte[] data = event.getBytes();
			Set<Integer> nodes = dataset.getKeys();
			Iterator<Integer> iter = nodes.iterator();
			
			while(iter.hasNext()) {
				dataset.getConnection(iter.next()).sendData(data);
			}
		
		} catch (IOException e ) {
			System.out.println("Error in sending task initiate messages: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void printOverlayToConsole() {
		Iterator<Integer> iter = overlay.keySet().iterator();
		try {
			while (iter.hasNext()) {
				int id = iter.next();
				byte[] ip = dataset.getIPAddress(id);
				int portNum = dataset.getPort(id);
				
				System.out.println("Node ID: " + id + ", IP: " + InetAddress.getByAddress(ip).toString() + ", Port: " + portNum);
				
				ArrayList<Integer> hops = overlay.get(id);
				
				for (int i = 0; i < hops.size(); i++) {
					int numHops = (int) Math.pow(2, i);
					System.out.println("\t" + numHops + ": " + hops.get(i));
				}
			}
		} catch (UnknownHostException e) {
			System.out.println("Unknown host in print overlay:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void listMessagingNodes() {
		Iterator<Integer> iter = dataset.getKeys().iterator();
		try {
			while (iter.hasNext()) {
				int id = iter.next();
				TCPConnection connection = dataset.getConnection(id);
				String host = InetAddress.getByAddress(connection.getConnectedAddress()).getHostName();
				System.out.println("Node ID: " + id + ", host name: " + host + ", port: " + connection.getPort()); 
			}
		} catch (UnknownHostException e ) {
			System.out.println("Unknown host: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static String usage() {
		return "Registry <portnum>";
	}
	
	public static void main (String[] args) {
		if (args.length != 1) {
			System.out.println(usage());
		} else {
			Registry newReg = new Registry(Integer.parseInt(args[0]));
			Scanner input = new Scanner(System.in);
			
			String command = "";
			
			int[] commandArray;
			
			InteractiveCommandParser parser = new InteractiveCommandParser("registry");
			
			while (true) {
				command = input.nextLine();
				if (command.equalsIgnoreCase("exit")) {
					break;
				} else {
					commandArray = parser.parse(command);
					switch(commandArray[0]) {
						case 1:
							// Call list subroutine
							newReg.listMessagingNodes();
							break;
						case 2:
							// call generate overlay
							if (commandArray.length != 2) {
								System.out.println("Error! Need a entry number");
							} else {
								if (commandArray[1] > 5) {
									System.out.println("Error! Can't have that many entries.");
								} else {
									newReg.generateOverlay(commandArray[1]);
								}
							}
							break;
						case 3:
							// print overlay
							newReg.printOverlayToConsole();
							break;
						case 4:
							// Call registry task initiate
							if (commandArray.length != 2) {
								System.out.println("Error! Need a number of messages to send");
							} else {
								if (newReg.overlay.size() == 0) {
									System.out.println("Error! Must setup the overlay before attempting to send any messages. ");
								} else {
									newReg.sendTaskInitiateMessage(commandArray[1]);
								}
							}
							break;
						default: 
							System.out.println("Unknown command.");
							System.out.println("Please try again with legal commands: ");
							System.out.println("\t list-messaging-nodes");
							System.out.println("\t setup-overlay <num_entries>");
							System.out.println("\t list-routing-tables");
							System.out.println("\t start <num_messages>");
					}
				}
			}
			
			input.close();
		}
	}

}
