package ads1.ss14.can.testutils.commands;

import ads1.ss14.can.exceptions.CANException;
import ads1.ss14.can.testutils.NetworkRegistry;

public abstract class CANCommand {
	protected int cmdNo;
	
	public CANCommand(int cmdNo) {
		this.cmdNo = cmdNo;
	}
	
	public abstract void executeAndTest(NetworkRegistry reg) throws CANException;

	public void finish(NetworkRegistry reg) {
		//Does nothing
	}
}
