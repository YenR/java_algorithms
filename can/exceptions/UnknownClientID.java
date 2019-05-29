package ads1.ss14.can.exceptions;

@SuppressWarnings("serial")
public class UnknownClientID extends CANException {
	public UnknownClientID(String clientID) {
		super("The client " + clientID + " is not known");
	}
}
