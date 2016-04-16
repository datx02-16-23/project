package application.assets;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import interpreter.Interpreter;
import io.CommunicatorListener;
import io.JGroupCommunicator;
import io.LogStreamManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import wrapper.*;
import wrapper.datastructures.*;
import wrapper.operations.*;

public class Simulator extends Application {

    private StreamSimulator       sm;
    private Dialog<String>        inspectDialog, memberDialog;
    private BorderPane            variablesView = new BorderPane();
    private BorderPane            baseView      = new BorderPane();
    private StackPane             root;
    ObservableList<DataStructure> knowVariablesList;

    @Override
    public void start (Stage primaryStage) throws Exception{
        sm = new StreamSimulator();
        inspectDialog = new Dialog<String>();
        memberDialog = new Dialog<String>();
        root = new StackPane();
        //Construct "Inspect" dialog.
        inspectDialog.setTitle("Inspecting Operation");
        inspectDialog.getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonData.CANCEL_CLOSE));
        //Construct "Members" dialog.
        memberDialog.setTitle("Channel members");
        memberDialog.getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonData.CANCEL_CLOSE));
        //Known Variables view
        Button closeKnownVariablesPane = buildCloseKnownVariablesButton();
        buildKnownVariablesView(closeKnownVariablesPane);
        Button knownVariables = buildKnownVariablesViewButton();
        //Labels
        Label waitingOperations = buildWaitingOperationsLabel();
        Label sentOperations = buildSentOperationsLabel();
        //Construct lists
        ListView<Operation> waitList = buildWaitList();
        ListView<Operation> sentList = buildSentList();
        //Buttons
        Button inspectSent = buildInspectSentOperationButton(sentList);
        Button inspectQueued = buildInspectQueuedOperationButton(waitList);
        Button transmit = buildTransmitButton();
        Button transmitAll = buildTransmitAllButton();
        Button read = buildReadButton();
        Button write = buildWriteButton();
        Button swap = buildSwapButton();
        Button init = buildInitButton();
        Button message = buildMessageButton();
        Button interpret = buildInterpretButton();
        ToggleButton continuousTransmit = buildContinousTransmitButton();
        Button clearLists = buildClearListsButton();
        Button importList = buildImportButton(primaryStage);
        Button exportSent = buildExportSentButton(primaryStage);
        Button exportQueued = buildExportQueuedButton(primaryStage);
        Button showMembers = buildShowMembersButton();
        //Construct stage
        //Add text fields
        FlowPane textPane = new FlowPane();
        textPane.setBorder(null);
        baseView.setTop(textPane);
        textPane.getChildren().add(waitingOperations);
        textPane.getChildren().add(sentOperations);
        //Add operation buttons
        VBox operationButtons = new VBox();
        baseView.setLeft(operationButtons);
        Separator separator1 = new Separator();
        separator1.setPrefHeight(10);
        Separator separator2 = new Separator();
        separator2.setPrefHeight(10);
        operationButtons.getChildren().addAll(knownVariables, separator1, read, write, swap, init, message, separator2, interpret);
        //Add operation buttons
        VBox controlButtons = new VBox();
        baseView.setRight(controlButtons);
        Separator separator3 = new Separator();
        separator3.setPrefHeight(10);
        Separator separator4 = new Separator();
        separator4.setPrefHeight(10);
        controlButtons.getChildren().addAll(transmit, transmitAll, continuousTransmit, separator3, clearLists, importList, exportQueued, exportSent, separator4, showMembers);
        //Add lists
        GridPane listPane = new GridPane();
        baseView.setCenter(listPane);
        listPane.add(waitingOperations, 0, 0);
        listPane.add(sentOperations, 1, 0);
        listPane.add(waitList, 0, 1);
        listPane.add(sentList, 1, 1);
        listPane.add(inspectSent, 1, 2);
        listPane.add(inspectQueued, 0, 2);
        primaryStage.setTitle("id =" + sm.getId() + ", channel = " + sm.getChannelName());
        root.getChildren().add(baseView);
        Scene scene = new Scene(root, 700, 400);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Button buildExportQueuedButton (Stage primaryStage){
        Button exportQueued = new Button();
        exportQueued.setText("Export Queued");
        exportQueued.setPrefSize(100, 30);
        exportQueued.setTooltip(new Tooltip("Export queued/received operations on a JSON format."));
        exportQueued.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                File f = chooseDirectory(primaryStage);
                if (f == null) {
                    return;
                }
                sm.exportQueued(f);
            }
        });
        return exportQueued;
    }

    private Button buildExportSentButton (Stage primaryStage){
        Button exportSent = new Button();
        exportSent.setText("Export Sent");
        exportSent.setPrefSize(100, 30);
        exportSent.setTooltip(new Tooltip("Export sent operations on a JSON format."));
        exportSent.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                File f = chooseDirectory(primaryStage);
                if (f == null) {
                    return;
                }
                sm.exportSent(f);
            }
        });
        return exportSent;
    }

    private Button buildImportButton (Stage primaryStage){
        //Construct "Import "button.
        Button importList = new Button();
        importList.setText("Import");
        importList.setPrefSize(100, 30);
        importList.setTooltip(new Tooltip("Import a log file and append it to queued/received operations."));
        importList.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                File f = chooseFile(primaryStage);
                if (f == null) {
                    return;
                }
                sm.importList(f);
            }
        });
        return importList;
    }

    private Button buildClearListsButton (){
        //Construct "Continuous Transmit "button.
        Button clearLists = new Button();
        clearLists.setText("Clear");
        clearLists.setPrefSize(100, 30);
        clearLists.setTooltip(new Tooltip("Clear lists, without transmitting anything."));
        clearLists.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                sm.clearLists();
            }
        });
        return clearLists;
    }

    private ToggleButton buildContinousTransmitButton (){
        ToggleButton continuousTransmit = new ToggleButton();
        continuousTransmit.setText("Continous\nTransmit");
        continuousTransmit.setPrefSize(100, 50);
        continuousTransmit.setTooltip(new Tooltip("Trasmit all operations automatically. Creates new random operations if neccessary."));
        continuousTransmit.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                sm.continousTransmit();
            }
        });
        return continuousTransmit;
    }

    private Button buildMessageButton (){
        //Construct "Message" button.
        Button message = new Button();
        message.setText("Message");
        message.setTooltip(new Tooltip("Create a new Message operation and add it to the queue."));
        message.setPrefSize(100, 30);
        message.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                sm.messageOperation();
            }
        });
        return message;
    }

    private Button buildInitButton (){
        //Construct "Init" button.
        Button init = new Button();
        init.setText("Init");
        init.setTooltip(new Tooltip("Create a new Init operation and add it to the queue."));
        init.setPrefSize(100, 30);
        init.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                sm.initOperation();
            }
        });
        return init;
    }

    private Button buildSwapButton (){
        //Construct "Swap" button.
        Button swap = new Button();
        swap.setText("Swap");
        swap.setTooltip(new Tooltip("Create a new Swap operation and add it to the queue."));
        swap.setPrefSize(100, 30);
        swap.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                sm.swapOperation();
            }
        });
        return swap;
    }

    private Button buildWriteButton (){
        //Construct "Write" button.
        Button write = new Button();
        write.setText("Write");
        write.setTooltip(new Tooltip("Create a new Write operation and add it to the queue."));
        write.setPrefSize(100, 30);
        write.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                sm.writeOperation();
            }
        });
        return write;
    }

    private Button buildReadButton (){
        //Construct "Read" button.
        Button read = new Button();
        read.setText("Read");
        read.setTooltip(new Tooltip("Create a new Read operation and add it to the queue."));
        read.setPrefSize(100, 30);
        read.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                sm.readOperation();
            }
        });
        return read;
    }

    private Button buildTransmitAllButton (){
        //Construct "Transmit All" button.
        Button transmitAll = new Button();
        transmitAll.setText("Transmit All\nFrom List");
        transmitAll.setTooltip(new Tooltip("Transmit all waiting operations."));
        transmitAll.setPrefSize(100, 30);
        transmitAll.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                sm.transmitAll();
            }
        });
        return transmitAll;
    }

    private Button buildTransmitButton (){
        //Construct "Transmit" button
        Button transmit = new Button();
        transmit.setText("Transmit\nFrom\nList");
        transmit.setTooltip(new Tooltip("Transmit the first waiting operation."));
        transmit.setPrefSize(100, 30);
        transmit.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                sm.transmitFirst();
            }
        });
        return transmit;
    }

    private Label buildSentOperationsLabel (){
        //Construct "sent Operations" label
        Label sentOperations = new Label();
        sentOperations.setPadding(new Insets(0, 5, 0, 5));
        sentOperations.setTooltip(new Tooltip("The number of operations that have been sent so far."));
        sentOperations.textProperty().bind(sm.getNbrSentString());
        return sentOperations;
    }

    private Label buildWaitingOperationsLabel (){
        Label waitingOperations = new Label();
        waitingOperations.setPadding(new Insets(0, 5, 0, 5));
        waitingOperations.setTooltip(new Tooltip("The number of operations waiting to be sent."));
        //		waitingOperations.setText("#Waiting: " + getNumberWaitingOperations());
        waitingOperations.textProperty().bind(sm.getNbrQueuedString());
        return waitingOperations;
    }

    private Button buildInspectQueuedOperationButton (ListView<Operation> waitList){
        Button inspectQueued = new Button();
        inspectQueued.setText("Inspect Queued Operation");
        inspectQueued.setPrefSize(250, 30);
        inspectQueued.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                inspectOperation(waitList);
            }
        });
        return inspectQueued;
    }

    private Button buildInspectSentOperationButton (ListView<Operation> sentList){
        Button inspectSent = new Button();
        inspectSent.setText("Inspect Sent Operation");
        inspectSent.setPrefSize(250, 30);
        inspectSent.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                inspectOperation(sentList);
            }
        });
        return inspectSent;
    }

    private Button buildShowMembersButton (){
        Button showMembers = new Button();
        showMembers.setText("Members");
        showMembers.setPrefSize(100, 30);
        showMembers.setTooltip(new Tooltip("Request a list of the members connected the channel."));
        showMembers.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                showMembers();
            }
        });
        return showMembers;
    }

    private void showMembers (){
        memberDialog.showAndWait();
    }

    private Button buildKnownVariablesViewButton (){
        //Construct "Known Variables view" button
        Button knownVariables = new Button();
        knownVariables.setText("Known\nVariables");
        knownVariables.setPrefSize(100, 50);
        knownVariables.setTooltip(new Tooltip("Open the Known Variables view."));
        knownVariables.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                knownVariablesView();
            }
        });
        return knownVariables;
    }

    private void buildKnownVariablesView (Button closeKnownVariablesPane){
        ListView<DataStructure> knownVariablesListView = new ListView<DataStructure>();
        knownVariablesListView.setPrefWidth(500);
        knownVariablesListView.setPrefHeight(300);
        knowVariablesList = FXCollections.observableArrayList();
        knownVariablesListView.setItems(knowVariablesList);
        variablesView.setLeft(knownVariablesListView);
        variablesView.setTop(new Label("Known Variables"));
        variablesView.setBottom(closeKnownVariablesPane);
    }

    private Button buildCloseKnownVariablesButton (){
        //Contruct "Close" button for knowVariablesPane
        Button closeKnownVariablesPane = new Button();
        closeKnownVariablesPane.setText("Close");
        closeKnownVariablesPane.setPrefSize(100, 50);
        closeKnownVariablesPane.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                root.getChildren().remove(variablesView);
                root.getChildren().add(baseView);
            }
        });
        return closeKnownVariablesPane;
    }

    private ListView<Operation> buildSentList (){
        //Construct "Sent Operations" list
        ListView<Operation> sentList = new ListView<Operation>();
        sentList.setItems(sm.getSentOperations());
        sentList.setMinWidth(250);
        sentList.setMaxWidth(250);
        sentList.setTooltip(new Tooltip("A list of operations which have been sent."));
        return sentList;
    }

    private ListView<Operation> buildWaitList (){
        //Construct "Waiting Operations" list
        ListView<Operation> waitList = new ListView<Operation>();
        waitList.setItems(sm.getQueuedOperations());
        waitList.setMinWidth(250);
        waitList.setMaxWidth(250);
        waitList.setTooltip(new Tooltip("A list of operations waiting to be sent."));
        return waitList;
    }

    private Button buildInterpretButton (){
        Button interpret = new Button();
        interpret.setText("Interpret");
        interpret.setTooltip(new Tooltip("Attempt to consolidate all queued/received operations."));
        interpret.setPrefSize(100, 30);
        interpret.setOnAction(new EventHandler<ActionEvent>() {

            public void handle (ActionEvent event){
                sm.interpret();
            }
        });
        return interpret;
    }

    private void knownVariablesView (){
        //Construct "Known Variables" dialog
        knowVariablesList.addAll(sm.getKnownVariables());
        root.getChildren().add(variablesView);
        root.getChildren().remove(baseView);
    }

    private void inspectOperation (ListView<Operation> sentList){
        Operation op = sentList.getSelectionModel().getSelectedItem();
        if (op == null) {
            return;
        }
        //Construct "Inspect" dialog
        inspectDialog.setHeaderText(op.toString());
        inspectDialog.setContentText(new GsonBuilder().setPrettyPrinting().create().toJson(op));
        inspectDialog.showAndWait();
    }

    public static void main (String[] args){
        launch(args);
    }

    private File chooseDirectory (Stage stage){
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Output Directory");
        return dc.showDialog(stage);
    }

    @Override
    public void stop (){
        sm.stop();
    }

    private File chooseFile (Stage stage){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        fileChooser.setTitle("Select Log File");
        return fileChooser.showOpenDialog(stage);
    }

    /*
     * SIMULATOR
     */
    private class StreamSimulator implements CommunicatorListener {

        private final LogStreamManager                     LSM;
        private final ObservableList<Operation>            queuedOperations, sentOperations;
        private final ObservableMap<String, DataStructure> knownVariables;
        private final SimpleStringProperty                 nbrQueuedString, nbrSentString, waitingOperationsList, sentOperationsList;
        private final Interpreter                          interpreter;

        public ObservableList<Operation> getQueuedOperations (){
            return queuedOperations;
        }

        private final int id;

        public StreamSimulator (){
            interpreter = new Interpreter();
            nbrQueuedString = new SimpleStringProperty();
            nbrSentString = new SimpleStringProperty();
            waitingOperationsList = new SimpleStringProperty();
            sentOperationsList = new SimpleStringProperty();
            id = (int) (Math.random() * Integer.MAX_VALUE);
            LSM = new LogStreamManager();
            LSM.PRETTY_PRINTING = true;
            LSM.setListener(this);
            queuedOperations = FXCollections.observableArrayList();
            sentOperations = FXCollections.observableArrayList();
            knownVariables = FXCollections.observableHashMap();
            updateSent();
            updateQueued();
            build();
        }

        private Wrapper         initWrapper;
        private List<Operation> ops;
        private List<Operation> inits;

        @SuppressWarnings("rawtypes")
        private void build (){
            initWrapper = new Wrapper(new Header(1337, buildStructs(), buildSources()), null);
            ops = buildOps();
        }

        private List<Operation> buildOps (){
            int arraySize = 10;
            int nbrOfOps = 30;
            ArrayList<Operation> _ops = new ArrayList<Operation>();
            inits = new ArrayList<Operation>();
            _ops.ensureCapacity(nbrOfOps + 3);
            Locator loc;
            //Create init for a1
            OP_Write init_a1 = new OP_Write(srcNames[0], 1, 2, -1, -1);
            loc = new Locator("a1", null);
            init_a1.setTarget(loc);
            init_a1.setValue(new double[] {1, 2, 4, 8, 16, 32, 64, 128, 256, 512});
            //Create init for a2
            OP_Write init_a2 = new OP_Write(srcNames[0], 1, 2, -1, -1);
            loc = new Locator("a2", null);
            init_a2.setTarget(loc);
            init_a2.setValue(new double[] {512, 256, 128, 64, 32, 16, 8, 4, 2, 1});
            //Create init for tmp
            OP_Write init_tmp = new OP_Write(srcNames[0], 1, 2, -1, -1);
            loc = new Locator("tmp", null);
            init_tmp.setTarget(loc);
            init_tmp.setValue(new double[] {1337});
            inits.add(init_a1);
            inits.add(init_a2);
            inits.add(init_tmp);
            OP_Write op;
            loop: for (int i = 0; i < nbrOfOps; i++) {
                if (i % 10 == 0) {
                    op = new OP_Write(srcNames[0], (i * i) % srcSizes[0], (i * i * 1) % srcSizes[0], -1, -1);
                    loc = new Locator("a1", new int[] {i % arraySize});
                    op.setSource(loc);
                    loc = new Locator("tmp", null);
                    op.setTarget(loc);
                    op.setValue(new double[] {(i * i) % 2});
                    continue loop;
                }
                if (i % 2 == 0) {
                    op = new OP_Write(srcNames[0], (i * i) % srcSizes[0], (i * i * 1) % srcSizes[0], -1, -1);
                    loc = new Locator("a1", new int[] {i % arraySize});
                    op.setSource(loc);
                    loc = new Locator("a2", new int[] {(i * 2) % arraySize});
                    op.setTarget(loc);
                    op.setValue(new double[] {(i * i) % 2});
                }
                else {
                    op = new OP_Write(srcNames[0], (i * i) % srcSizes[1], (i * i * 1) % srcSizes[1], -1, -1);
                    op.setSource(loc);
                    loc = new Locator("a2", new int[] {i % arraySize});
                    op.setSource(loc);
                    loc = new Locator("a1", new int[] {(i * 2) % arraySize});
                    op.setTarget(loc);
                    op.setValue(new double[] {(i * i) % 2});
                }
            }
            return _ops;
        }

        private Map<String, AnnotatedVariable> buildStructs (){
            Map<String, AnnotatedVariable> _structs = new HashMap<String, AnnotatedVariable>();
            Array a1 = new Array("a1", null, "box");
            Array a2 = new Array("a2", null, "box");
            IndependentElement tmp = new IndependentElement("tmp", null, null);
            _structs.put(a1.identifier, a1);
            _structs.put(a2.identifier, a2);
            _structs.put(tmp.identifier, tmp);
            return _structs;
        }

        int      max1, max2;
        String[] srcNames;
        int[]    srcSizes;

        private HashMap<String, List<String>> buildSources (){
            //Build sources
            ObservableList<String> tmp1 = FXCollections.observableArrayList();
            tmp1.addAll("    public void show (Operation op){", "        if (op == null) {", "            return;", "        }", "        Integer sourceTabIndex = nameTabMapping.get(op.source);",
                    "        if (sourceTabIndex == null) {", "            Main.console.err(\"No source file given for Operation: \" + op);", "            return;", "        }",
                    "        //Get ListView", "        this.getSelectionModel().select(sourceTabIndex.intValue());",
                    "        ListView<String> linesView = (ListView<String>) this.getTabs().get(nameTabMapping.get(op.source)).getContent();", "        //Select lines",
                    "        linesView.getSelectionModel().clearSelection();", "        linesView.getSelectionModel().selectRange(op.beginLine, op.endLine);", "    }", "    public SourceViewer (){",
                    "        this = new TabPane();", "        nameTabMapping = new HashMap<String, Integer>();", "        this.prefHeightProperty().bind(this.heightProperty());",
                    "        this.prefWidthProperty().bind(this.widthProperty());", "        this.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);", "        this.setPrefSize(200, 200);",
                    "        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);", "        this.getChildren().add(this);", "        initTab(); //Print some brillaint source code.", "    }",
                    "Richard Sundqvist 2016-04-15 14:48");
            ObservableList<String> tmp2 = FXCollections.observableArrayList();
            tmp2.addAll("    public void trySources (Map<String, List<String>> sources){", "        if (sources == null) {", "            return;", "        }", "        this.getTabs().clear();",
                    "        nameTabMapping.clear();", "        int tabNumber = 0;", "        for (String sourceName : sources.keySet()) {",
                    "           addSourceTab(sourceName, sources.get(sourceName));", "            nameTabMapping.put(sourceName, tabNumber);", "            tabNumber++;", "        }",
                    "        this.getSelectionModel().select(0);", "", "    private void addSourceTab (String sourceName, List<String> sourceLines){", "        //Build new Tab",
                    "        Tab newTab = new Tab();", "        newTab.setText(sourceName);", "        //Build ListView", "        ListView<String> linesView = new ListView<String>();",
                    "        linesView.setEditable(false);", "        linesView.getItems().addAll(sourceLines);", "        linesView.prefHeightProperty().bind(this.heightProperty());",
                    "        linesView.prefWidthProperty().bind(this.widthProperty());", "        //Add children", "        newTab.setContent(linesView);", "        this.getTabs().add(newTab);",
                    "    }", "Whisp is a shitty awper!!11oneone");
            srcNames = new String[] {"foo.java", "bar.py"};
            ArrayList<String> sourceOne = new ArrayList<String>(tmp1);
            ArrayList<String> sourceTwo = new ArrayList<String>(tmp2);
            srcSizes = new int[] {sourceOne.size(), sourceTwo.size()};
            HashMap<String, List<String>> _sources = new HashMap<String, List<String>>();
            _sources.put(srcNames[0], sourceOne);
            _sources.put(srcNames[1], sourceTwo);
            return _sources;
        }

        public void transmitOperation (Operation op){
            Platform.runLater(new Runnable() {

                @Override
                public void run (){
                    ArrayList<Operation> operationList = new ArrayList<Operation>();
                    operationList.add(op);
                    Wrapper message = new Wrapper(null, operationList);
                    if (transmit(message)) {
                        sentOperations.add(op);
                        updateSent();
                    }
                }
            });
        }

        public void clearLists (){
            sentOperations.clear();
            queuedOperations.clear();
            updateSent();
            updateQueued();
        }

        int             sleepDur          = 1500;
        private boolean continousTransmit = false;
        Timeline        autoplayTimeline;

        public void continousTransmit (){
            continousTransmit = !continousTransmit;
            if (continousTransmit == false) {
                autoplayTimeline.stop();
                return;
            }
            System.out.println("init!");
            transmit(initWrapper);
            queuedOperations.addAll(inits);
            transmitFirst();
            transmitFirst();
            transmitFirst(); //Transmit inits
            System.out.println(inits);
            System.out.println("begin regular ops transmit\n\n");
            autoplayTimeline = new Timeline();
            autoplayTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(1500), new EventHandler<ActionEvent>() {

                @Override
                public void handle (ActionEvent actionEvent){
                    if (queuedOperations.isEmpty()) {
                        queuedOperations.addAll(ops);
                    }
                    System.out.println("send!");
                    transmitFirst();
                }
            }));
            autoplayTimeline.setCycleCount(Animation.INDEFINITE);
            autoplayTimeline.play();
        }

        //Getters and setters
        public ObservableList<Operation> getSentOperations (){
            return sentOperations;
        }

        public SimpleStringProperty getNbrQueuedString (){
            return nbrQueuedString;
        }

        public SimpleStringProperty getNbrSentString (){
            return nbrSentString;
        }

        public int getId (){
            return id;
        }

        //Receiver stuff
        public boolean transmit (Wrapper wrapper){
            System.out.println("stream wrapper");
            try {
                LSM.stream(wrapper);
                System.out.println("stream wrapper ok");
            } catch (Exception e) {
                System.out.println("stream wrapper failed: " + e);
                return false;
            }
            return true; //Return true if transmit was successful.
        }

        public void transmitAll (){
            while(queuedOperations.isEmpty() == false) {
                transmitOperation(queuedOperations.remove(0));
            }
            updateQueued();
        }

        public void transmitFirst (){
            if (queuedOperations.isEmpty() == false) {
                transmitOperation(queuedOperations.remove(0));
            }
            updateQueued();
        }

        public void messageOperation (){
            OP_Message op = new OP_Message();
            op.setMessage("JavaFX is the future!");
            queuedOperations.add(op);
            updateQueued();
        }

        public void readOperation (){
            OP_Read op = new OP_Read();
            op.setSource(new Locator("a1", new int[] {1}));
            op.setTarget(new Locator("a1", new int[] {2}));
            op.setValue(new double[] {Math.random()});
            queuedOperations.add(op);
            updateQueued();
        }

        public void writeOperation (){
            OP_Write op = new OP_Write();
            op.setSource(new Locator("a2", new int[] {2}));
            op.setTarget(new Locator("a2", new int[] {1}));
            op.setValue(new double[] {Math.random()});
            queuedOperations.add(op);
            updateQueued();
        }

        //TODO: Convert to Write
        public void initOperation (){
            OP_Write op = new OP_Write();
            op.setTarget(new Locator("a1", new int[] {1}));
            op.setValue(
                    new double[] {Math.round(Math.random() * 100), Math.round(Math.random() * 100), Math.round(Math.random() * 100), Math.round(Math.random() * 100), Math.round(Math.random() * 100)});
            queuedOperations.add(op);
            updateQueued();
        }

        public void swapOperation (){
            OP_Swap op = new OP_Swap();
            op.setVar1((new Locator("a1", new int[] {0})));
            op.setVar2((new Locator("a2", new int[] {0})));
            op.setValues(null);
            queuedOperations.add(op);
            updateQueued();
        }

        public void updateQueued (){
            Platform.runLater(new Runnable() {

                @Override
                public void run (){
                    nbrQueuedString.set("#Queued/Received: " + queuedOperations.size());
                    if (queuedOperations.isEmpty()) {
                        waitingOperationsList.set("\tNo operations in queue!");
                    }
                    else {
                        StringBuilder sb = new StringBuilder();
                        for (Operation op : queuedOperations) {
                            sb.append("\t" + op.toSimpleString() + "\n");
                        }
                        waitingOperationsList.set(sb.toString());
                    }
                }
            });
        }

        public void updateSent (){
            Platform.runLater(new Runnable() {

                @Override
                public void run (){
                    nbrSentString.set("#Sent: " + sentOperations.size());
                    if (sentOperations.isEmpty()) {
                        sentOperationsList.set("\tNo operations have been sent!");
                    }
                    else {
                        StringBuilder sb = new StringBuilder();
                        for (Operation op : sentOperations) {
                            sb.append("\t" + op.toSimpleString() + "\n");
                        }
                        sentOperationsList.set(sb.toString());
                    }
                }
            });
        }

        public void interpret (){
            interpreter.consolidate(queuedOperations);
            updateQueued();
        }

        public void importList (File jsonFile){
            LSM.clearData();
            LSM.readLog(jsonFile);
            queuedOperations.addAll(LSM.getOperations());
            knownVariables.putAll(LSM.getKnownVariables());
            updateQueued();
        }

        public void exportSent (File targetDir){
            exportList(targetDir, sentOperations);
        }

        public void exportQueued (File targetDir){
            exportList(targetDir, queuedOperations);
        }

        private void exportList (File targetDir, List<Operation> list){
            LSM.setKnownVariables(knownVariables);
            LSM.setOperations(list);
            LSM.printLogAutoName(targetDir);
        }

        @Override
        public void messageReceived (short messageType){
            Platform.runLater(new Runnable() {

                @Override
                public void run (){
                    //Operations
                    queuedOperations.addAll(LSM.getOperations());
                    LSM.clearOperations();
                    updateQueued();
                    //Variables
                    knownVariables.putAll(LSM.getKnownVariables());
                    LSM.clearKnownVariables();
                }
            });
        }

        public List<DataStructure> getKnownVariables (){
            ArrayList<DataStructure> ans = new ArrayList<DataStructure>();
            ans.addAll(knownVariables.values());
            return ans;
        }

        public String getChannelName (){
            return ((JGroupCommunicator) LSM.getCommunicator()).getChannel();
        }

        public void stop (){
            LSM.getCommunicator().close();
        }

        @Override
        public CommunicatorListener getListener (){
            return null; //StreamSimulator doesn't have any listeners.
        }
    }
}
