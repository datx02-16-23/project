package assets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import contract.datastructure.Array.IndexedElement;
import contract.datastructure.DataStructure;
import contract.datastructure.Element;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import render.ARender;
import render.Visualization;
import render.assets.ARenderManager;
import render.assets.HintPane;
import render.element.AVElement;

/**
 * Utility class to reduce clutter.
 *
 * @author Richard Sundqvist
 *
 */
public abstract class Tools {

    private Tools () {
    } // Not to be instantiated.

    // A FXML pane showing user instructions.
    public static final HintPane HINT_PANE = new HintPane();

    /*
     * Render base stuff
     */

    public static Background createArrayBg () {
        Image image = new Image(ARender.class.getResourceAsStream("/assets/array.png"));
        BackgroundImage bgi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
        return new Background(bgi);

    }

    public static Background createTreeBg () {
        Image image = new Image(ARender.class.getResourceAsStream("/assets/tree.png"));
        BackgroundImage bgi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
        return new Background(bgi);
    }

    public static Background createOrphanBg () {
        Image image = new Image(ARender.class.getResourceAsStream("/assets/orphan.png"));
        BackgroundImage bgi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
        return new Background(bgi);
    }

    public static Border getMOBorder () {
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
     * Memoized function. Calculates the total number of elements above a given depth and
     * saves it to higherLevelSums.
     *
     * @param targetDepth
     *            The greatest depth to calculate for.
     * @param K
     *            The number of children per node in the tree.
     * @return The total number of elements above {@code targetDepth} for a K-ary tree.
     */
    public static int lowerLevelSum (int targetDepth, int K) {
        ArrayList<Integer> sums = Tools.lowerLevelSums.get(K);

        if (sums == null) {
            // Start a new list.
            sums = new ArrayList<Integer>();
            sums.add(new Integer(0));
            Tools.lowerLevelSums.put(K, sums);
        }

        int cDepth = sums.size();
        for (; cDepth <= targetDepth; cDepth++) {
            int prev = sums.get(cDepth - 1);
            int cur = pow(K, cDepth - 1);
            sums.add(new Integer(prev + cur));
        }

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
    public static int pow (int b, int e) {
        if (e == 0) {
            return 1;
        } else if (b == 1) {
            return 1;
        }

        int p = 1;
        for (int i = 1; i <= e; i++) {
            p = p * b;
        }
        return p;
    }

    /**
     * Tries to simplify the variable name. For example,
     * {@code "package.subpackage.class:var"} becomes {@code "var"}.
     *
     * @param orig
     *            A string to simplify.
     * @return A simplified variable name (hopefully).
     */
    public static String stripQualifiers (String orig) {
        String a[] = orig.split("\\p{Punct}");
        a = orig.split(" ");
        return a [a.length - 1];
    }

    public static Background getRawTypeBackground (DataStructure struct) {
        if (struct == null) {
            return null;
        }

        switch (struct.rawType) {
        case array:
            return render.assets.Const.ARRAY_BACKGROUND;
        case tree:
            return render.assets.Const.TREE_BACKGROUND;
        case independentElement:
            return render.assets.Const.ORPHAN_BACKGROUND;
        }

        return null;
    }

    public static void markElementXY (Visualization vis) {
        for (ARenderManager rm : vis.getManagers()) {
            ARender r = rm.getRender();
            for (Element e : r.getDataStructure().getElements()) {
                double x = r.getX(e);
                double y = r.getY(e);
                double width = r.getPrefWidth();
                double height = r.getPrefHeight();

                Line lineX = new Line(x, 0, x, height);
                lineX.setStroke(Color.HOTPINK);
                lineX.setOpacity(0.5);

                Line lineY = new Line(0, y, width, y);
                lineY.setStroke(Color.HOTPINK);
                lineY.setOpacity(0.5);

                r.getNodes().getChildren().addAll(lineX, lineY, new Circle(x, y, 5));
            }
        }
    }

    public static double getAdjustedX (ARender render, Element e) {
        AVElement ave = render.getVisualMap().get(Arrays.toString(((IndexedElement) e).getIndex()));
        if (ave != null) {
            return (render.getNodeWidth() - ave.width) / 2;
        }
        return 0;
    }

    public static double getAdjustedY (ARender render, Element e) {
        AVElement ave = render.getVisualMap().get(Arrays.toString(((IndexedElement) e).getIndex()));
        if (ave != null) {
            return (render.getNodeHeight() - ave.height) / 2;
        }
        return 0;
    }
}
