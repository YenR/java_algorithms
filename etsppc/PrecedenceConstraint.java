package ads2.ss14.etsppc;

public class PrecedenceConstraint {
	private int first;
	private int second;
	
	public PrecedenceConstraint(int first, int second) {
		this.first = first;
		this.second = second;
	}
	
	public PrecedenceConstraint(PrecedenceConstraint other) {
		first = other.first;
		second = other.second;
	}
	
	public int getFirst() {
		return first;
	}
	
	public int getSecond() {
		return second;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj.getClass() == PrecedenceConstraint.class) {
			PrecedenceConstraint other = (PrecedenceConstraint) obj;
			return first == other.first && second == other.second;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return 10*first + second;
	}
}
