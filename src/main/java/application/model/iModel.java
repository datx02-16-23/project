package application.model;

import wrapper.Operation;
import wrapper.datastructures.DataStructure;

import java.util.List;
import java.util.Map;

// TODO: Javadoc #50
public interface iModel {

    void reset ();

    boolean stepForward ();

    boolean stepBackward ();

    void set (Map<String, DataStructure> structs, List<Operation> ops);

    void setOperations (List<Operation> items);

    Map<String, DataStructure> getStructures ();

    List<Operation> getOperations ();

    iStep getCurrentStep ();

    int getIndex ();

    void goToStep (int toStepNo);

    void goToEnd ();
}
