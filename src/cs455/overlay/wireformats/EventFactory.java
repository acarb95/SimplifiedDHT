package cs455.overlay.wireformats;

import java.io.IOException;

/***
 * A singleton class that generates events. This must be single because it would 
 * cause too much memory overhead to have one Event factor for each thread that needs
 * to create an event.
 * 
 * @author acarbona
 *
 */
public class EventFactory {
	// START: This portion makes it singleton (there will only be one instance per machine)
	private static EventFactory instance = new EventFactory();
	
	private EventFactory() {
	}
	
	public static EventFactory getInstance() {
		return instance;
	}
	// END
	
	/***
	 * Creates the event object based on the first byte of data.
	 * It exits if the event type is unknown because that means
	 * the messages got corrupted or mixed up and the program can no
	 * longer continue
	 * @param data - the byte array received
	 * @return The event object that represents the data
	 * @throws IOException
	 */
	public Event createEvent(byte[] data) throws IOException {
		Event e = null;
		switch(data[0]) {
			case Protocol.OVERLAY_NODE_SENDS_REGISTRATION:
				e = new OverlayNodeSendsRegistration(data);
				break;
			case Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS:
				e = new RegistryReportsRegistrationStatus(data);
				break;
			case Protocol.REGISTRY_SENDS_NODE_MANIFEST:
				e = new RegistrySendsNodeManifest(data);
				break;
			case Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS:
				e = new NodeReportsOverlaySetupStatus(data);
				break;
			case Protocol.REGISTRY_REQUESTS_TASK_INITIATE:
				e = new RegistryRequestsTaskInitiate(data);
				break;
			case Protocol.OVERLAY_NODE_SENDS_DATA:
				e = new OverlayNodeSendsData(data);
				break;
			case Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED:
				e = new OverlayNodeReportsTaskFinished(data);
				break;
			case Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
				e = new RegistryRequestsTrafficSummary(data);
				break;
			case Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
				e = new OverlayNodeReportsTrafficSummary(data);
				break;
			case Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION:
				e = new OverlayNodeSendsDeregistration(data);
				break;
			case Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS:
				e = new RegistryReportsDeregistrationStatus(data);
				break;
			default:
				System.out.println("Error in Event Factory. Message type unknown: " + data[0]);
				Exception e1 = new Exception();
				e1.printStackTrace();
				System.exit(-1);
		}
		
		return e;
	}
}
