package application.model;

import wrapper.Operation;
import wrapper.datastructures.DataStructure;

import java.util.List;
import java.util.Map;

import javafx.collections.ObservableList;

// TODO: Javadoc #50
public interface iModel {

    void addStructure (String id, DataStructure structure);

    void reset ();

    boolean stepForward ();

    boolean stepBackward ();

    void set (Map<String, DataStructure> structs, List<Operation> ops);

    void setOperations (List<Operation> items);

    void setStructures (Map<String, DataStructure> structs);

    public Map<String, DataStructure> getStructures ();

    public List<Operation> getOperations ();

    iStep getCurrentStep ();

    int getIndex ();

    void goToStep (int toStepNo);

    void goToEnd ();
}
