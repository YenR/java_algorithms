package ads1.ss14.can.exceptions;

@SuppressWarnings("serial")
public class CANCommandFailed extends CANException {
	public CANCommandFailed(int cmdNo, String cmd) {
		super(String.format("Command #%d failed (%s)", cmdNo, cmd));
	}
}
