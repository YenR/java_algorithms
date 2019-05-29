package ads1.ss14.can.exceptions;

@SuppressWarnings("serial")
public class InvalidDocument extends CANException {

	public InvalidDocument(String reason) {
		super(reason);
	}
	
}
