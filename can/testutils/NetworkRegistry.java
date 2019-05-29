package ads1.ss14.can.testutils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ads1.ss14.can.Area;
import ads1.ss14.can.Client;
import ads1.ss14.can.ClientInterface;
import ads1.ss14.can.Document;
import ads1.ss14.can.Pair;
import ads1.ss14.can.Position;
import ads1.ss14.can.exceptions.CANCommandFailed;
import ads1.ss14.can.exceptions.CANException;
import ads1.ss14.can.exceptions.DuplicateClientID;
import ads1.ss14.can.exceptions.InvalidClient;
import ads1.ss14.can.exceptions.InvalidDocument;
import ads1.ss14.can.exceptions.NoSuchDocument;
import ads1.ss14.can.exceptions.UnknownClientID;

public class NetworkRegistry {
	private static final int MAX_CLIENT_NUM_DOCS = 10000;
	
	public class Search {
		public String documentName;
		public List<Client> searchTrace;
		
		public Search(String docName, List<Client> searchTrace) {
			this.documentName = docName;
			this.searchTrace = searchTrace;
		}
	}
	
	private Map<String, ClientTestWrapper> registeredClients;
	private ListIterator<Client> currentSearch;
	private List<Search> searches;
	private Map<String, Set<String>> clientID2StoredDocNames;
	private Map<String, String> child2Parent; 
	
	public NetworkRegistry() {
		resetRegistry();
	}
	
	public void resetRegistry() {
		registeredClients = new HashMap<String, ClientTestWrapper>();
		currentSearch = null;
		searches = new LinkedList<Search>();;
		clientID2StoredDocNames = new HashMap<String, Set<String>>();
		child2Parent = new HashMap<String, String>();
	}
	
	public void resetCANHistory() {
		searches = new LinkedList<Search>();
		currentSearch = null;
	}
	
	public void registerNewClient(ClientTestWrapper client, Position p) throws DuplicateClientID, InvalidClient {
		if(client == null)
			throw new InvalidClient("null is not a valid Client");
		else if(client.getUniqueID() == null || client.getUniqueID().equals("null"))
			throw new InvalidClient("null and 'null' are not allowed as Client ID");
		
		if(registeredClients.containsKey(client.getUniqueID()))
			throw new DuplicateClientID(client.getUniqueID());
		registeredClients.put(client.getUniqueID(), client);
		clientID2StoredDocNames.put(client.getUniqueID(), new HashSet<String>());
	}
	
	public void setParentOf(ClientTestWrapper child, ClientInterface parent) {
		if(parent != null && !parent.equals("null"))
			child2Parent.put(child.getUniqueID(), parent.getUniqueID());
	}
	
	public ClientTestWrapper getClient(String clientID) throws UnknownClientID {
		if(clientID == null || clientID.equals("null"))
			return null;
		if(!registeredClients.containsKey(clientID)) {
			throw new UnknownClientID(clientID);
		}
		return registeredClients.get(clientID);
	}

	public void beginSearch(String searchedDocName) throws InvalidDocument {
		if(searchedDocName == null)
			throw new InvalidDocument("null is not a valid document name");
		if(currentSearch == null) {
			LinkedList<Client> l = new LinkedList<Client>();
			currentSearch = l.listIterator();
			searches.add(new Search(searchedDocName, l));
		}
	}
	
	public void addVisitedClient(Client client) {
		if(currentSearch != null)
			currentSearch.add(client);
	}

	public void endSearch() {
		currentSearch = null;
	}
	
	public void loadCANStructure(int networkX, int networkY, Map<String, Set<String>> adjacencyList, Map<String, String> parentOf, Map<String, Integer> maxDocs, Map<String, Set<String>> storedDocNames) throws CANException {
		resetRegistry();
		
		int idx = 0;
		String base = "client";
		String curClientID = base + String.valueOf(idx);
		ClientTestWrapper curClient = new ClientTestWrapper(curClientID, networkX, networkY, this);
		curClient.setArea(new Area(0, networkX, 0, networkY));
		curClient.setMaxNumberOfDocuments(maxDocs.get(curClientID));
		registerNewClient(curClient, null);
		
		//first determine the managed areas
		Area oldArea;
		Pair<Area, Area> areas;
		ClientTestWrapper parent;
		for(idx = 1; idx < parentOf.size(); ++idx) {
			curClientID = base + String.valueOf(idx);
			curClient = new ClientTestWrapper(curClientID, networkX, networkY, this);
			
			parent = registeredClients.get(parentOf.get(curClientID));
			
			oldArea = parent.getArea();
			if(oldArea.getUpperX() - oldArea.getLowerX() >= oldArea.getUpperY() - oldArea.getLowerY()){
				areas = oldArea.splitVertically();
				parent.setArea(areas.first);
				curClient.setArea(areas.second);
			} else {
				areas = oldArea.splitHorizontally(); 
				parent.setArea(areas.second);
				curClient.setArea(areas.first);
			}
			
			curClient.setMaxNumberOfDocuments(maxDocs.get(curClientID));
			setParentOf(curClient, parent);
			registerNewClient(curClient, null);
		}
		
		//link neighbours
		ClientTestWrapper neighbour;
		for(idx = 0; idx < adjacencyList.size(); ++idx) {
			curClientID = base + String.valueOf(idx);
			curClient = registeredClients.get(curClientID);
			for(String neighbourID : adjacencyList.get(curClientID)) {
				neighbour = registeredClients.get(neighbourID);
				curClient.addNeighbour(neighbour);
			}
		}
		
		//set documents
		int m;
		for(idx = 0; idx < storedDocNames.size(); ++idx) {
			curClientID = base + String.valueOf(idx);
			curClient = registeredClients.get(curClientID);
			for(String docName : storedDocNames.get(curClientID)) {
				try {
					m = curClient.getMaxNumberOfDocuments();
					curClient.setMaxNumberOfDocuments(MAX_CLIENT_NUM_DOCS);
					curClient.storeDocument(new Document(docName), null);
					curClient.setMaxNumberOfDocuments(m);
					
				} catch(CANException e) {
					throw new CANCommandFailed(-1, "Unable to load CAN structure; no storage space left");
				}
			}
		}
	}

	public void checkCANStructure(int cmdNo, Map<String, Set<String>> expAdjacencyList, Map<String, Set<String>> expStoredDocuments) throws CANCommandFailed {
		//clients must store exactly the expected documents and must have rejected the expected documents for which no storage space has been available
		if(!(expStoredDocuments.equals(clientID2StoredDocNames)))// && rejectedDocuments.equals(clientID2RejectedDocNames)))
			throw new CANCommandFailed(cmdNo, "test - the client to document mapping is not correct");
		
		//the neighbours of any client must match its expected adjacency list 
		for(ClientTestWrapper c : registeredClients.values()) {
			if(!expAdjacencyList.containsKey(c.getUniqueID()))
				throw new CANCommandFailed(cmdNo, "test - the CAN neighbourhood ist not correct");
			Set<String> expNeighbours = expAdjacencyList.get(c.getUniqueID());
			
			Set<String> actNeighbours = new HashSet<String>();
			for(ClientInterface n : c.getNeighbours()) {
				actNeighbours.add(n.getUniqueID());
			}
			
			if(!expNeighbours.equals(actNeighbours))
				throw new CANCommandFailed(cmdNo, "test - the CAN neighbourhood ist not correct");
		}
		
		//searches are only allowed to progress from neighbour to neighbour
		for(Search search : searches) {
			String docName = search.documentName;
			
			Iterator<Client> it = search.searchTrace.iterator();
			
			//empty searches are not possible - at least the client invoking the search must be present
			if(!it.hasNext())
				throw new CANCommandFailed(cmdNo, "test - an empty search exists (not possible)");
			
			Client firstClient = it.next();
			Client curClient = firstClient;
			Client nextClient = null;
			
			for(; it.hasNext(); curClient = nextClient) {
				nextClient = it.next();
				
				//a client can loop on itself (necessary for recursive function calls)
				if(nextClient == curClient) {
					//pass
				}
				//check if the next client is a neighbour
				else if(nextClient != null && expAdjacencyList.get(curClient.getUniqueID()).contains(nextClient.getUniqueID())) {
					//pass
				}
				//the current client has been a candidate to store the doc but does not store it
				else if(nextClient == null && docName != null){
					//restart search by rehashing
					curClient = firstClient;
					if(it.hasNext()) {
						nextClient = it.next();
						if(nextClient != curClient && !expAdjacencyList.get(curClient.getUniqueID()).contains(nextClient.getUniqueID()))
							throw new CANCommandFailed(cmdNo, "test - after a rehashing, the next client is not adjacent to the first client");
					}
				}
				//fail otherwise
				else
					throw new CANCommandFailed(cmdNo, "test - the search visited a client not adjacent to the previous one");;
			}
			
			//is the document expected to be stored in the network?
			if(docName != null) {
				boolean shouldBeStored = false;
				for(Set<String> docNames : expStoredDocuments.values()) {
					if(docNames.contains(docName)) {
						shouldBeStored = true;
						break;
					}
				}
				//if the searched document is expected to be stored in the network,
				if(shouldBeStored
				   // check if the last client stores the document otherwise fails (in the network and as expected)
				   && (curClient == null ||
				       !clientID2StoredDocNames.get(curClient.getUniqueID()).contains(docName) ||
				       !expStoredDocuments.get(curClient.getUniqueID()).contains(docName))) {
					throw new CANCommandFailed(cmdNo, "test - the last client in the search is not expected to store the retrieved document");
				}
			}
		}
	}
	
	public String dumpCANStructure() {
		return dumpCANStructure(false);
	}
	
	public String dumpCANStructure(boolean forLoading) {
		StringBuffer buf = new StringBuffer();
		
		if(!forLoading)
			buf.append("begin_test_can\n");
		else
			buf.append("begin_load_can\n");
		
		//Dump network structure
		for(ClientTestWrapper c : registeredClients.values()) {
			buf.append("    ");
			buf.append(c.getUniqueID());
			
			if(forLoading)
				if(child2Parent.containsKey(c.getUniqueID()))
					buf.append(" <").append(child2Parent.get(c.getUniqueID()))
					   .append(", ").append(c.getMaxNumberOfDocuments()).append(">");
				else
					buf.append(" <null, ").append(c.getMaxNumberOfDocuments()).append(">");
			buf.append(": ");
			
			boolean first = true;
			for(ClientInterface n : c.getNeighbours()) {
				if(!first)
					buf.append(", ");
				buf.append(n.getUniqueID());
				first = false;
			}
			buf.append("\n");
		}
		
		//Dump stored documents
		buf.append("  --- stored documents ---\n");
		for(Entry<String, Set<String>> e : clientID2StoredDocNames.entrySet()) {
			buf.append("    ");
			buf.append(e.getKey()).append(": ");
			
			boolean first = true;
			for(String name : e.getValue()) {
				if(!first)
					buf.append(", ");
				buf.append(name);
				first = false;
			}
			buf.append("\n");
		}
		if(!forLoading)
			buf.append("end_test_can");
		else
			buf.append("end_load_can");
		
		return buf.toString();
	}
	
	public void testLastSearchTrace(int cmdNo, List<String> clientIDTrace) throws CANCommandFailed {
		if(searches.size() == 0)
			throw new CANCommandFailed(cmdNo, "test - a search test is requested but no immediately preceding search has been conducted");
		
		clientIDTrace = compressClientIDSearchTrace(clientIDTrace);
		
		Search lastSearch = searches.get(searches.size() - 1);
		Iterator<String> expTraceIt = clientIDTrace.iterator();
		String expClientID;
		int i=0;
		for(Client c : compressSearchTrace(lastSearch.searchTrace)) {
			if(!expTraceIt.hasNext())
				throw new CANCommandFailed(cmdNo, "test - the recorded search trace is longer than the expected one");
			
			expClientID = expTraceIt.next();
			if((expClientID.equals("null") && c != null) || (c != null && !c.getUniqueID().equals(expClientID)))
				throw new CANCommandFailed(cmdNo, "test - the recorded and the expected search trace differ at position " + String.valueOf(i));
			i+=1;
		}
		
		if(expTraceIt.hasNext())
			throw new CANCommandFailed(cmdNo, "test - the expected search trace is longer than the recorded one");
	}
	
	private List<String> compressClientIDSearchTrace(List<String> searchTrace) {
		List<String> compressed = new LinkedList<String>();
		String last = null, first = searchTrace.get(0);
		
		for(String c : searchTrace) {
			//ensure that after a rehashing the trace starts again with the first (searching) client
			if(last == null) {
				last = first;
				compressed.add(first);
			}
			
			if(!last.equals(c)) {
				last = c;
				compressed.add(c);
			}
		}
		return compressed;
	}
	
	private List<Client> compressSearchTrace(List<Client> searchTrace) {
		List<Client> compressed = new LinkedList<Client>();
		Client last = null, first = searchTrace.get(0);
		
		for(Client c : searchTrace) {
			//ensure that after a rehashing the trace starts again with the first (searching) client
			if(last == null) {
				last = first;
				compressed.add(first);
			}
			
			if(last != c) {
				last = c;
				compressed.add(c);
			}
		}
		return compressed;
	}
	
	public String dumpLastSearch() {
		StringBuffer buf = new StringBuffer();
		Search lastSearch = searches.get(searches.size() - 1);
		buf.append("test_search");
		for(Client c : compressSearchTrace(lastSearch.searchTrace)) {
			buf.append(' ');
			if(c == null)
				buf.append("null");
			else
				buf.append(c.getUniqueID());
		}
		buf.append("\n");
		
		return buf.toString();
	}

	public void addStoredDocument(String uniqueID, String name) {
		if(clientID2StoredDocNames.containsKey(uniqueID))
			clientID2StoredDocNames.get(uniqueID).add(name);
		else {
			HashSet<String> rdn = new HashSet<String>();
			clientID2StoredDocNames.put(uniqueID, rdn);
			rdn.add(name);
		}
	}

	public void removeStoredDocument(String uniqueID, String documentName) {
		if(clientID2StoredDocNames.containsKey(uniqueID))
			clientID2StoredDocNames.get(uniqueID).remove(documentName);
	}

	public void updateStoredDocuments(ClientInterface parent, ClientTestWrapper joinedClient) {
		List<String> toDelete = new LinkedList<String>();
		for(String documentName : clientID2StoredDocNames.get(parent.getUniqueID())) {
			try {
				parent.getDocument(documentName);
			} catch (NoSuchDocument e) {
				toDelete.add(documentName);
			}
		}
		
		for(String name : toDelete)
			clientID2StoredDocNames.get(parent.getUniqueID()).remove(name);
		
		for(String name : toDelete)
			clientID2StoredDocNames.get(joinedClient.getUniqueID()).add(name);
	}
}
