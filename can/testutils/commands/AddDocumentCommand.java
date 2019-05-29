package ads1.ss14.can.testutils.commands;

import ads1.ss14.can.ClientCommandInterface;
import ads1.ss14.can.Document;
import ads1.ss14.can.exceptions.CANException;
import ads1.ss14.can.testutils.NetworkRegistry;


public class AddDocumentCommand extends ModifyingCANCommand {
	private String clientID;
	private Document document;

	public AddDocumentCommand(int cmdNo, String clientID, String documentName) {
		super(cmdNo);
		this.clientID = clientID;
		this.document = new Document(documentName);
	}

	@Override
	public void executeAndTest(NetworkRegistry reg) throws CANException {
		ClientCommandInterface c = reg.getClient(clientID);
		c.addDocumentToNetwork(document);
	}
}
