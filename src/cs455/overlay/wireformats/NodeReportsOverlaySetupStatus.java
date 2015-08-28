package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/***
 * Holds all information regarding the node overlay setup status. It marshalls and unmarshalls
 * the data as well.
 * 
 * @author acarbona
 *
 */
public class NodeReportsOverlaySetupStatus implements Event {

	private int type;
	private int id;
	private String info;
	
	/***
	 * This constructor should be used by the EventFactory and the onEvent methods in the nodes.
	 * It is used when the node wishes to get information from a message it received. 
	 * 
	 * @param marshalledBytes - The byte array that needs to be unmarshalled
	 * @throws IOException
	 */
	public NodeReportsOverlaySetupStatus (byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream bInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(bInputStream));
		
		type = din.read();
		id = din.readInt();
		
		int infoLength = din.read();
		byte[] infoBytes = new byte[infoLength];
		din.readFully(infoBytes);
		
		info = new String(infoBytes);
		
		bInputStream.close();
		din.close();
	}
	
	/***
	 * This constructor is used when the node wishes to send the message. It will save the
	 * information in the class variables. The getBytes method then can be used to turn it 
	 * into a byte array
	 * 
	 * @param type - the type of message (PROTOCOL.NodeReportsOverlaySetupStatus)
	 * @param id - The id of the node send the status
	 * @param information - Any informational message the node wishes to send
	 */
	public NodeReportsOverlaySetupStatus (int type, int id, String information) {
		this.type = type;
		this.id = id;
		info = information;
	}
	
	// Getters for the information (useful when attempting to read the values)
	public int getID() {
		return id;
	}
	
	public String getMessage() {
		return info;
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
		
		byte[] infoBytes = info.getBytes();
		int length = infoBytes.length;
		
		dout.write(length);
		dout.write(infoBytes);
		
		dout.flush();
		
		marshalledBytes = bOutputStream.toByteArray();
		
		dout.close();
		bOutputStream.close();
		
		return marshalledBytes;
	}

}
