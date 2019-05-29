package ads1.ss14.can.testutils.commands;

import java.util.Map;
import java.util.Set;

import ads1.ss14.can.exceptions.CANException;
import ads1.ss14.can.testutils.NetworkRegistry;

public class LoadCANCommand extends ModifyingCANCommand {

	private Map<String, Set<String>> adjacencyList;
	private Map<String, String> parentOf;
	private Map<String, Integer> maxDocs;
	private Map<String, Set<String>> storedDocNames;
	private int xSize;
	private int ySize;

	public LoadCANCommand(int cmdNo, int xSize, int ySize, Map<String, Set<String>> adjacencyList, Map<String, String> parentOf, Map<String, Integer> maxDocs, Map<String, Set<String>> storedDocNames) {
		super(cmdNo);
		this.xSize = xSize;
		this.ySize = ySize;
		this.adjacencyList = adjacencyList;
		this.parentOf = parentOf;
		this.maxDocs = maxDocs;
		this.storedDocNames = storedDocNames;
	}

	@Override
	public void executeAndTest(NetworkRegistry reg) throws CANException {
		reg.loadCANStructure(xSize, ySize, adjacencyList, parentOf, maxDocs, storedDocNames);
	}

}
