package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/***
 * Provides the encapsulation of the data pertaining to the RegistryReportsDeregistrationStatus
 * event. Also provides useful methods for the onEvent to use in marshalling and unmarshalling.
 * 
 * @author acarbona
 *
 */
public class RegistryReportsDeregistrationStatus implements Event {

	private int type;
	private int success;
	private String info;
	
	/***
	 * This constructor should be used by the EventFactory and the onEvent methods in the nodes.
	 * It is used when the node wishes to get information from a message it received. 
	 * 
	 * @param marshalledBytes - The byte array that needs to be unmarshalled
	 * @throws IOException
	 */
	public RegistryReportsDeregistrationStatus (byte[] marshalledByte) throws IOException {
		ByteArrayInputStream bInputStream = new ByteArrayInputStream(marshalledByte);
		DataInputStream din = new DataInputStream(new BufferedInputStream(bInputStream));
		
		type = din.read();

		success = din.readInt();
		
		int length = din.read();
		byte[] information = new byte[length];
		
		din.readFully(information);
		
		info = new String(information);
		
		bInputStream.close();
		din.close();
	}
	
	/***
	 * This constructor is used when the node wishes to send the message. It will save the
	 * information in the class variables. The getBytes method then can be used to turn it 
	 * into a byte array
	 * 
	 * @param type - the type of message (PROTOCOL.RegistryReportsDeregistrationStatus)
	 * @param success - the success of the deregistration (-1 if unsuccessful and the id if it was successful)
	 * @param message - the information message associated with the status
	 */
	public RegistryReportsDeregistrationStatus(int type, int success, String message) {
		this.type = type;
		this.success = success;
		info = message;
	}
	
	public int getSuccess() {
		return success;
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
		byte[] marshalledBytes = null;
		
		ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bOutputStream));
		
		dout.write(type);
		dout.writeInt(success);
		byte[] infobytes = info.getBytes();
		int length = infobytes.length;
		
		dout.write(length);
		dout.write(infobytes);
		
		dout.flush();
		
		marshalledBytes = bOutputStream.toByteArray();
		
		bOutputStream.close();
		dout.close();
		
		return marshalledBytes;
	}

}
