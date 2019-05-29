package ads1.ss14.can.exceptions;

@SuppressWarnings("serial")
public class MalformedCANCommand extends CANException {
	public MalformedCANCommand(int lno, String line) {
		super(String.format("Error in command at line %d: %s", lno, line));
	}
}
