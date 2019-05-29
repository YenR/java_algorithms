package ads1.ss14.can;

/**
 * Stores a 2-tuple of arbitrary type.
 * 
 * @param <F> the type of the Pair.first member variable
 * @param <S> the type of the Pair.second member variable
 */
public class Pair<F, S> {
	public F first;
	public S second;
	
	/**
	 * Constructs a new Pair.
	 * @param first the value for the first element of the Pair
	 * @param second the value for the second element of the Pair
	 */
	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}
}
