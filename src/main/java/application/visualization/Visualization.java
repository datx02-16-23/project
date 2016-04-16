package application.visualization;

import application.gui.Main;
import application.model.iModel;
import application.visualization.render2d.BarchartRender;
import application.visualization.render2d.BoxRender;
import application.visualization.render2d.IndependentElementRender;
import application.visualization.render2d.Render;
import javafx.scene.layout.GridPane;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.IndependentElement;

public class Visualization extends GridPane {

    private final iModel model;

    public Visualization (iModel model){
        this.model = model;
        this.setStyle("-fx-background-color: white ;");
    }

    public void createVisuals (){
        getChildren().clear();
        int regular = 0;
        int independent = 0;
        for (DataStructure struct : model.getStructures().values()) {
            if(struct.rawType == "independentElement"){
                Render render = new IndependentElementRender(struct);
                this.add(render, 1, independent++);
                continue;
            }
            Render render = getRender(struct);
            if(render != null){
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
    private Render getRender (DataStructure struct){
        Render render = null;
        String visual = struct.visual == null ? "" : struct.visual;
        outer: for (int attempt = 1; attempt <= 3; attempt++) {
            switch (visual) {
                case "bar":
                    render = new BarchartRender(struct);
                    break outer;
                case "box":
                    render = new BoxRender(struct);
                    break outer;
                default:
                    /*
                     * Visual null or unknown.
                     */
                    switch (attempt) {
                        case 1: //Fallback 1
                            visual = struct.getAbstractVisual();
                            break;
                        case 2: //Fallback 2
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

    /*
     * @Override protected void layoutChildren (){ final int top = (int) snappedTopInset(); final int right = (int)
     * snappedRightInset(); final int bottom = (int) snappedBottomInset(); final int left = (int) snappedLeftInset();
     * final int w = (int) getWidth() - left - right; final int h = (int) getHeight() - top - bottom;
     * canvas.setLayoutX(left); canvas.setLayoutY(top); if (w != canvas.getWidth() || h != canvas.getHeight()) {
     * canvas.setWidth(w); canvas.setHeight(h); } render(); } private void clear (){ GraphicsContext g =
     * canvas.getGraphicsContext2D(); g.setFill(Color.WHITE); g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight()); }
     */
    /**
     * Should be called whenever model is updated, does a complete rerender of the structures.
     */
    public void render (){
        Render render;
        for(Object o : getChildren()){
            render = (Render) o;
            render.setPrefSize(this.getWidth(), this.getHeight());
            render.render();
        }
    }

    private String generateStructHeader (String id, DataStructure struct){
        StringBuilder sB = new StringBuilder();
        sB.append("Identifier: ");
        sB.append(id);
        sB.append("\t");
        sB.append("Type: ");
        sB.append(struct.rawType);
        return sB.toString();
    }

    private void drawIndependentElement (IndependentElement element){
        //        System.out.println("Drawing independent element");
    }

    private void renderStructure (String id, DataStructure struct, int x, int y){
        /*
         * int width = (int) canvas.getWidth(); int height = Consts.structHeight; final Class<? extends DataStructure>
         * structClass = struct.getClass(); final Operation op = model.getCurrentStep().getLastOp(); if
         * (structClass.equals(Array.class)) { ArrayRender arrayRender = new ArrayRender(canvas, id, struct, op, x, y,
         * width, height); arrayRender.render(); } else if (structClass.equals(IndependentElement.class)) {
         * drawIndependentElement((IndependentElement) struct); }
         */
    }
}