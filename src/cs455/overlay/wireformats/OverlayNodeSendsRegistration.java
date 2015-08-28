package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/***
 * Provides the encapsulation of the data pertaining to the OverlayNodeSendsRegistration
 * event. Also provides useful methods for the onEvent to use in marshalling and unmarshalling.
 * 
 * @author acarbona
 *
 */
public class OverlayNodeSendsRegistration implements Event {

	private int type;
	private byte[] ipArray;
	private int portNum;
	
	/***
	 * This constructor should be used by the EventFactory and the onEvent methods in the nodes.
	 * It is used when the node wishes to get information from a message it received. 
	 * 
	 * @param marshalledBytes - The byte array that needs to be unmarshalled
	 * @throws IOException
	 */
	public OverlayNodeSendsRegistration(byte[] marshalledBytes) throws IOException {
		ByteArrayInputStream bInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(bInputStream));
		
		type = din.readByte();
		
		int length = din.read();
		byte[] ipAddress = new byte[length];
		
		din.readFully(ipAddress);
		
		ipArray = ipAddress;
		
		portNum = din.readInt();
		
		bInputStream.close();
		din.close();
	}
	
	/***
	 * This constructor is used when the node wishes to send the message. It will save the
	 * information in the class variables. The getBytes method then can be used to turn it 
	 * into a byte array
	 * 
	 * @param type - the type of message (PROTOCOL.OverlayNodeSendsRegistration)
	 * @param ipAddr - the ip address of the node
	 * @param port - the port of the node
	 */
	public OverlayNodeSendsRegistration(int type, byte[] ipAddr, int port) {
		this.type = type;
		ipArray = ipAddr;
		portNum = port;
	}
	
	public byte[] getIPAddress() {
		return ipArray;
	}
	
	public int getPort() {
		return portNum;
	}
	
	@Override
	public byte getType() {
		return (byte) type;
	}

	@Override
	public byte[] getBytes() throws IOException{
		byte[] marshalledBytes = null;
		
		ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bOutputStream));
		
		dout.write(type);
		
		int length = ipArray.length;
		dout.write(length);
		dout.write(ipArray);
		dout.writeInt(portNum);
		
		dout.flush();
		
		marshalledBytes = bOutputStream.toByteArray();
		
		bOutputStream.close();
		dout.close();
		
		return marshalledBytes;
	}

}
