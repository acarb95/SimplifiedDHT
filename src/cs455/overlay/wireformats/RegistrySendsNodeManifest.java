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
 * Provides the encapsulation of the data pertaining to the REgistryRequestsTaskInitiate
 * event. Also provides useful methods for the onEvent to use in marshalling and unmarshalling.
 * 
 * @author acarbona
 *
 */
public class RegistrySendsNodeManifest implements Event {

	private int type;
	private int size;
	
	private ArrayList<Integer> hopIDs;
	private ArrayList<byte[]> hopIPs;
	private ArrayList<Integer> hopPorts;

	private int[] allNodes;

	/***
	 * This constructor should be used by the EventFactory and the onEvent methods in the nodes.
	 * It is used when the node wishes to get information from a message it received. 
	 * 
	 * @param marshalledBytes - The byte array that needs to be unmarshalled
	 * @throws IOException
	 */
	public RegistrySendsNodeManifest(byte[] marshalledByte) throws IOException {
		hopIDs = new ArrayList<Integer>();
		hopIPs = new ArrayList<byte[]>();
		hopPorts = new ArrayList<Integer>();
		ByteArrayInputStream bInputStream = new ByteArrayInputStream(marshalledByte);
		DataInputStream din = new DataInputStream(new BufferedInputStream(bInputStream));

		type = din.read();
		size = din.read();

		for (int i = 0; i < size; i++) {
			hopIDs.add(din.readInt());
			int length = din.read();
			byte[] temp = new byte[length];
			din.readFully(temp);
			hopIPs.add(temp);
			hopPorts.add(din.readInt());	
		}
		
		// Get number of nodes
		int numNodes = din.read();

		allNodes = new int[numNodes];

		// Read in array
		byte[] tempByte = new byte[numNodes*4];
		din.readFully(tempByte);

		ByteArrayInputStream bAInputStream = new ByteArrayInputStream(tempByte);
		DataInputStream dAin = new DataInputStream(new BufferedInputStream(bAInputStream));
		
		for (int i = 0; i < numNodes; i++) {
			allNodes[i] = dAin.readInt();
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
	 * @param type - the type of message (PROTOCOL.RegistryRequestsTaskInitiate)
	 * @param size - the number of entries in the routing table
	 * @param ids - a list of the node ids for the routing table (in order of hops)
	 * @param ips - a list of the node ips for the routing table (in order of hops)
	 * @param ports - a list of the node ports for the routing table (in order of hops)
	 * @param nodes - a list of all the node ids for the entire overlay
	 */
	public RegistrySendsNodeManifest(int type, int size, ArrayList<Integer> ids, ArrayList<byte[]> ips, ArrayList<Integer> ports, int[] nodes) {
		this.type = type;
		this.size = size;
		hopIDs = ids;
		hopIPs = ips;
		hopPorts = ports;
		allNodes = nodes;
	}
	
	public int getSize() {
		return size;
	}
	
	public ArrayList<Integer> getIDs() {
		return hopIDs;
	}
	
	public ArrayList<byte[]> getIPs() {
		return hopIPs;
	}
	
	public ArrayList<Integer> getPorts() {
		return hopPorts;
	}
	
	public int[] getNodes() {
		return allNodes;
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
		dout.write(size);

		for (int i = 0; i < size; i++) {
			dout.writeInt(hopIDs.get(i));
			int length = hopIPs.get(i).length;
			dout.write(length);
			dout.write(hopIPs.get(i));
			dout.writeInt(hopPorts.get(i));	
		}

		// Add number of nodes
		int numNodes = allNodes.length;
		dout.write(numNodes);

		// Add int array
		ByteArrayOutputStream bAOutputStream = new ByteArrayOutputStream();
		DataOutputStream daout = new DataOutputStream(new BufferedOutputStream(bAOutputStream));

		for (int i = 0; i < allNodes.length; i++) {
			daout.writeInt(allNodes[i]);
		}

		daout.flush();
		byte[] allNodesByte = bAOutputStream.toByteArray();

		dout.write(allNodesByte);

		dout.flush();

		marshalledBytes = bOutputStream.toByteArray();

		bAOutputStream.close();
		daout.close();
		bOutputStream.close();
		dout.close();

		return marshalledBytes;
	}

}
