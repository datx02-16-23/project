package gui.panel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import contract.Operation;
import gui.Main;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Class and methods for displaying and jumping to relevant source code. Can
 * only highlight full rows.
 *
 * @author Richard Sundqvist
 *
 */
public class SourcePanel extends TabPane {

    private final HashMap<String, Integer>  nameTabMapping;
    private final Map<String, List<String>> sources;
    private boolean                         initTabs = true;
    private double                          divPos   = 0;

    /**
     * Create a new SourceViewer.
     */
    public SourcePanel () {
        this.nameTabMapping = new HashMap<String, Integer>();
        this.sources = new HashMap<String, List<String>>();
        this.prefHeightProperty().bind(this.heightProperty());
        this.prefWidthProperty().bind(this.widthProperty());
        this.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        if (this.initTabs) {
            this.initTab(); // Print some brilliant source code.
        }
    }

    /**
     * Add sources which are to be displayed by this SourceViewer.
     *
     * @param newSources
     *            The sources to display.
     */
    public void addSources (Map<String, List<String>> newSources) {
        if (newSources == null) {
            return;
        }
        if (this.initTabs) {
            this.initTabs = false;
            this.clear();
        }
        this.sources.putAll(newSources);
        ;
        int tabNumber = 0;
        for (String sourceName : this.sources.keySet()) {
            this.addSourceTab(sourceName, this.sources.get(sourceName));
            this.nameTabMapping.put(sourceName, tabNumber++);
        }
    }

    /**
     * Clear sources and tabs.
     */
    public void clear () {
        this.sources.clear();
        this.getTabs().clear();
        SplitPane sp = (SplitPane) this.getParent().getParent().getParent();
        this.divPos = sp.getDividerPositions() [0];
        sp.setDividerPosition(0, 0);
    }

    /**
     * Create a tab for a source file.
     *
     * @param sourceName
     *            The name of the source file.
     * @param sourceLines
     *            The lines for the source file.
     */
    private void addSourceTab (String sourceName, List<String> sourceLines) {
        // Build new Tab
        Tab newTab = new Tab();
        newTab.setText(sourceName);
        // Build ListView
        ListView<String> linesView = new ListView<String>();
        linesView.setEditable(false);
        linesView.getItems().addAll(sourceLines);
        linesView.prefHeightProperty().bind(this.heightProperty());
        linesView.prefWidthProperty().bind(this.widthProperty());
        // Add children
        newTab.setContent(linesView);
        this.getTabs().add(newTab);
        SplitPane sp = (SplitPane) this.getParent().getParent().getParent();
        sp.setDividerPosition(0, this.divPos);
    }

    /**
     * Jump to the appropriate tab and line number for this Operation. Will
     * abort if {@code op == null} or {@code op.source == null}.
     *
     * @param op
     *            The Operation to show.
     */
    @SuppressWarnings("unchecked")
    public void show (Operation op) {
        if (op == null || op.source == null) {
            return;
        }
        Integer sourceTabIndex = this.nameTabMapping.get(op.source);
        if (sourceTabIndex == null) {
            Main.console.err("Could not find source file \"" + op.source + "\" for Operation: " + op);
            Main.console.err("Known sources: " + this.nameTabMapping.keySet());
            return;
        }
        // Select tab
        this.getSelectionModel().select(sourceTabIndex);
        // Select lines
        ListView<String> linesView = (ListView<String>) this.getTabs().get(this.nameTabMapping.get(op.source))
                .getContent();
        linesView.getSelectionModel().select(op.beginLine - 1);
        linesView.getFocusModel().focus(op.beginLine);
        linesView.scrollTo(op.beginLine - 1);
    }

    /**
     * Returns the sources held by this SourceViewer.
     *
     * @return The sources held by this SourceViewer.
     */
    public Map<String, List<String>> getSources () {
        return this.sources;
    }

    @Deprecated
    private void initTab () {
        Tab newTab = new Tab();
        newTab.setText("sample_source.java");
        // Build ListView
        ListView<String> linesView = new ListView<String>();
        linesView.setEditable(false);
        linesView.getItems().addAll("    public void show (Operation op){", "        if (op == null) {",
                "            return;", "        }", "        Integer sourceTabIndex = nameTabMapping.get(op.source);",
                "        if (sourceTabIndex == null) {",
                "            Main.console.err(\"No source file given for Operation: \" + op);", "            return;",
                "        }", "        //Get ListView",
                "        this.getSelectionModel().select(sourceTabIndex.intValue());",
                "        ListView<String> linesView = (ListView<String>) this.getTabs().get(nameTabMapping.get(op.source)).getContent();",
                "        //Select lines", "        linesView.getSelectionModel().clearSelection();",
                "        linesView.getSelectionModel().selectRange(op.beginLine, op.endLine);", "    }",
                "    public SourceViewer (){", "        nameTabMapping = new HashMap<String, Integer>();",
                "        this.prefHeightProperty().bind(this.heightProperty());",
                "        this.prefWidthProperty().bind(this.widthProperty());",
                "        this.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);",
                "        this.setPrefSize(200, 200);", "        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);",
                "        this.getChildren().add(this);", "        initTab(); //Print some brillaint source code.",
                "    }", "Richard Sundqvist Sundqvist 2016-04-15 14:48");
        linesView.prefHeightProperty().bind(this.heightProperty());
        linesView.prefWidthProperty().bind(this.widthProperty());
        // Add children
        newTab.setContent(linesView);
        this.getTabs().add(newTab);
        newTab = new Tab();
        newTab.setText("sample_source2.java");
        // Build ListView
        linesView = new ListView<String>();
        linesView.setEditable(false);
        linesView.getItems().addAll("    public void trySources (Map<String, List<String>> sources){",
                "        if (sources == null) {", "            return;", "        }", "        this.getTabs().clear();",
                "        nameTabMapping.clear();", "        int tabNumber = 0;",
                "        for (String sourceName : sources.keySet()) {",
                "           addSourceTab(sourceName, sources.get(sourceName));",
                "            nameTabMapping.put(sourceName, tabNumber);", "            tabNumber++;", "        }",
                "        this.getSelectionModel().select(0);", "",
                "    private void addSourceTab (String sourceName, List<String> sourceLines){",
                "        //Build new Tab", "        Tab newTab = new Tab();", "        newTab.setText(sourceName);",
                "        //Build ListView", "        ListView<String> linesView = new ListView<String>();",
                "        linesView.setEditable(false);", "        linesView.getItems().addAll(sourceLines);",
                "        linesView.prefHeightProperty().bind(this.heightProperty());",
                "        linesView.prefWidthProperty().bind(this.widthProperty());", "        //Add children",
                "        newTab.setContent(linesView);", "        this.getTabs().add(newTab);", "    }",
                "Whisp is a shitty awper!!11oneone");
        linesView.prefHeightProperty().bind(this.heightProperty());
        linesView.prefWidthProperty().bind(this.widthProperty());
        // Add children
        newTab.setContent(linesView);
        this.getTabs().add(newTab);
    }
}
