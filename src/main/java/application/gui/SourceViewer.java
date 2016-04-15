package application.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import wrapper.Header;
import wrapper.Operation;

/**
 * A SourceViewer
 * 
 * @author Richard
 *
 */
public class SourceViewer extends Pane {

    private final TabPane                  root;
    private final HashMap<String, Integer> nameTabMapping;

    /**
     * Create an empty SourceViewer()
     */
    public SourceViewer (){
        root = new TabPane();
        this.setStyle("-fx-background-color: blue;");
        nameTabMapping = new HashMap<String, Integer>();
        root.prefHeightProperty().bind(this.heightProperty());
        root.prefWidthProperty().bind(this.widthProperty());
        this.setPrefSize(200, 200);
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.getChildren().add(root);
    }

    /**
     * Try to add sources which are to be displayed by this SourceViewer.
     * 
     * @param header The sources to display.
     */
    public void trySources (Map<String, List<String>> sources){
        System.out.println("\ttry");
        if (sources == null) {
            System.out.println("\t\trySources null");
            return;
        }
        root.getTabs().clear();
        int tabNumber = 0;
        for (String sourceName : sources.keySet()) {
            addSourceTab(sourceName, sources.get(sourceName));
            nameTabMapping.put(sourceName, tabNumber);
            tabNumber++;
        }
        root.getSelectionModel().select(0);
    }

    private void addSourceTab (String sourceName, List<String> sourceLines){
        //Build new Tab
        Tab newTab = new Tab();
        newTab.setText(sourceName);
        //Build ListView
        ListView<String> linesView = new ListView<String>();
        linesView.setEditable(false);
        linesView.getItems().addAll(sourceLines);
        linesView.prefHeightProperty().bind(this.heightProperty());
        linesView.prefWidthProperty().bind(this.widthProperty());
        //Add children
        newTab.setContent(linesView);
        root.getTabs().add(newTab);
    }

    /**
     * Jump to the appropriate tab and line number for this Operation.
     * @param op The Operation to show.
     */
    public void show (Operation op){
        if(op == null){
            return;
        }
        System.out.println(nameTabMapping);
        System.out.println("op.source= " + op.source);
        Integer source = nameTabMapping.get(op.source);
        if (source == null) {
            Main.console.err("No source file given for Operation: " + op);
            return; //No source file given for this operation;
        }
        root.getSelectionModel().select(source);
        ListView<String> linesView = (ListView<String>) root.getTabs().get(nameTabMapping.get(op.source)).getContent();
        linesView.getSelectionModel().selectRange(op.beginLine, op.endLine);
        linesView.getFocusModel().focus(op.beginLine + 1);
    }
}
