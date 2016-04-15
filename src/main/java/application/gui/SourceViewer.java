package application.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
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
        nameTabMapping = new HashMap<String, Integer>();
        root.prefHeightProperty().bind(this.heightProperty());
        root.prefWidthProperty().bind(this.widthProperty());
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        this.setPrefSize(200, 200);
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.getChildren().add(root);
        initTab();
    }

    /**
     * Try to add sources which are to be displayed by this SourceViewer.
     * 
     * @param header The sources to display.
     */
    public void trySources (Map<String, List<String>> sources){
        if (sources == null) {
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
     * 
     * @param op The Operation to show.
     */
    @SuppressWarnings("unchecked")
    public void show (Operation op){
        if (op == null) {
            return;
        }
        System.out.println("op.source= " + op.source);
        Integer sourceTabIndex = nameTabMapping.get(op.source);
        if (sourceTabIndex == null) {
            Main.console.err("No source file given for Operation: " + op);
            return; //No source file given for this operation;
        }
        //Get ListView
        root.getSelectionModel().select(sourceTabIndex.intValue());
        ListView<String> linesView = (ListView<String>) root.getTabs().get(nameTabMapping.get(op.source)).getContent();
        //Select lines
        linesView.getSelectionModel().clearSelection();
        linesView.getSelectionModel().selectRange(op.beginLine, op.endLine);
    }
    
    private void initTab(){
        Tab newTab = new Tab();
        newTab.setText("sample_source.java");
        //Build ListView
        ListView<String> linesView = new ListView<String>();
        linesView.setEditable(false);
        linesView.getItems().addAll(
                "    public void show (Operation op){",
                "        if (op == null) {",
                "            return;",
                "        }",
                "        System.out.println(\"op.source= \" + op.source);",
                "        Integer sourceTabIndex = nameTabMapping.get(op.source);",
                "        if (sourceTabIndex == null) {",
                "            Main.console.err(\"No source file given for Operation: \" + op);",
                "            return; //No source file given for this operation;",
                "        }",
                "        //Get ListView",
                "        root.getSelectionModel().select(sourceTabIndex.intValue());",
                "        ListView<String> linesView = (ListView<String>) root.getTabs().get(nameTabMapping.get(op.source)).getContent();",
                "        //Select lines",
                "        linesView.getSelectionModel().clearSelection();",
                "        linesView.getSelectionModel().selectRange(op.beginLine, op.endLine);",
                "    }",
                "Richard Sundqvist 2016-04-15 14:48");
        linesView.prefHeightProperty().bind(this.heightProperty());
        linesView.prefWidthProperty().bind(this.widthProperty());
        //Add children
        newTab.setContent(linesView);
        root.getTabs().add(newTab);
        
        newTab = new Tab();
        newTab.setText("sample_source2.java");
        //Build ListView
        linesView = new ListView<String>();
        linesView.setEditable(false);
        linesView.getItems().addAll(
                "    public void trySources (Map<String, List<String>> sources){",
                "        if (sources == null) {",
                "            return;",
                "        }",
                "        root.getTabs().clear();",
                "        int tabNumber = 0;",
                "        for (String sourceName : sources.keySet()) {",
                "           addSourceTab(sourceName, sources.get(sourceName));",
                "            nameTabMapping.put(sourceName, tabNumber);",
                "            tabNumber++;",
                "        }",
                "        root.getSelectionModel().select(0);",
                "Whisp is a shitty awper!!11oneone");
        linesView.prefHeightProperty().bind(this.heightProperty());
        linesView.prefWidthProperty().bind(this.widthProperty());
        //Add children
        newTab.setContent(linesView);
        root.getTabs().add(newTab);
    }
}
