package ads1.ss14.can.testutils.commands;

import ads1.ss14.can.Client;
import ads1.ss14.can.Position;
import ads1.ss14.can.exceptions.CANException;
import ads1.ss14.can.testutils.ClientTestWrapper;
import ads1.ss14.can.testutils.NetworkRegistry;


public class JoinCommand extends ModifyingCANCommand {
	private String newClientID;
	private String entryClientID;
	private Position entryPosition;
	private int storageCapaciy;
	private int networkXSize;
	private int networkYSize;
	
	public JoinCommand(int cmdNo, String newClientID, String entryClientID,
			int newClientXPos, int newClientYPos, int storageCapacity, int networkXSize, int networkYSize) {
		super(cmdNo);
		this.newClientID = newClientID;
		this.entryClientID = entryClientID;
		this.entryPosition = new Position(newClientXPos, newClientYPos);
		this.storageCapaciy = storageCapacity;
		this.networkXSize = networkXSize;
		this.networkYSize = networkYSize;
	}

	@Override
	public void executeAndTest(NetworkRegistry reg) throws CANException {
		Client joiningClient = new ClientTestWrapper(newClientID, networkXSize, networkYSize, reg);
		joiningClient.setMaxNumberOfDocuments(storageCapaciy);
		
		Client entryPoint = reg.getClient(entryClientID);
		joiningClient.joinNetwork(entryPoint, entryPosition);
	}
}
