package application.model;

import wrapper.Operation;

import java.util.List;

/**
 * Created by Ivar on 2016-03-24.
 */
public interface iModel {
    void setOperations(List<Operation> operations);
    void stepForward();
    void stepBackward();
    void stepForward(int steps);
    void stepBackward(int steps);
    void stepToIndex(int index);
    iStep getCurrentStep();
}
