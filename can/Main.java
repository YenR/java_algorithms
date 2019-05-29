package ads1.ss14.can;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

import ads1.ss14.can.exceptions.CANException;
import ads1.ss14.can.testutils.NetworkRegistry;
import ads1.ss14.can.testutils.commands.CANCommand;
import ads1.ss14.can.testutils.commands.CANTestCommandReader;

public class Main {
	/**
	 * Der Name der Datei, aus der die Testinstanz auszulesen ist. Ist <code>
	 * null</code>, wenn von {@link System#in} eingelesen wird.
	 */
	private static String fileName = null;

	/** Der abgeschnittene Pfad */
	private static String choppedFileName;

	/** Test flag f&uuml;r Laufzeit Ausgabe */
	private static boolean test = false;

	/** Debug flag f&uuml;r zus&auml;tzliche Debug Ausgaben */
	private static boolean debug = false;
	
	private static boolean printCANStructure = false;
	
	private static boolean printSearchTraces = false;
	
	private static boolean recordCanStructure = false;
	
	public static void main(String[] args) {
		String instFn = null;
		for(String arg : args) {
			if(arg.equals("-d"))
				debug = true;
			else if(arg.equals("-t"))
				test = true;
			else if(arg.equals("-c"))
				printCANStructure = true;
			else if(arg.equals("-s"))
				printSearchTraces = true;
			else if(arg.equals("-r"))
				recordCanStructure = true;
			else
				instFn = arg;
		}
		
		if(instFn == null) {
			System.out.println("A problem instance must be supplied!");
			System.exit(1);
		}
		
		long start = System.currentTimeMillis();
		long end = System.currentTimeMillis();
		long offs = end - start;

		chopFileName();
		StringBuffer msg = new StringBuffer(test ? choppedFileName + ": " : "");

		File instFile = new File(instFn);
		
		NetworkRegistry registry = new NetworkRegistry();
		CANTestCommandReader commandReader = new CANTestCommandReader(instFile, printCANStructure, printSearchTraces);
		try {
			Iterable<CANCommand> commandList = commandReader.readCommands();
			Iterator<String> cmdStringIt = commandReader.getCommandStrings().iterator();
			printDebug(cmdStringIt.next());

			start = System.currentTimeMillis();
			
			CANCommand cmd;
			Iterator<CANCommand> cmdIt = commandList.iterator();
			while(cmdIt.hasNext()) {
				printDebug(cmdStringIt.next());
				cmd = cmdIt.next();
				cmd.executeAndTest(registry);
				cmd.finish(registry);
			}
			
			end = System.currentTimeMillis();
			
			if(recordCanStructure)
				System.out.println(registry.dumpCANStructure(true));
			
		} catch (FileNotFoundException e) {
			bailOut("The provided input files does not exist");
		} catch (CANException e) {
			printDebug(registry.dumpCANStructure());
			bailOut(e.toString());
		} catch (Exception e) {
			//e.printStackTrace();
			bailOut("An unspecified error occurred");
		}
		
		msg.append("OK");
		long sum = end - start - offs;
		if (test)
			msg.append(", Zeit: " + sum + " ms");

		System.out.println("");
		System.out.println(msg.toString());
	}
	
	/**
	 * Gibt die Meldung <code>msg</code> aus und beendet das Programm.
	 * 
	 * @param msg
	 *            Die Meldung die ausgegeben werden soll.
	 */
	private static void bailOut(String msg) {
		System.out.println();
		System.err.println((test ? choppedFileName + ": "
				: "") + "ERR " + msg);
		System.exit(1);
	}

	/**
	 * Generates a chopped String representation of the filename.
	 */
	private static void chopFileName() {
		if (fileName == null) {
			choppedFileName = "System.in";
			return;
		}

		int i = fileName.lastIndexOf(File.separatorChar);

		if (i > 0)
			i = fileName.lastIndexOf(File.separatorChar, i - 1);
		if (i == -1)
			i = 0;

		choppedFileName = ((i > 0) ? "..." : "") + fileName.substring(i);
	}

	/**
	 * Gibt eine debugging Meldung aus. Wenn das Programm mit <code>-d</code>
	 * gestartet wurde, wird <code>msg</code> zusammen mit dem Dateinamen der
	 * Inputinstanz ausgegeben, ansonsten macht diese Methode nichts.
	 * 
	 * @param msg
	 *            Text der ausgegeben werden soll.
	 */
	public static void printDebug(String msg) {
		if (!debug)
			return;

		System.out.println(choppedFileName + ": DBG " + msg);
	}
	
	private Main() {
		
	}

}
