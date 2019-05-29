package ads1.ss14.can.testutils.commands;

import ads1.ss14.can.testutils.NetworkRegistry;

public abstract class ModifyingCANCommand extends CANCommand {
	private static boolean flagPrintStructure; 

	public ModifyingCANCommand(int cmdNo) {
		super(cmdNo);
	}

	public static void setPrintCANStructure(boolean flag) {
		flagPrintStructure = flag;
	}
	
	@Override
	public void finish(NetworkRegistry reg) {
		if(flagPrintStructure)
			System.out.println(reg.dumpCANStructure());
	}
}