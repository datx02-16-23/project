package application.model;

import wrapper.AnnotatedVariable;
import wrapper.Operation;

import java.util.List;

/**
 * Created by Ivar on 2016-03-24.
 */
public interface iModel {
    void addStructure(AnnotatedVariable structure);
    void setOperations(List<Operation> operations);
    void stepForward();
    iStep getCurrentStep();
}
