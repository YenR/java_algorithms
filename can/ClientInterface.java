package ads1.ss14.can;

import ads1.ss14.can.exceptions.CANException;
import ads1.ss14.can.exceptions.NoAdditionalStorageAvailable;
import ads1.ss14.can.exceptions.NoSuchDocument;

/**
 * ClientInterface describes all methods which any client in the network should be able to execute,
 * regardless of its implementation.
 */
public interface ClientInterface {
	/**
	 * @return the unique ID provided to the Client during construction
	 */
	public String getUniqueID();
	
	/*** Storage related methods ***/
	
	/**
	 * Sets the number of documents which the Client is able to store.
	 * @param m the maximum number of documents
	 */
	public void setMaxNumberOfDocuments(int m);
	
	/**
	 * @return the previously set max. number of storable documents or -1 if it has not been set
	 */
	public int getMaxNumberOfDocuments();
	
	/**
	 * Returns the requested Document if it is stored by the Client.
	 * @attention This method may only be called if the Client could(!) store the document 
	 * i.e. if the previously calculated hash values of the Document coincide with the Client's managed area in the CAN. 
	 *  
	 * @param documentName the name of the requested Document
	 * @return the requested Document if the Client holds a copy
	 * @throws NoSuchDocument if the Client does not hold the Document
	 */
	public Document getDocument(String documentName) throws NoSuchDocument;
	
	/**
	 * Stores the passed Document if storage space is available otherwise throws a NoAdditionalStorageAvailable exception.
	 * @param d the Document to store
	 * @param p the calculated Position of the document
	 * @throws NoAdditionalStorageAvailable if the number of storable Documents is exceeded
	 * @throws CANException used by the testing framework; you do not need to consider this
	 */
	public void storeDocument(Document d, Position p) throws NoAdditionalStorageAvailable, CANException;
	
	/**
	 * Deletes the document from the Client's storage if it holds a copy of the Document
	 * @param documentName the name of the Document to be deleted
	 * @throws NoSuchDocument if the Client does not hold a copy of the Document
	 */
	public void deleteDocument(String documentName) throws NoSuchDocument;
	
	/*** Network related methods ***/
	
	/**
	 * @return the Position of the Client in the CAN (see the "Aufgabenstellung" how this is calculated)
	 */
	public Position getPosition();
	
	/**
	 * @return the area managed by the Client
	 */
	public Area getArea();
	
	/**
	 * Sets the area managed by the Client.
	 * If the area is changed, then the position is adapted too.
	 */
	public void setArea(Area newArea);
	
	/**
	 * @attention It is not allowed to downcast the returned value!
	 * @return an Iterable e.g. a java.util.List<ClientInterface> storing all neighbours of the Client
	 */
	public Iterable<ClientInterface> getNeighbours();
	
	/**
	 * Adds the passed ClientInterface to the set of neighbours.
	 * @param newNeighbour the ClientInterface which shall be added as neighbour
	 */
	public void addNeighbour(ClientInterface newNeighbour);
	
	/**
	 * Removes the neighbour with the passed ID. Does nothing if no such neighbour exists
	 * @param clientID the ID of the neighbour to be removed
	 */
	public void removeNeighbour(String clientID);
	
	/**
	 * Searches for the client who is reponsible for the passed position i.e. whose area contains the passed position.
	 * @param p the requested Position
	 * @return the ClientInterface which area contains p
	 */
	public ClientInterface searchForResponsibleClient(Position p);
	
	/**
	 * Removes all stored documents from a client which are no longer contained in its area.
	 * This method is optional i.e. its functionality can be realised in a different way, an empty implementation might therefore be sufficient.
	 * @return some Iterable over all documents which are no longer contained in the client's area
	 */
	public Iterable<Pair<Document, Position>> removeUnmanagedDocuments();
	
	/**
	 * Updates the client's neighbourhood structure and possible the neighbourhood structure of the joining client too.
	 * This method is optional i.e. its functionality can be realised in a different way, an empty implementation might therefore be sufficient. 
	 * @param joiningClient the client who is currently joining the network
	 */
	public void adaptNeighbours(ClientInterface joiningClient);
}
