package draw.render.elements;


	/**
	 * Shape enumeration.
	 * 
	 * @author Richard Sundqvist
	 *
	 */
	public enum ElementShape {
		ELLIPSE, CIRCLE, RECTANGLE, SQUARE, TRAPEZOID, TRIANGLE;

		final double[] points;

		private ElementShape() {
			points = null;
		}

		private ElementShape(double[] points) {
			this.points = points;
		}

	}