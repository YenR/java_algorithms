package ads1.ss14.can.testutils.commands;

import ads1.ss14.can.ClientCommandInterface;
import ads1.ss14.can.Document;
import ads1.ss14.can.exceptions.CANCommandFailed;
import ads1.ss14.can.exceptions.CANException;
import ads1.ss14.can.testutils.NetworkRegistry;


public class SearchDocumentCommand extends CANCommand {
	private String clientID;
	private String documentName;
	
	private static boolean printSearch = false;
	
	public static void setPrintSearch(boolean printSearch) {
		SearchDocumentCommand.printSearch = printSearch;
	}
	
	public SearchDocumentCommand(int cmdNo, String clientID, String documentName) {
		super(cmdNo);
		this.clientID = clientID;
		this.documentName = documentName;
	}

	@Override
	public void executeAndTest(NetworkRegistry reg) throws CANException {
		ClientCommandInterface c = reg.getClient(clientID);
		Document returnedDoc = c.searchForDocument(documentName);
		
		//check if the correct document is returned
		if(returnedDoc != null && !returnedDoc.getName().equals(documentName)) {
			throw new CANCommandFailed(cmdNo, "search - the wrong document has been returned");
		}
		
		if(printSearch)
			System.out.println(reg.dumpLastSearch());
	}
}
