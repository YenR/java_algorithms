package ads1.ss14.can.testutils.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ads1.ss14.can.Pair;
import ads1.ss14.can.exceptions.MalformedCANCommand;

public class CANTestCommandReader {
	private File instFile;
	private List<String> commands;

	public CANTestCommandReader(File instFile, boolean printCANStructure, boolean printSearchTraces) {
		this.instFile = instFile;
		SearchDocumentCommand.setPrintSearch(printSearchTraces);
		ModifyingCANCommand.setPrintCANStructure(printCANStructure);
	}
	
	public List<String> getCommandStrings() {
		return commands;
	}

	public Iterable<CANCommand> readCommands() throws MalformedCANCommand, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(instFile));
		String line;
		String[] cmdComponents;
		int lno, cmdNo = 1;
		List<CANCommand> commandList = new ArrayList<CANCommand>();
		commands = new LinkedList<String>();
		int xSize = -1, ySize = -1;
		
		try {
			for(lno = 1, line = reader.readLine();
					line != null;
					lno += 1, line = reader.readLine()) {
				line = line.toLowerCase();
				cmdComponents = line.split(" +");
				
				if(!line.isEmpty() && !line.startsWith("#"))
					commands.add(line);
				
				if(lno == 1) {
					if(!cmdComponents[0].equals("network"))
						throw new MalformedCANCommand(lno, "The first line must be the 'network xSize ySize' command.");
					try {
						xSize = Integer.parseInt(cmdComponents[1]);
						ySize = Integer.parseInt(cmdComponents[2]);
					} catch(NumberFormatException e) {
						throw new MalformedCANCommand(lno, line);
					}
					
				} else if(cmdComponents[0].equals("join")) {
					if(cmdComponents.length != 6)
						throw new MalformedCANCommand(lno, line);
					try {
						commandList.add(new JoinCommand(cmdNo++, cmdComponents[1], cmdComponents[2],
								Integer.parseInt(cmdComponents[3]), Integer.parseInt(cmdComponents[4]), Integer.parseInt(cmdComponents[5]),
								xSize, ySize));
					} catch(NumberFormatException e) {
						throw new MalformedCANCommand(lno, line);
					}
					
				} else if(cmdComponents[0].equals("add")) {
					if(cmdComponents.length != 3)
						throw new MalformedCANCommand(lno, line);
					commandList.add(new AddDocumentCommand(cmdNo++, cmdComponents[1], cmdComponents[2]));
					
				} else if(cmdComponents[0].equals("delete")) {
					if(cmdComponents.length != 3)
						throw new MalformedCANCommand(lno, line);
					commandList.add(new DeleteDocumentCommand(cmdNo++, cmdComponents[1], cmdComponents[2]));
					
				} else if(cmdComponents[0].equals("search")) {
					if(cmdComponents.length != 3)
						throw new MalformedCANCommand(lno, line);
					commandList.add(new SearchDocumentCommand(cmdNo++, cmdComponents[1], cmdComponents[2]));
					
				} else if(cmdComponents[0].equals("begin_test_can")) {
					List<String> adjacencyStrings = readTestSection(reader, "^\\s*---.*");
					
					List<String> storedDocs;
					storedDocs = readTestSection(reader, "end_test_can");
					
					Map<String, Set<String>> expAdjacencyList = createTestSectionMap(adjacencyStrings);
					Map<String, Set<String>> expStoredDocNames = createTestSectionMap(storedDocs);
					
					commandList.add(new TestCANStructureCommand(cmdNo++, expAdjacencyList, expStoredDocNames));

				} else if(cmdComponents[0].equals("test_search")) {
					List<String> expSearchTrace = new LinkedList<String>();
					for(int i=1; i<cmdComponents.length; ++i) {
						expSearchTrace.add(cmdComponents[i]);
					}
					
					commandList.add(new TestSearchCommand(cmdNo++, expSearchTrace));
				
				} else if(cmdComponents[0].equals("begin_load_can")) {
					List<String> adjacencyStrings = readTestSection(reader, "^\\s*---.*");
					
					List<String> storedDocs;
					storedDocs = readTestSection(reader, "end_load_can");
					
					Map<String, Set<String>> expAdjacencyList = createTestSectionMap(adjacencyStrings);
					Pair<Map<String, String>, Map<String, Integer>> info = getLoadingInfo(adjacencyStrings);
					Map<String, Set<String>> expStoredDocNames = createTestSectionMap(storedDocs);
					
					commandList.add(new LoadCANCommand(cmdNo++, xSize, ySize, expAdjacencyList, info.first, info.second, expStoredDocNames));
				}
				
				else if(!line.isEmpty() && !line.startsWith("#")){
					throw new MalformedCANCommand(lno, line);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}
		
		return commandList;
	}
	
	private static List<String> readTestSection(BufferedReader reader, String delimiter) throws IOException, MalformedCANCommand {
		List<String> lines = new LinkedList<String>();
		String line = null;
		for(line = reader.readLine();
				line != null && !line.matches(delimiter);
				line = reader.readLine()) {
			line = line.toLowerCase();
			if(!line.matches("\\s*"))
				lines.add(line);
		}
		if(line == null)
			throw new MalformedCANCommand(-1, "PARSING OF SECTION FAILED. DELIMITER NOT FOUND: " + delimiter);
		return lines;
	}
	
	private static Map<String, Set<String>> createTestSectionMap(List<String> section) {
		Map<String, Set<String>> sectionMap = new HashMap<String, Set<String>>();
		
		for(Iterator<String> it = section.iterator(); it.hasNext();) {
			String[] lhsRhs = it.next().trim().split(" *(<[^>]+>)? *: *");
			String curClient = lhsRhs[0];
			Set<String> rhs = new HashSet<String>();
			
			if(lhsRhs.length == 2) {
				String[] rhsComponents = lhsRhs[1].split(" *, *");
				if(rhsComponents.length > 1 || !rhsComponents[0].matches("\\s*")) {
					Collections.addAll(rhs, rhsComponents);
				}
			}
			sectionMap.put(curClient, rhs);
		}
		
		return sectionMap;
	}
	
	private static Pair<Map<String, String>, Map<String, Integer>> getLoadingInfo(List<String> section) throws MalformedCANCommand {
		Pattern parentRegex = Pattern.compile("([A-Za-z0-9]+) *<([A-Za-z0-9]+), *([0-9]+)>");
		Map<String, String> parentOf = new HashMap<String, String>();
		Map<String, Integer> maxDocs = new HashMap<String, Integer>();
		
		Matcher m;
		String curLine, child, parent;
		
		for(Iterator<String> it = section.iterator(); it.hasNext();) {
			curLine = it.next();
			m = parentRegex.matcher(curLine);
			
			if(!m.find())
				throw new MalformedCANCommand(-1, curLine);
			child = m.group(1);
			parent = m.group(2);
			parentOf.put(child, parent);
			maxDocs.put(child, Integer.valueOf(m.group(3)));
		}
		
		return new Pair<Map<String, String>, Map<String, Integer>>(parentOf, maxDocs);
	}
}
