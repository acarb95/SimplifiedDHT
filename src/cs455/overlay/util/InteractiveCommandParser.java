package cs455.overlay.util;

/***
 * Parses out the commands for the registry and messaging-node. If any other 
 * types of nodes are added, their commands and type would need to be added here. 
 * 
 * @author acarbona
 *
 */
public class InteractiveCommandParser {
	
	private String type;
	
	public InteractiveCommandParser(String type) {
		this.type = type.toLowerCase();
	}
	
	/***
	 * Check to see which parsing method to call based on the type of node
	 * The type of node is set when the command parser is constructed.
	 *  
	 * @param command - the string to parse
	 * @return the command code for that node with any arguments
	 */
	public int[] parse(String command) {
		if (type.toLowerCase().equalsIgnoreCase("registry")) {
			return parseRegistryCommand(command);
		} else if (type.toLowerCase().equalsIgnoreCase("messaging-node")){
			return parseNodeCommand(command);
		} else {
			System.out.println("Error, wrong type given.");
		}
		
		return null;
	}
	
	/***
	 * Takes in a command from the registry node and parses it according to the specifications.
	 * If it cannot understand the command it returns a command code of -1
	 * 
	 * @param command - The string given to the node from the user
	 * @return the command code and arguments if applicable
	 */
	private int[] parseRegistryCommand(String command) {
		int codes[] = null;
		
		String[] list_command = command.split(" ");
		
		switch(list_command[0]) {
			case "list-messaging-nodes":
				if (list_command.length != 1) {
					// Error message
				} else {
					codes = new int[1];
					codes[0] = 1;
				}
				break;
			case "setup-overlay":
				if (list_command.length != 2) {
					// Error message
				} else {
					codes = new int[2];
					codes[0] = 2;
					try {
						codes[1] = Integer.parseInt(list_command[1]);
					} catch (NumberFormatException e) {
						System.out.println(e.getMessage());
					}
				}
				break;
			case "list-routing-tables":
				if (list_command.length != 1) {
					// Error
				} else {
					codes = new int[1];
					codes[0] = 3;
				}
				break;
			case "start":
				if (list_command.length != 2) {
					// Error
				} else {
					codes = new int[2];
					codes[0] = 4;
					try {
						codes[1] = Integer.parseInt(list_command[1]);
					} catch (NumberFormatException e) {
						System.out.println(e.getMessage());
					}
				}
				
				break;
			default:
				codes = new int[1];
				codes[0] = -1;
		}
		
		return codes;
	}
	
	/***
	 * Takes in a command from the messaging node and parses it according to the specifications.
	 * If it cannot understand the command it returns a command code of -1
	 * 
	 * @param command - The string given to the node from the user
	 * @return the command code and arguments if applicable
	 */
	private int[] parseNodeCommand(String command) {
		int codes[] = null;
		
		String[] list_command = command.split(" ");
		
		switch(list_command[0]) {
			case "print-counters-and-diagnostics":
				if (list_command.length != 1) {
					codes = new int[1];
					codes[0] = -1;
				} else {
					codes = new int[1];
					codes[0] = 1;
				}
				break;
			case "exit-overlay":
				if (list_command.length != 1) {
					codes = new int[1];
					codes[0] = -1;
				} else {
					codes = new int[1];
					codes[0] = 2;
				}
				break;
			default:
				codes = new int[1];
				codes[0] = -1;
		}
		
		return codes;
	}

}
