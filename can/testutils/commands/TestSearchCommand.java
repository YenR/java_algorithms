package ads1.ss14.can.testutils.commands;

import java.util.List;

import ads1.ss14.can.exceptions.CANCommandFailed;
import ads1.ss14.can.exceptions.UnknownClientID;
import ads1.ss14.can.testutils.NetworkRegistry;

public class TestSearchCommand extends CANCommand {
	private List<String> clientIDTrace;

	public TestSearchCommand(int cmdNo, List<String> clientIDTrace) {
		super(cmdNo);
		
		this.clientIDTrace = clientIDTrace;
	}

	@Override
	public void executeAndTest(NetworkRegistry reg) throws UnknownClientID, CANCommandFailed {
		reg.testLastSearchTrace(cmdNo, clientIDTrace);
	}

}
