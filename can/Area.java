package ads1.ss14.can;

/**
 * Represents a rectangular area in the network.
 * 
 * Provides methods for checking containment and splitting of an Area.
 */
public final class Area {
	private double lowerX, upperX;
	private double lowerY, upperY;
	
	/**
	 * Constructs a new Area with the given measurements.
	 * 
	 * @param lowerX the starting point of the Area on the X-Axis
	 * @param upperX the end point (exclusive) of the Area on the X-Axis
	 * @param lowerY the starting point of the Area on the Y-Axis
	 * @param upperY the end point (exclusive) of the Area on the Y-Axis
	 */
	public Area(double lowerX, double upperX, double lowerY, double upperY) {
		assert lowerX < upperX;
		assert lowerY < upperY;
		this.lowerX = lowerX;
		this.upperX = upperX;
		
		this.lowerY = lowerY;
		this.upperY = upperY;
	}

	/**
	 * @return the starting point of the Area on the X-Axis
	 */
	public double getLowerX() {
		return lowerX;
	}

	/**
	 * @return the end point (exclusive) of the Area on the X-Axis
	 */
	public double getUpperX() {
		return upperX;
	}

	/**
	 * @return the starting point of the Area on the Y-Axis
	 */
	public double getLowerY() {
		return lowerY;
	}

	/**
	 * @return the end point (exclusive) of the Area on the Y-Axis
	 */
	public double getUpperY() {
		return upperY;
	}
	
	/**
	 * @param p
	 * @return true if the Position p is contained in the Area, false otherwise
	 */
	public boolean contains(Position p) {
		return (lowerX <= p.getX() && p.getX() < upperX) &&
			   (lowerY <= p.getY() && p.getY() < upperY);
	}
	
	/**
	 * Splits the Area vertically (normal to the X-Axis).
	 * Does not modify the calling Area object!
	 * 
	 * @return a Pair containing the left half as first and the right half as second element. 
	 */
	public Pair<Area, Area> splitVertically() {
		Area leftArea = new Area(getLowerX(),
				 getLowerX() + (getUpperX() - getLowerX())/2,
				 getLowerY(),
				 getUpperY());
		
		Area rightArea = new Area(getLowerX() + (getUpperX() - getLowerX())/2,
				  getUpperX(),
				  getLowerY(),
				  getUpperY());
		
		return new Pair<Area, Area>(leftArea, rightArea);
	}
	
	/**
	 * Splits the Area horizontally (normal to the Y-Axis).
	 * Does not modify the calling Area object!
	 * 
	 * @return a Pair containing the lower half as first and the upper half as second element. 
	 */
	public Pair<Area, Area> splitHorizontally() {
		Area lowerArea = new Area(getLowerX(),
				  getUpperX(),
				  getLowerY(),
				  getLowerY() + (getUpperY() - getLowerY())/2);
		
		Area upperArea = new Area(getLowerX(),
				  getUpperX(),
				  getLowerY() + (getUpperY() - getLowerY())/2,
				  getUpperY());
		
		return new Pair<Area, Area>(lowerArea, upperArea);
	}
}
