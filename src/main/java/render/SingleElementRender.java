package render;

import contract.datastructure.DataStructure;
import contract.datastructure.Element;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import render.element.AVElement;
import render.element.AVElementFactory;
import render.element.ElementShape;

public class SingleElementRender extends ARender {

    public static final ElementShape DEFAULT_ELEMENT_STYLE = ElementShape.SINGLE;


    // ============================================================= //
    /*
     *
     * Constructor
     *
     */
    // ============================================================= //
    
    /**
     * Create a new SingleElementRender.
     *
     * @param struct
     *            The structure to draw as a single element.
     * @param width
     *            The width of the nodes.
     * @param height
     *            The height of the nodes.
     */
    public SingleElementRender (DataStructure struct, double width, double height) {
        super(struct, width, height, 0, 0);
        renderWidth = 150;
        renderHeight = 150;
        this.setRelativeNodeSize(-1);
    }
    

    // ============================================================= //
    /*
     *
     * Superimplementations.
     *
     */
    // ============================================================= //

    @Override public void afterParentLoadFXML (FXMLLoader fxmlLoader) {
        // Make it a little smaller.
        String style = "-fx-font-size: 12; \n fx-font-weight: bold;";
        Label name2 = (Label) fxmlLoader.getNamespace().get("name_mo");
        name2.setStyle(style);
        name.setStyle(style);
    }

    @Override public void render () {
        if (struct.isRepaintAll()) {
            struct.setRepaintAll(false);
            repaintAll();
        }
        super.render();
    }

    @Override public boolean repaintAll () {
        if (super.repaintAll() == false) {
            return false;
        }
        return true;
    }

    @Override public void calculateSize () {
        renderWidth = 150;
        renderHeight = nodeHeight;
        setRestricedSize(renderWidth, renderHeight);
    }

    /**
     * This method always returns 0.
     *
     * @param e
     *            An element.
     * @return 0 regardless of e.
     */
    @Override public double getX (Element e) {
        return 0;
    }

    /**
     * This method always returns 0.
     *
     * @param e
     *            An element.
     * @return 0 regardless of e.
     */
    @Override public double getY (Element e) {
        return 0; // Always 0.
    }

    @Override protected AVElement createVisualElement (Element e) {
        AVElement re = AVElementFactory.shape(DEFAULT_ELEMENT_STYLE, e, nodeWidth, nodeHeight);
        return re;
    }

    @Override protected AVElement createVisualElement (double value, Color color) {
        AVElement re = AVElementFactory.shape(DEFAULT_ELEMENT_STYLE, value, color, nodeWidth, nodeHeight);
        re.setInfoPos(null);
        return re;
    }

    @Override protected void bellsAndWhistles (Element e, AVElement ve) {
        // System.out.println("single: bells shape = " + ve.getShape());
    }
}
