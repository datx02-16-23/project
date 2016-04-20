package application.visualization;

import application.gui.Main;
import application.model.iModel;
import application.visualization.render2d.*;
import application.visualization.render2d.MatrixRender.Order;
import javafx.scene.layout.GridPane;
import wrapper.datastructures.DataStructure;

public class Visualization extends GridPane {

    private final iModel model;

    public Visualization (iModel model){
        this.model = model;
        this.setStyle("-fx-background-color: white ;");
    }
    
    public void clear(){
      getChildren().clear();
    }

    public void clearAndCreateVisuals (){
        getChildren().clear();
        int regular = 0;
        int independent = 0;
        for (DataStructure struct : model.getStructures().values()) {
//            if (struct.rawType == "independentElement") {
//                Render render = new IndependentElementRender(struct);
//                this.add(render, 1, independent++);
//                continue;
//            }
            Render render = resolveRender(struct);
            if (render != null) {
                this.add(render, 0, regular++);
                continue;
            }
        }
    }

    /**
     * Determines the model to use for this DataStructure. Will iteratively
     * 
     * @param struct The DataStructure to assign a Render to.
     */
    private Render resolveRender (DataStructure struct){
        Render render = null;
        String visual = struct.visual == null ? "NULL" : struct.visual;
        outer: for (int attempt = 1; attempt < 3; attempt++) {
            switch (visual) {
                case "bar":
                    render = new BarRender(struct, 50, 1, 5, 25);
                    break outer;
                case "box":
                    render = new MatrixRender(struct, Order.COLUMN_MAJOR, 50, 50, 50, 0);
                    break outer;
                case "tree":
                    render = new KTreeRender(struct, struct.visualOptions, 50, 50, 20, 10);
                    break outer;
                default:
                    /*
                     * Visual null or unknown.
                     */
                    switch (attempt) {
                        case 0: //Fallback 1
                            visual = struct.getAbstractVisual();
                            break;
                        case 1: //Fallback 2
                            visual = struct.getRawVisual();
                            break;
                        default:
                            Main.console.err("Unable to determine Visual style for: " + struct);
                            return null;
                    }
                    break;
            }
        }
        return render;
    }

    /**
     * Should be called whenever model is updated, does a complete rerender of the structures.
     */
    public void render (){
        Render render;
        for (Object o : getChildren()) {
            render = (Render) o;
            render.setPrefSize(this.getWidth(), this.getHeight());
            render.render();
        }
    }
}