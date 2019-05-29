package ads1.ss14.can;

import ads1.ss14.can.exceptions.CANException;


/**
 * This interface describes the different high-level commands which a CAN Client is able to process
 * e.g. joining, addding/removing documents, searching for documents
 */
public interface ClientCommandInterface {

	/**
	 * The caller request to join the network.
	 * With the help of the passed client who acts as his entry point to the network the caller tries
	 * to find the responsible client C_p for its chosen position in the network.
	 * The caller then requests C_p to split its managed area with the caller.
	 * See the "Aufgabenstellung" for details.
	 * 
	 * @param entryPoint the only client known to the caller before joining the network 
	 * @param p the joining position of the caller 
	 * @return the client C_p with whom the caller splitted its managed area
	 * @throws CANException used by the testing frameowork; you do not need to consider this
	 */
	public abstract ClientInterface joinNetwork(ClientInterface entryPoint, Position p) throws CANException;

	/*** Command related methods ***/

	/**
	 * The caller adds the passed document to the network.
	 * The hash values are calculated for the document and the client C_r responsible for the calculated position in the CAN
	 * is retrieved. The caller tries to store the document in C_r. If this fails the document is rehashed
	 * and the process begins anew. The caller aborts the process if there is no more possibility to store
	 * the document in the network. 
	 * 
	 * @param d the document to be added to the network
	 * @throws CANException used by the testing frameowork; you do not need to consider this
	 */
	public abstract void addDocumentToNetwork(Document d) throws CANException;

	/**
	 * The caller removes the document with the passed name from the network.
	 * If the document is not stored in the network, nothing changes in the network.
	 * 
	 * @param docummentName
	 */
	public abstract void removeDocumentFromNetwork(String docummentName);

	/**
	 * The caller searches for the document with the passed name in the network.
	 *  
	 * @param documentName the name of the requested document
	 * @return the requested document or null (iff the document is not stored in the network)
	 * @throws CANException used by the testing frameowork; you do not need to consider this
	 */
	public abstract Document searchForDocument(String documentName) throws CANException;

}