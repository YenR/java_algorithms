package ads1.ss14.can;

/**
 * Represents point in the the network.
 */
public final class Position {
	private double xCoord;
	private double yCoord;
	
	/**
	 * Constructs a new Position at the given point.
	 * @param x the coordinate on the X-Axis
	 * @param y the coordinate on the Y-Axis
	 */
	public Position(double x, double y) {
		xCoord = x;
		yCoord = y;
	}
	
	/**
	 * @return the coordinate on the X-Axis
	 */
	public double getX() {
		return xCoord;
	}
	
	/**
	 * @return the coordinate on the Y-Axis
	 */
	public double getY() {
		return yCoord;
	}
	
	/**
	 * Checks if two Positions are equal i.e. possess the same coordinates
	 * @param b the compared Position
	 * @return
	 */
	public boolean equals(Position b) {
		return b != null && xCoord == b.getX() && yCoord == b.getY();
	}
}
