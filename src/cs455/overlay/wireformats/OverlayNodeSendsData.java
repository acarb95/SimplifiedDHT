package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/***
 * Provides the encapsulation of the data pertaining to the OverlayNodeSendsData
 * event. Also provides useful methods for the onEvent to use in marshalling and unmarshalling.
 * 
 * @author acarbona
 *
 */
public class OverlayNodeSendsData implements Event {

	private int type;
	private int destID;
	private int srcID;
	private int payload;
	private ArrayList<Integer> hopTrace;

	/***
	 * This constructor should be used by the EventFactory and the onEvent methods in the nodes.
	 * It is used when the node wishes to get information from a message it received. 
	 * 
	 * @param marshalledBytes - The byte array that needs to be unmarshalled
	 * @throws IOException
	 */
	public OverlayNodeSendsData(byte[] marshalledBytes) throws IOException {
		ByteArrayInputStream bInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(bInputStream));

		type = din.read();
		destID = din.readInt();
		srcID = din.readInt();
		payload = din.readInt();
		
		hopTrace = new ArrayList<Integer>();
		
		int byteLength = din.readInt();
		byte[] hopTraceBytes = new byte[byteLength];
		din.readFully(hopTraceBytes);

		ByteArrayInputStream bAInputStream = new ByteArrayInputStream(hopTraceBytes);
		DataInputStream dAin = new DataInputStream(new BufferedInputStream(bAInputStream));
		
		for (int i = 0; i < byteLength/4; i++) {
			hopTrace.add(dAin.readInt());
		}
		
		bAInputStream.close();
		dAin.close();
		bInputStream.close();
		din.close();
	}
	
	/***
	 * This constructor is used when the node wishes to send the message. It will save the
	 * information in the class variables. The getBytes method then can be used to turn it 
	 * into a byte array
	 * 
	 * @param type - the type of message (PROTOCOL.OverlayNodeSendsData)
	 * @param dest - the destination id of the message
	 * @param src - the source id of the message (which node INITIATED the send)
	 * @param payload - the payload of the message
	 * @param hopTrace - which nodes the message has been to
	 */
	public OverlayNodeSendsData(int type, int dest, int src, int payload, ArrayList<Integer> hopTrace) {
		this.type = type;
		destID = dest;
		srcID = src;
		this.payload = payload;
		this.hopTrace = hopTrace;
	}
	
	// Getters
	public int getDestID() {
		return destID;
	}
	
	public int getSrcID() {
		return srcID;
	}
	
	public int getPayload() {
		return payload;
	}
	
	public ArrayList<Integer> getHopTrace() {
		return hopTrace;
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
		dout.writeInt(destID);
		dout.writeInt(srcID);
		dout.writeInt(payload);
		
		ByteArrayOutputStream bAOutputStream = new ByteArrayOutputStream();
		DataOutputStream daout = new DataOutputStream(new BufferedOutputStream(bAOutputStream));

		for (int i = 0; i < hopTrace.size(); i++) {
			daout.writeInt(hopTrace.get(i));
		}

		daout.flush();
		byte[] hopTraceBytes = bAOutputStream.toByteArray();

		int lengthArray = hopTraceBytes.length;

		dout.writeInt(lengthArray);
		dout.write(hopTraceBytes);
		
		dout.flush();
		
		marshalledBytes = bOutputStream.toByteArray();
		
		bAOutputStream.close();
		daout.close();
		dout.close();
		bOutputStream.close();
		
		return marshalledBytes;
	}


}
