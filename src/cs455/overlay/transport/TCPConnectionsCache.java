package cs455.overlay.transport;

import java.util.HashMap;
import java.util.Set;

/***
 * The TCPConnectionsCache holds a data structure that contains all the connections
 * in the Registry or Messaging Node for the Routing table. It provides basic operations such as
 * 	- Get
 * 	- Contains
 * 	- Add
 * 	- Remove
 * 	- Size
 * @author acarbona
 *
 */

public class TCPConnectionsCache {

	HashMap<Integer, TCPConnection> dataset;
	
	public TCPConnectionsCache() {
		dataset = new HashMap<Integer, TCPConnection>();
	}
	
	public void add(int nodeID, TCPConnection connection) {
		dataset.put(new Integer(nodeID), connection);
	}
	
	public void remove(int nodeID){
		dataset.remove(nodeID);
	}
	
	public TCPConnection getConnection(int nodeID) {
		return dataset.get(new Integer(nodeID));
	}
	
	public byte[] getIPAddress(int nodeID) {
		return dataset.get(nodeID).getConnectedAddress();
	}
	
	public int getPort(int nodeID) {
		return dataset.get(nodeID).getPort();
	}
	
	public boolean containsValue(TCPConnection connection) {
		return dataset.containsValue(connection);
	}
	
	public boolean containsKey(int id) {
		return dataset.containsKey(new Integer(id));
	}
	
	public int size() {
		return dataset.size();
	}
	
	public Set<Integer> getKeys() {
		return dataset.keySet();
	}
}
