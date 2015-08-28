package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/***
 * Provides the encapsulation of the data pertaining to the REgistryRequestsTaskInitiate
 * event. Also provides useful methods for the onEvent to use in marshalling and unmarshalling.
 * 
 * @author acarbona
 *
 */
public class RegistryRequestsTrafficSummary implements Event {

	private int type;

	/***
	 * This constructor should be used by the EventFactory and the onEvent methods in the nodes.
	 * It is used when the node wishes to get information from a message it received. 
	 * 
	 * @param marshalledBytes - The byte array that needs to be unmarshalled
	 * @throws IOException
	 */
	public RegistryRequestsTrafficSummary(byte[] marshalledBytes) throws IOException {
		ByteArrayInputStream bInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(bInputStream));

		type = din.read();
		
		bInputStream.close();
		din.close();
	}
	
	/***
	 * This constructor is used when the node wishes to send the message. It will save the
	 * information in the class variables. The getBytes method then can be used to turn it 
	 * into a byte array
	 * 
	 * @param type - the type of message (PROTOCOL.RegistryRequestsTaskInitiate)
	 */
	public RegistryRequestsTrafficSummary(int type) {
		this.type = type;
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

		dout.flush();
		
		marshalledBytes = bOutputStream.toByteArray();
		
		dout.close();
		bOutputStream.close();
		
		return marshalledBytes;
	}


}
