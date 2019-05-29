package ads1.ss14.can.testutils.commands;

import ads1.ss14.can.ClientCommandInterface;
import ads1.ss14.can.exceptions.CANCommandFailed;
import ads1.ss14.can.exceptions.UnknownClientID;
import ads1.ss14.can.testutils.NetworkRegistry;


public class DeleteDocumentCommand extends ModifyingCANCommand {
	String clientID;
	String documentName;

	public DeleteDocumentCommand(int cmdNo, String clientID, String documentName) {
		super(cmdNo);
		this.clientID = clientID;
		this.documentName = documentName;
	}

	@Override
	public void executeAndTest(NetworkRegistry reg) throws UnknownClientID, CANCommandFailed {
		ClientCommandInterface c = reg.getClient(clientID);
		c.removeDocumentFromNetwork(documentName);
	}
}
