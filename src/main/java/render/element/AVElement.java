package render.element;

import java.io.IOException;
import java.util.Arrays;

import assets.Debug;
import contract.datastructure.Element;
import contract.operation.OperationCounter.OperationCounterHaver;
import gui.Main;
import javafx.animation.RotateTransition;
import javafx.animation.StrokeTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

/**
 * A visualisation element. Elements which use the
 * {@code VisualElement(Element element, ..)} constructor are bound to their
 * elements. Using the {@code VisualElement(double value, Paint style, ..)}
 * allows the user to set values manually. Attempting to set values manually on
 * a bound element will cast an exception.
 *
 * @author Richard Sundqvist
 *
 */
public abstract class AVElement extends Pane {

    // Should always merge into purple (255, 0, 255)!!
    public static final String DEBUG_FXML_ROOT = "-fx-background-color: rgba(255, 0, 0, 0.3); \n -fx-opacity: 0.8;";
    public static final String DEBUG_FXML_THIS = "-fx-background-color: rgba(0, 0, 255, 0.3); \n -fx-opacity: 0.8;";
    public static final String URL             = "/render/FXMLElement.fxml";

    /**
     * Enum indicating the shape of this polygon. Used by the factory.
     */
    public ElementShape        elemShape;

    /**
     * Current info label position.
     */
    protected Pos              infoPos;

    /**
     * The element this VisualElement represents.
     */
    protected final Element    element;
    /**
     * Extra info label for stuff such as index.
     */
    protected final Label      infoLabel       = new Label();
    /*
     * FXML elements.
     */
    protected Shape            shape;

    protected Label            valueLabel;
    protected GridPane         root;

    /**
     * Bounding width of the node.
     */
    public double              width;
    /**
     * Bounding height of the node.
     */
    public double              height;

    /**
     * Points used by Polygons.
     */
    public final double[]      points;

    /**
     * Create a static, unbound VisualElement.
     *
     * @param value
     *            The initial value.
     * @param style
     *            The style to use.
     * @param node_width
     *            The width of the node.
     * @param node_height
     *            The height of the node. * @param fxmlUrl Path to the FXML file
     *            containing the layout for the element.
     */
    public AVElement (double value, Paint style, double node_width, double node_height) {
        this.element = null;
        this.points = null;
        this.init(node_width, node_height);

        this.valueLabel.setText("" + value);
        this.shape.setFill(style);
    }

    /**
     * Create a bound VisualElement.
     *
     * @param element
     *            The Element this VisualElement represents
     * @param node_width
     *            The width of the node.
     * @param node_height
     *            The height of the node.
     * @param fxmlUrl
     *            Path to the FXML file containing the layout for the element.
     */
    public AVElement (Element element, double node_width, double node_height) {
        this.element = element;
        this.points = null;
        this.init(node_width, node_height);

        // Automatic updating of value
        this.valueLabel.textProperty().bind(element.stringProperty);
        this.shape.fillProperty().bind(element.fillProperty);
    }

    /**
     * Create a static, unbound VisualElement.
     *
     * @param value
     *            The initial value.
     * @param style
     *            The style to use.
     * @param node_width
     *            The width of the node.
     * @param node_height
     *            The height of the node.
     */
    public AVElement (double value, Paint style, double node_width, double node_height, double[] points) {
        this.element = null;
        this.points = points;
        this.init(node_width, node_height);

        this.valueLabel.setText("" + value);
        this.shape.setFill(style);
    }

    /**
     * Create a bound VisualElement.
     *
     * @param element
     *            The Element this VisualElement represents
     * @param node_width
     *            The width of the node.
     * @param node_height
     *            The height of the node.
     */
    public AVElement (Element element, double node_width, double node_height, double[] points) {
        this.element = element;
        this.points = points;
        this.init(node_width, node_height);

        // Automatic updating of value
        this.valueLabel.textProperty().bind(element.stringProperty);
        this.shape.fillProperty().bind(element.fillProperty);
    }

    /**
     * Create a shape to use as the holder of the element value;
     */
    public void createShape () {
        this.root.setPrefSize(this.width, this.height);
    }

    private void init (double node_width, double node_height) {

        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource(URL));
        fxmlLoader.setController(this);

        try {
            this.root = (GridPane) fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        this.height = node_height;
        this.width = node_width;
        if (Debug.ERR) {
            this.root.setStyle(DEBUG_FXML_ROOT);
            this.setStyle(DEBUG_FXML_THIS);
        }

        // Container for the value
        Pane shapePane = (Pane) fxmlLoader.getNamespace().get("shape");
        shapePane.setCursor(Cursor.HAND);
        this.createShape();
        this.shape.setPickOnBounds(true);
        shapePane.setPickOnBounds(true);
        shapePane.getChildren().add(this.shape);

        this.valueLabel = (Label) fxmlLoader.getNamespace().get("value");

        this.infoLabel.setMouseTransparent(true);
        this.infoLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");
        this.root.getChildren().add(this.infoLabel);
        this.getChildren().add(this.root);
    }

    /**
     * Returns the element this VisualElement represents.
     *
     * @return The element this VisualElement represents.
     */
    public Element getElement () {
        return this.element;
    }

    /**
     * Returns the shape containing the element.
     *
     * @return The shape containing the element.
     */
    public Shape getElementShape () {
        return this.shape;
    }

    /**
     * Listener for the onMouseClicked event.
     */
    public void onMouseClicked () {
        this.showSelected();
        Main.console.force("w = " + this.width + ", h = " + this.height);
        Main.console.info("Statistics for \"" + this.element + "\":");
        OperationCounterHaver.printStats(this.element);
    }

    /**
     * Indicate to the user that the element has been clicked.
     */
    private void showSelected () {

        // Rotate.
        this.shape.setRotate(0);
        RotateTransition rotate = new RotateTransition(Duration.millis(150), this.shape);
        rotate.setFromAngle(-8);
        rotate.setToAngle(8);
        rotate.setCycleCount(6);
        rotate.setAutoReverse(true);
        rotate.setOnFinished(event -> {
            this.shape.setRotate(0);
        });
        rotate.play();

        // Border - cannot see rotation on circular elements :).
        this.shape.setStrokeWidth(5);
        StrokeTransition stroke = new StrokeTransition(Duration.millis(900), this.shape, Color.BLACK, Color.SKYBLUE);
        stroke.setOnFinished(event -> {
            this.shape.setStrokeWidth(1);
            this.shape.setStroke(Color.BLACK);
        });
        stroke.setAutoReverse(true);
        stroke.play();
    }

    /**
     * Listener for the onMouseEntered event.
     */
    public void onMouseEntered () {
        // if (Debug.ERR) {
        // root.setStyle(DEBUG_FXML_ROOT);
        // this.setStyle(DEBUG_FXML_THIS);
        // } else {
        // root.setStyle(null);
        // this.setStyle(null);
        // }

        this.root.setScaleX(1.20);
        this.root.setScaleY(1.20);
        this.toFront();
    }

    /**
     * Listener for the onMouseExited event.
     */
    public void onMouseExited () {
        this.root.setScaleX(1);
        this.root.setScaleY(1);
        this.shape.setStrokeWidth(1);
    }

    /**
     * Determines whether this element should be shown as a ghost, with no value
     * and a dashed border.
     *
     * @param ghost
     *            The new value.
     */
    public void setGhost (boolean ghost) {
        if (ghost != this.valueLabel.isVisible()) {
            return; // Ghost status not changed.
        }
        if (ghost) {
            this.setMouseTransparent(true);
            this.shape.fillProperty().unbind();
            this.shape.setFill(Color.TRANSPARENT);
            this.shape.getStrokeDashArray().addAll(5.0);
            this.valueLabel.setVisible(false);
        } else {
            this.setMouseTransparent(false);
            this.shape.fillProperty().bind(this.element.fillProperty);
            this.shape.getStrokeDashArray().clear();
            this.valueLabel.setVisible(true);
        }
    }

    /**
     * Unbind the element, leaving it in whatever state is is currently in.
     */
    public void unbind () {
        this.valueLabel.textProperty().unbind();
        this.shape.fillProperty().unbind();
    }

    /**
     * Show an array using the info label.
     *
     * @param array
     *            The array to show.
     */
    public void setInfoArray (int[] array) {
        this.setInfoText(Arrays.toString(array));
    }

    /**
     * Enables and disables visibility for the info label.
     *
     * @param visible
     *            The new visibility setting.
     */
    public void setInfoVisible (boolean visible) {
        this.infoLabel.setVisible(visible);
    }

    /**
     * Set text for the label. Will render at {@link Pos#BOTTOM_CENTER} if no
     * position is specified.
     *
     * @param text
     *            The text to render.
     */
    public void setInfoText (String text) {
        this.infoLabel.setText(text);
        this.setInfoVisible(true);
        this.setInfoPos(this.infoPos == null ? Pos.BOTTOM_CENTER : this.infoPos);
    }

    /**
     * Set the position of the extra info label. Will hide the label if
     * {@code pos == null}. <br>
     *
     * @param pos
     *            The new position for the info label.
     */
    @SuppressWarnings("incomplete-switch") public void setInfoPos (Pos pos) {
        this.infoPos = pos;

        if (this.infoPos == null) {
            this.setInfoVisible(false);
            return;
        } else {
            this.setInfoVisible(true);
        }
        String text = this.infoLabel.getText();

        if (text.length() == 0) {
            return;
        }

        Label tmp = new Label(text);

        new Scene(new Group(tmp));
        tmp.applyCss();
        final double textW = tmp.getLayoutBounds().getWidth();
        final double textH = tmp.getLayoutBounds().getHeight() / 2;

        double tx = 0;
        double ty = 0;

        switch (pos.getHpos()) {
        case LEFT:
            tx = -(this.width / 2 + textW);
            break;
        case CENTER:
            // tx already 0.
            break;
        case RIGHT:
            tx = this.width / 2 + textW;
            break;
        }

        switch (pos.getVpos()) {
        case BOTTOM:
            ty = this.height / 2 + textH;
            break;
        case CENTER:
            // ty already 0.
            break;
        case TOP:
            ty = -(this.height / 2 + textH);
            break;
        }

        this.infoLabel.setTranslateX(tx);
        this.infoLabel.setTranslateY(ty);
    }

    /**
     * Set the value of this element, applying it to the <b>visual
     * representation only</b>. The model is not changed.
     *
     * @param value
     *            A double value to show.
     */
    public void setValue (double value) {
        this.valueLabel.setText(" " + value + " ");
    }

    /**
     * Set the value of this element, applying it to the <b>visual
     * representation only</b>. The model is not changed.
     *
     * @param value
     *            A String value to show.
     */
    public void setValue (String value) {
        this.valueLabel.setText(" " + value + " ");
    }

    @Override public AVElement clone () {
        return AVElementFactory.clone(this);
    }

    /**
     * Adjust the size of the Shape used to display the value of this AVElement,
     * as well as the root. The default implementation of this method only
     * changes the size of the root.
     *
     * @param newWidth
     *            The new width.
     * @param newHeight
     *            The new height.
     */
    public void setSize (double newWidth, double newHeight) {
        if (newWidth != this.width) {
            // Repostion on render.
            this.setLayoutX(this.getLayoutX() + (this.width - newWidth) / 2);
            this.width = newWidth;
        }
        if (newHeight != this.height) {
            // Repostion on render.
            this.setLayoutY(this.getLayoutY() + (this.height - newHeight) / 2);
            this.height = newHeight;
        }
        this.root.setPrefSize(this.width, this.height);
    }
}
