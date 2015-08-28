package cs455.overlay.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

/***
 * The data structure that organizes and holds the routing table for a node. 
 * It also computes the next node to send a package to.
 * 
 * @author acarbona
 *
 */
public class RoutingTable {
	private Hashtable<Integer, Integer> entries;
	private int myID;
	private ArrayList<Integer> allNodes;
	
	/***
	 * The constructor for the routing table. It requires the id of it's creating node
	 * to determine where to send the node
	 * @param myID The id of the creating node
	 */
	public RoutingTable(int myID) {
		// Clear variables and setup the routing table for entries
		entries = new Hashtable<Integer, Integer>();
		this.myID = myID;
		allNodes = new ArrayList<Integer>();
	}
	
	/***
	 * Populate the overall node list. The routing table needs to know what nodes exist in the
	 * overall overlay in order to determine certain corner cases
	 * @param nodes A list of all the node ids in the overlay
	 */
	public void populateNodeList(ArrayList<Integer> nodes) {
		allNodes = nodes;
	}
	
	/***
	 * Adds an entry into the routing table
	 * @param id the id of the node
	 * @param hops the distance from the current node to the id (in number of "hops")
	 */
	public void add(int id, int hops) {
		entries.put(hops, id);
	}
	
	/***
	 * Generates which id to send the package to. It relies on three cases:
	 * 	1. The node is directly connected (exists in the routing table)
	 * 	2. The node is greater than the highest node on the list (i.e. its greater than 4 hops away)
	 * 	3. The node is a "gap node". It is in between two listings
	 * 
	 * @param destid Where the packet needs to go
	 * @return the id to send the packet to
	 */
	public int getDest(int destid){
		int sendTo = -1;
		// Make sure that the allNodes array has been populated
		if (allNodes.size() != 0) {
			// Case 1
			if (entries.containsValue(destid)) {
				sendTo = destid;
			// Case 2
			} else if (destid > Collections.max(entries.values())) {
				sendTo = Collections.max(entries.values());
			// Case 3
			} else {
				// Two possible subcases: The destination is less than the current id (must go around the circle)
				// or the destination is greater
				if (destid < myID) {
					// If the destination is less, start at the farthest out node and work backwards
					for (int i = entries.size() - 1; i > 0; i--) {
						// If the destination id is greater than the entry, then we send it to that entry
						if (destid > entries.get((int) Math.pow(2, i))) {
							sendTo = entries.get((int) Math.pow(2, i));
							break;
						}
					}
					
					// If sendTo -1 that means, no entry was found that had worked. This is a special
					// corner case in the algorithm. The solution is the send it to the highest id of all the nodes
					if (sendTo == -1) {
						sendTo = Collections.max(entries.values());
					}
				} else {
					// Else the destination is greater (meaning we shouldn't go around the circle)
					// Start at the lowest number of hops away and check each gap.
					for (int i = 1; i < entries.size(); i++) {
						// If it is greater than the entry before but less than the entry after, send it to the entry before
						if (destid < entries.get((int) Math.pow(2, i)) && destid > entries.get((int) Math.pow(2, i-1))) {
							sendTo = entries.get((int) Math.pow(2, i-1));
							break;
						}
					}
					
					// If sendTo is -1 then we hit a special corner case where we are attempting to send it to the smallest
					// id. Therefore we need to send it to the largest id in our routing table
					if (sendTo == -1) {
						sendTo = Collections.max(entries.values());
					}
				}
			}
		} else {
			System.out.println("Must populate all nodes list in routing table.");
			System.exit(-1);
		}
		
		// This ensures we don't attempt to send to -1. The algorithm should not ever allow sendTo to equal -1
		if (sendTo == -1) {
			System.out.println("Error sending to -1. Source: " + myID + ", Destination: " + destid);
			System.exit(-1);
		}
		
		return sendTo;
	}
}
