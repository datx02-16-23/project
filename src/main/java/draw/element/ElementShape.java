package draw.element;

/**
 * Shape enumeration.
 * 
 * @author Richard Sundqvist
 *
 */
public enum ElementShape {
	ELLIPSE, CIRCLE, RECTANGLE, BAR_ELEMENT, SQUARE, TRAPEZOID(true, true), TRIANGLE(true, true),
	/**
	 * Used for cloning. Will randomly pick {@link #TRAPEZOID} or
	 * {@link #TRIANGLE} if no points are provided.
	 */
	POLYGON(false, false),
	/**
	 * A random style.
	 */
	RANDOM(false, false), SINGLE;

	/**
	 * 	Shapes may be selected when picking polygons at random, if true.
	 */
	private final boolean randomPolygon;
	/**
	 * Shapes may be selected when picking at random, if true.
	 */
	private final boolean random;

	private ElementShape() {
		randomPolygon = false;
		random = true;
	}

	private ElementShape(boolean randomPolygon, boolean random) {
		this.randomPolygon = randomPolygon;
		this.random = random;
	}

	/**
	 * Returns a ElementStyle at random excluding {@link ElementShape#RANDOM}
	 * and {@link ElementShape#POLYGON}.
	 * 
	 * @return A random ElementStyle.
	 */
	public static ElementShape random() {
		ElementShape random = null;

		do {
			random = values()[(int) Math.random() * values().length];
		} while (random == null || random.random == false);

		return random;
	}

	/**
	 * Returns a random polygon style.
	 * 
	 * @return A random ElementStyle of style polygon.
	 */
	public static ElementShape randomPolygon() {
		ElementShape[] polys = new ElementShape[values().length];

		int count = 0;
		for (ElementShape es : values()) {
			if (es.randomPolygon) {
				polys[count] = es;
				count++;
			}
		}

		return polys[(int) (Math.random() * count)];
	}
}