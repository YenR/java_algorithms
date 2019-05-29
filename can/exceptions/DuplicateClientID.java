package ads1.ss14.can.exceptions;

@SuppressWarnings("serial")
public class DuplicateClientID extends CANException {
	public DuplicateClientID(String uniqueID) {
		super(uniqueID + " already exists");
	}
}
