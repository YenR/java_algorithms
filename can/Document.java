package ads1.ss14.can;

/**
 * Represents a document in the network.
 */
public class Document {
	private String name;
	
	/**
	 * Constructs a new Document with the given name.
	 * @param name the unique name of the Document
	 */
	public Document(String name) {
		this.name = name;
	}
	
	/**
	 * @return the unique name of the Document
	 */
	public final String getName() {
		return name;
	}
}
