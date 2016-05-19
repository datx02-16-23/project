package assets;

import java.util.ArrayList;
import java.util.HashMap;

import draw.ARender;
import gui.Visualization;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 * Utility class to reduce clutter.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class DasToolkit {

	private DasToolkit() {
	} // Not to be instantiated.

	// A FXML pane showing user instructions.
	public static final Visualization.HintPane HINT_PANE = new Visualization.HintPane();

	/*
	 * Render base stuff
	 */

	public static Background createArrayBg() {
		Image image = new Image(ARender.class.getResourceAsStream("/assets/array.png"));
		BackgroundImage bgi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
		return new Background(bgi);

	}

	public static Background createTreeBg() {
		Image image = new Image(ARender.class.getResourceAsStream("/assets/tree.png"));
		BackgroundImage bgi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
		return new Background(bgi);
	}

	public static Background createOrphanBg() {
		Image image = new Image(ARender.class.getResourceAsStream("/assets/orphan.png"));
		BackgroundImage bgi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
		return new Background(bgi);
	}

	public static Border getMOBorder() {
		return new Border(new BorderStroke(Color.web("#123456"), BorderStrokeStyle.SOLID, new CornerRadii(3),
				new BorderWidths(3), new Insets(-3)));
	}

	/*
	 * 
	 * KTreeRender stuff
	 * 
	 */
	/**
	 * Memoization for number of nodes.
	 */
	public static final HashMap<Integer, ArrayList<Integer>> lowerLevelSums = new HashMap<Integer, ArrayList<Integer>>();

	/**
	 * Memoized function. Calculates the total number of elements below a given
	 * depth and saves it to higherLevelSums.
	 * 
	 * @param targetDepth
	 *            The greatest depth to calculate for.
	 * @param K
	 *            The number of children per node in the tree.
	 * @return The total number of elements above {@code targetDepth} for a
	 *         K-ary tree.
	 */
	public static int lowerLevelSum(int targetDepth, int K) {
		if(K < 2){
			throw new IllegalArgumentException("Cannot calculate for K lower than 2!!");
		}
		ArrayList<Integer> sums = DasToolkit.lowerLevelSums.get(K);

		if (sums == null) {
			// Start a new list.
			System.out.println("NEW LIST: " + K);
			sums = new ArrayList<Integer>();
			sums.add(new Integer(0));
			DasToolkit.lowerLevelSums.put(K, sums);
		}

		int cDepth = sums.size();
		for (; cDepth <= targetDepth; cDepth++) {
			int prev = sums.get(cDepth - 1);
			int curr = DasToolkit.pow(cDepth + 1 , K);
			System.out.println("curr = " + curr);
			
			sums.add(new Integer(prev + curr));
		}
		System.out.println(K + " sums = " + sums);
		System.out.println("---------");
		return sums.get(targetDepth);
	}

	/**
	 * Calculate base^exp. No need for double as in java.lang.Math
	 * 
	 * @param b
	 *            The base.
	 * @param e
	 *            The exponent.
	 * @return base^x
	 */
	public static int pow(int b, int e) {
		int p = 1;
		for (int i = 0; i < e; i++) {
			p = p * b;
		}
		return p;
	}
}
