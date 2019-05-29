package ads1.ss14.can.exceptions;

@SuppressWarnings("serial")
public class InvalidClient extends CANException {
	public InvalidClient(String reason) {
		super(reason);
	}
}
