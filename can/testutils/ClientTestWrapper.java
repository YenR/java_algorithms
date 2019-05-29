package ads1.ss14.can.testutils;

import ads1.ss14.can.Client;
import ads1.ss14.can.ClientInterface;
import ads1.ss14.can.Document;
import ads1.ss14.can.Position;
import ads1.ss14.can.exceptions.CANException;
import ads1.ss14.can.exceptions.DuplicateClientID;
import ads1.ss14.can.exceptions.InvalidDocument;
import ads1.ss14.can.exceptions.NoAdditionalStorageAvailable;
import ads1.ss14.can.exceptions.NoSuchDocument;

public class ClientTestWrapper extends Client {
	private NetworkRegistry registry;
	
	public ClientTestWrapper(String uniqueID, int networkXSize, int networkYSize, NetworkRegistry registry) {
		super(uniqueID, networkXSize, networkYSize);
		this.registry = registry;
	}
	
	/*** Storage related methods ***/
	
	@Override
	public Document getDocument(String documentName) throws NoSuchDocument {
		try {
			registry.addVisitedClient(this);
			Document d = super.getDocument(documentName);
			return d;
		} catch(NoSuchDocument e) {
			// add a delimiter to mark an unsuccessful search
			registry.addVisitedClient(null);
			throw e;
		}
	}
	
	@Override
	public void deleteDocument(String documentName) throws NoSuchDocument {
		try {
			super.deleteDocument(documentName);
			registry.removeStoredDocument(getUniqueID(), documentName);
		} catch(NoSuchDocument e) {
			//registry.removeRejectedDocument(getUniqueID(), documentName);
			throw e;
		}
	}
	
	@Override
	public void storeDocument(Document document, Position p) throws NoAdditionalStorageAvailable, CANException {
		super.storeDocument(document, p);
		registry.addStoredDocument(getUniqueID(), document.getName());
	}

	/*** Network related methods ***/
	
	@Override
	public ClientInterface searchForResponsibleClient(Position p) {
		registry.addVisitedClient(this);
		ClientInterface c = super.searchForResponsibleClient(p);
		return c;
	}

	@Override
	public ClientInterface joinNetwork(ClientInterface entryPoint, Position p) throws CANException {
		registry.resetCANHistory();
		try {
			registry.registerNewClient(this, p);
		} catch (DuplicateClientID e) {
			//SHOULD NEVER HAPPEN!! (only 1 Client can be the first)
			e.printStackTrace();
		}
		ClientInterface parent = super.joinNetwork(entryPoint, p);
		registry.setParentOf(this, parent);
		if(parent != null) {
			registry.updateStoredDocuments(parent, this);
		//	registry.updateRejectedDocuments(parent, this);
		}
		return parent;
	}
	
	/*** Command related methods 
	 * @throws InvalidDocument ***/
	
	@Override
	public void addDocumentToNetwork(Document d) throws CANException {
		//this command potentially modifies the network structure therefore a reset is required
		registry.resetCANHistory();
		super.addDocumentToNetwork(d);
	}
	
	@Override
	public void removeDocumentFromNetwork(String documentName) {
		//this command potentially modifies the network structure therefore a reset is required
		registry.resetCANHistory();
		super.removeDocumentFromNetwork(documentName);
	}
	
	@Override
	public Document searchForDocument(String documentName) throws CANException {
		registry.beginSearch(documentName);
		registry.addVisitedClient(this);
		Document d = super.searchForDocument(documentName);
		registry.endSearch();
		return d;
	}
}
