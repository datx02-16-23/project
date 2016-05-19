package draw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import contract.datastructure.DataStructure;
import draw.GridRender.Order;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

/**
 * SpinnerValueFactory for Render implementations.
 * 
 * @author Richard Sundqvist
 *
 */
public class RenderSVF extends SpinnerValueFactory<Integer> {

	// Mode variable
	private final boolean explicit;
	/**
	 * Used to cycle through explicit values.
	 */
	private final ArrayList<Integer> values = new ArrayList<>();
	/**
	 * Used to cycle through ranges.
	 */
	private final int min;
	private final int max;
	/**
	 * The current spinner value.
	 */
	private int current;

	/**
	 * Creates a new RenderSVF with the specified min and max value.
	 * Increments will occur in steps of one. Rollover is applied, so
	 * {@code max + 1 = min} and vice versa.
	 * 
	 * @param min
	 *            The minimum value.
	 * @param max
	 *            The maximum value.
	 */
	public RenderSVF(int min, int max) {
		this.min = min;
		this.max = max;
		current = min;
		setConverter(new Converter());
		for (int i = min; i <= max; i++) {
			values.add(new Integer(i));
		}
		setValue(current);
		explicit = false;
	}

	/**
	 * Creates a new RenderSVF with the specified values and userValues. The
	 * user values is what will be shown to the user when going through the
	 * options of the spinner.
	 * 
	 * @param values
	 *            The keys for this RenderSpinner.
	 * @param userValues
	 *            Their display values.
	 */
	public RenderSVF(List<Integer> values, List<String> userValues) {
		min = -1;
		max = -1;
		setConverter(new Converter(values, userValues));
		for (int i = 0; i < values.size(); i++) {
			this.values.add(values.get(i));
		}
		current = values.get(0);
		setValue(current);
		explicit = true;
	}

	@Override
	public void decrement(int steps) {
		if (explicit) {
			current = current - steps < 0 ? values.size() - 1 : current - steps;
		} else {
			current = current - steps < min ? max : current - steps;
		}
		setValue(current);
	}

	@Override
	public void increment(int steps) {
		if (explicit) {
			current = current + steps > values.size() - 1 ? 0 : current + steps;
		} else {
			current = current + steps > max ? min : current + steps;
		}
		setValue(current);
	}

	public String toString() {
		if (explicit) {
			return "Explicit. Values =  " + values;
		} else {
			return "Non-explict. Range = [" + min + ", " + max + "]";
		}
	}

	/**
	 * Converter for the SpinnerSVF class.
	 * 
	 * @author Richard Sundqvist
	 *
	 */
	public static class Converter extends StringConverter<Integer> {

		private final HashMap<Integer, String> conversion;
		private final boolean explicit;

		public Converter(List<Integer> values, List<String> userValues) {
			conversion = new HashMap<Integer, String>();
			for (int i = 0; i < values.size(); i++) {
				conversion.put(values.get(i), userValues.get(i));
			}
			explicit = true;
		}

		public Converter() {
			conversion = null;
			explicit = false;
		}

		@Override
		public String toString(Integer integer) {
			if (explicit) {
				return conversion.get(integer);
			} else {
				return integer.toString();
			}
		}

		@Override
		public Integer fromString(String string) {
			if (explicit) {
				Integer ans = null;
				for (Integer i : conversion.keySet()) {
					if (conversion.get(i).equals(string)) {
						ans = i;
						break;
					}
				}
				return ans;
			} else {
				return Integer.parseInt(string);
			}
		}
	}

	public static RenderSVF resolve(DataStructure struct) {
		RenderSVF svf = null;

		switch (struct.resolveVisual()) {
		case bar:
			svf = null;
			break;

		case box:
			ArrayList<Integer> values = new ArrayList<Integer>();
			values.add(Order.ROW_MAJOR.optionNbr);
			values.add(Order.COLUMN_MAJOR.optionNbr);
			ArrayList<String> userValues = new ArrayList<String>();
			userValues.add(Order.ROW_MAJOR.name);
			userValues.add(Order.COLUMN_MAJOR.name);
			svf = new RenderSVF(values, userValues);
			break;
		case single:
			svf = null;
			break;

		case tree:
			svf = new RenderSVF(2, 1337);
			break;

		default:
			break;

		}
		return svf;
	}
}