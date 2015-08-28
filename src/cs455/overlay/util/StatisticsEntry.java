package cs455.overlay.util;

/***
 * An object to contain all information needed for the statistics summary.
 * This includes: 
 * 	- Packets Sent
 * 	- Packets Received
 * 	- Packets Relayed
 * 	- Sum sent
 * 	- Sum Received
 * 
 * It also stores the nodeID for debugging purposes.
 * 
 * @author acarbona
 *
 */
public class StatisticsEntry {
	private int packetsSent;
	private int packetsReceived;
	private int packetsRelayed;
	private long sumSent;
	private long sumReceived;
	private int nodeID;
	
	public StatisticsEntry(int sent, int received, int relayed, long sumSent, long sumReceived, int id) {
		packetsSent = sent;
		packetsReceived = received;
		packetsRelayed = relayed;
		this.sumSent = sumSent;
		this.sumReceived = sumReceived;
		nodeID = id;
	}
	
	// Getters (no setters because everything should be set in the constructor)
	public int getSent() {
		return packetsSent;
	}
	
	public int getReceived() {
		return packetsReceived;
	}
	
	public int getRelayed() {
		return packetsRelayed;
	}
	
	public int getID() {
		return nodeID;
	}
	
	public long getSumSent() {
		return sumSent;
	}
	
	public long getSumReceived() {
		return sumReceived;
	}
}
