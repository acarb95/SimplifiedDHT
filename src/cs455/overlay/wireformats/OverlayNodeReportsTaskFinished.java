package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/***
 * Provides the encapsulation of the data pertaining to the OverlayNodeReportsTaskFinished
 * event. Also provides useful methods for the onEvent to use in marshalling and unmarshalling.
 * 
 * @author acarbona
 *
 */
public class OverlayNodeReportsTaskFinished implements Event {

	private int type;
	private byte[] ipAddress;
	private int portNum;
	private int id;

	/***
	 * This constructor should be used by the EventFactory and the onEvent methods in the nodes.
	 * It is used when the node wishes to get information from a message it received. 
	 * 
	 * @param marshalledBytes - The byte array that needs to be unmarshalled
	 * @throws IOException
	 */
	public OverlayNodeReportsTaskFinished(byte[] marshalledBytes) throws IOException {
		ByteArrayInputStream bInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(bInputStream));

		type = din.read();
		
		int arrayLength = din.read();
		ipAddress = new byte[arrayLength];
		din.readFully(ipAddress);
		
		portNum = din.readInt();
		id = din.readInt();
		
		bInputStream.close();
		din.close();
	}
	
	/***
	 * This constructor is used when the node wishes to send the message. It will save the
	 * information in the class variables. The getBytes method then can be used to turn it 
	 * into a byte array
	 * 
	 * @param type - the type of message (PROTOCOL.OverlayNodeReportsTaskFinished)
	 * @param ipAddress - The current ip address of the node sending the message
	 * @param portNum - the port number of the node sending the message
	 * @param id - the id of the node sending the message
	 */
	public OverlayNodeReportsTaskFinished(int type, byte[] ipAddress, int portNum, int id) {
		this.type = type;
		this.ipAddress = ipAddress;
		this.portNum = portNum;
		this.id = id;
	}
	
	// Getters (useful for reading a message)
	public byte[] getIPAddress() {
		return ipAddress;
	}
	
	public int getPortNum() {
		return portNum;
	}
	
	public int getID() {
		return id;
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

		int length = ipAddress.length;
		dout.write(length);
		dout.write(ipAddress);
		
		dout.writeInt(portNum);
		dout.writeInt(id);
		
		dout.flush();
		
		marshalledBytes = bOutputStream.toByteArray();
		
		dout.close();
		bOutputStream.close();
		
		return marshalledBytes;
	}

}
