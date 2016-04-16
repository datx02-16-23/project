package application.gui.panels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.gui.Main;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import wrapper.Operation;

/**
 * Class and methods for displaying and jumping to relevant source code. Can only highlight full rows.
 * 
 * @author Richard
 *
 */
public class SourcePanel extends TabPane {

    private final HashMap<String, Integer> nameTabMapping;
    private Map<String, List<String>> sources;

    /**
     * Create a new SourceViewer.
     */
    public SourcePanel (){
        nameTabMapping = new HashMap<String, Integer>();
        this.prefHeightProperty().bind(this.heightProperty());
        this.prefWidthProperty().bind(this.widthProperty());
        this.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        initTab(); //Print some brilliant source code.
    }

    /**
     * Set the sources which are to be displayed by this SourceViewer. <b>Clears if newSources is {@code null}.</b>
     * 
     * @param soutces The sources to display.
     */
    public void setSources (Map<String, List<String>> newSources){
        getTabs().clear();
        if (newSources == null) {
            return;
        }
        this.sources = newSources;
        int tabNumber = 0;
        for (String sourceName : sources.keySet()) {
            addSourceTab(sourceName, sources.get(sourceName));
            nameTabMapping.put(sourceName, tabNumber++);
        }
        this.getSelectionModel().select(0);
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
        this.getTabs().add(newTab);
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
        Integer sourceTabIndex = nameTabMapping.get(op.source);
        if (sourceTabIndex == null) {
            Main.console.err("Could not find source file \"" + op.source + "\" for Operation: " + op);
            Main.console.err("nameTabMapping = " + nameTabMapping);
            return;
        }
        //Select tab
        this.getSelectionModel().select(sourceTabIndex);
        //Select lines
        ListView<String> linesView = (ListView<String>) this.getTabs().get(nameTabMapping.get(op.source)).getContent();
        linesView.getSelectionModel().select(op.beginLine);
    }
    
    /**
     * Returns the sources held by this SourceViewer.
     * @return The sources held by this SourceViewer.
     */
    public Map<String, List<String>> getSources(){
        return sources;
    }

    private void initTab (){
        Tab newTab = new Tab();
        newTab.setText("sample_source.java");
        //Build ListView
        ListView<String> linesView = new ListView<String>();
        linesView.setEditable(false);
        linesView.getItems().addAll("    public void show (Operation op){", "        if (op == null) {", "            return;", "        }",
                "        Integer sourceTabIndex = nameTabMapping.get(op.source);", "        if (sourceTabIndex == null) {",
                "            Main.console.err(\"No source file given for Operation: \" + op);", "            return;", "        }", "        //Get ListView",
                "        this.getSelectionModel().select(sourceTabIndex.intValue());",
                "        ListView<String> linesView = (ListView<String>) this.getTabs().get(nameTabMapping.get(op.source)).getContent();", "        //Select lines",
                "        linesView.getSelectionModel().clearSelection();", "        linesView.getSelectionModel().selectRange(op.beginLine, op.endLine);", "    }", "    public SourceViewer (){",
                "        this = new TabPane();", "        nameTabMapping = new HashMap<String, Integer>();", "        this.prefHeightProperty().bind(this.heightProperty());",
                "        this.prefWidthProperty().bind(this.widthProperty());", "        this.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);", "        this.setPrefSize(200, 200);",
                "        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);", "        this.getChildren().add(this);", "        initTab(); //Print some brillaint source code.", "    }",
                "Richard Sundqvist 2016-04-15 14:48");
        linesView.prefHeightProperty().bind(this.heightProperty());
        linesView.prefWidthProperty().bind(this.widthProperty());
        //Add children
        newTab.setContent(linesView);
        this.getTabs().add(newTab);
        newTab = new Tab();
        newTab.setText("sample_source2.java");
        //Build ListView
        linesView = new ListView<String>();
        linesView.setEditable(false);
        linesView.getItems().addAll("    public void trySources (Map<String, List<String>> sources){", "        if (sources == null) {", "            return;", "        }",
                "        this.getTabs().clear();", "        nameTabMapping.clear();", "        int tabNumber = 0;", "        for (String sourceName : sources.keySet()) {",
                "           addSourceTab(sourceName, sources.get(sourceName));", "            nameTabMapping.put(sourceName, tabNumber);", "            tabNumber++;", "        }",
                "        this.getSelectionModel().select(0);", "", "    private void addSourceTab (String sourceName, List<String> sourceLines){", "        //Build new Tab",
                "        Tab newTab = new Tab();", "        newTab.setText(sourceName);", "        //Build ListView", "        ListView<String> linesView = new ListView<String>();",
                "        linesView.setEditable(false);", "        linesView.getItems().addAll(sourceLines);", "        linesView.prefHeightProperty().bind(this.heightProperty());",
                "        linesView.prefWidthProperty().bind(this.widthProperty());", "        //Add children", "        newTab.setContent(linesView);", "        this.getTabs().add(newTab);", "    }",
                "Whisp is a shitty awper!!11oneone");
        linesView.prefHeightProperty().bind(this.heightProperty());
        linesView.prefWidthProperty().bind(this.widthProperty());
        //Add children
        newTab.setContent(linesView);
        this.getTabs().add(newTab);
    }
}
