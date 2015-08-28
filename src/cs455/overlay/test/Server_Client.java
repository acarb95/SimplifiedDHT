package cs455.overlay.test;

import java.util.Scanner;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Registry;

public class Server_Client {

	@SuppressWarnings("unused")
	public static void main (String[] args) {
		int portNumber = Integer.parseInt(args[0]);
		
		Registry server = new Registry(portNumber);
		
		String hostname = server.getName();
		MessagingNode client = new MessagingNode(hostname, portNumber);
		
		MessagingNode client2 = new MessagingNode(hostname, portNumber);
		
		MessagingNode client3 = new MessagingNode(hostname, portNumber);
		MessagingNode client4 = new MessagingNode(hostname, portNumber);
		MessagingNode client5 = new MessagingNode(hostname, portNumber);
		MessagingNode client6 = new MessagingNode(hostname, portNumber);
		MessagingNode client7 = new MessagingNode(hostname, portNumber);
		MessagingNode client8 = new MessagingNode(hostname, portNumber);
		MessagingNode client9 = new MessagingNode(hostname, portNumber);
		MessagingNode client10 = new MessagingNode(hostname, portNumber);
		
		Scanner input = new Scanner(System.in);
		
		String command = input.next();
		
		while (true) {
			if (command.equalsIgnoreCase("exit")){
				break;
			}
			command = input.next();
		}
		
		input.nextLine();
		
		server.generateOverlay(3);
		
		while (true) {
			if (command.equalsIgnoreCase("start")){
				break;
			}
			command = input.next();
		}
		
		server.sendTaskInitiateMessage(1);
		input.close();
	}
	
}
