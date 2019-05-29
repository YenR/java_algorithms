package ads1.ss14.can.testutils.commands;

import java.util.Map;
import java.util.Set;

import ads1.ss14.can.exceptions.CANCommandFailed;
import ads1.ss14.can.testutils.NetworkRegistry;

public class TestCANStructureCommand extends CANCommand {
	private Map<String, Set<String>> expAdjacencyList;
	private Map<String, Set<String>> expStoredDocNames;

	public TestCANStructureCommand(int cmdNo, Map<String, Set<String>> expAdjacencyList, Map<String, Set<String>> expStoredDocNames) {
		super(cmdNo);
		this.expAdjacencyList = expAdjacencyList;
		this.expStoredDocNames = expStoredDocNames;
	}

	@Override
	public void executeAndTest(NetworkRegistry reg) throws CANCommandFailed {
		reg.checkCANStructure(cmdNo, expAdjacencyList, expStoredDocNames);
	}
}
