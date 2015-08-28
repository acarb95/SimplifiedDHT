package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/***
 * Provides the encapsulation of the data pertaining to the OverlayNodeReportsTrafficSummary
 * event. Also provides useful methods for the onEvent to use in marshalling and unmarshalling.
 * 
 * @author acarbona
 *
 */
public class OverlayNodeReportsTrafficSummary implements Event {

	private int type;
	private int id;
	private int totalSent;
	private int totalRelayed;
	private long sumSent;
	private int totalReceived;
	private long sumReceived;

	/***
	 * This constructor should be used by the EventFactory and the onEvent methods in the nodes.
	 * It is used when the node wishes to get information from a message it received. 
	 * 
	 * @param marshalledBytes - The byte array that needs to be unmarshalled
	 * @throws IOException
	 */
	public OverlayNodeReportsTrafficSummary(byte[] marshalledBytes) throws IOException {
		ByteArrayInputStream bInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(bInputStream));

		type = din.read();
		id = din.readInt();
		totalSent = din.readInt();
		totalRelayed = din.readInt();
		sumSent = din.readLong();
		totalReceived = din.readInt();
		sumReceived = din.readLong();
		
		bInputStream.close();
		din.close();
	}
	
	/***
	 * This constructor is used when the node wishes to send the message. It will save the
	 * information in the class variables. The getBytes method then can be used to turn it 
	 * into a byte array
	 * 
	 * @param type - the type of message (PROTOCOL.OverlayNodeReportsTrafficSummary)
	 * @param id - the id of the node sending the message
	 * @param totalSent - the number of messages sent
	 * @param totalRelayed - the number of messages relayed by the node
	 * @param sumSent - the summation of the payloads that the node sent
	 * @param totalReceived - the number of messages received
	 * @param sumReceived - the summation of the payloads received by the node
	 */
	public OverlayNodeReportsTrafficSummary(int type, int id, int totalSent, int totalRelayed, long sumSent, int totalReceived, long sumReceived) {
		this.type = type;
		this.id = id;
		this.totalSent = totalSent;
		this.totalRelayed = totalRelayed;
		this.totalReceived = totalReceived;
		this.sumReceived = sumReceived;
		this.sumSent = sumSent;
	}
	
	// Getters
	public int getID() {
		return id;
	}
	
	public int getTotalSent() {
		return totalSent;
	}
	
	public int getTotalRelayed() {
		return totalRelayed;
	}
	
	public int getTotalReceived() {
		return totalReceived;
	}
	
	public long getSumSent() {
		return sumSent;
	}
	
	public long getSumReceived() {
		return sumReceived;
	}

	@Override
	public byte getType() {
		return (byte) type;
	}

	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes;

		ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bOutputStream));

		dout.write(type);
		dout.writeInt(id);
		dout.writeInt(totalSent);
		dout.writeInt(totalRelayed);
		dout.writeLong(sumSent);
		dout.writeInt(totalReceived);
		dout.writeLong(sumReceived);
		
		dout.flush();
		
		marshalledBytes = bOutputStream.toByteArray();
		
		dout.close();
		bOutputStream.close();
		
		return marshalledBytes;
	}

}
