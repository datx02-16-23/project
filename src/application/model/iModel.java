package application.model;

import wrapper.AnnotatedVariable;
import wrapper.Operation;

import java.util.Collection;
import java.util.List;

/**
 * Created by Ivar on 2016-03-24.
 */
public interface iModel {
    void addStructure(AnnotatedVariable structure);
    void stepForward();
    void set(Collection<AnnotatedVariable> structs, List<Operation> ops);
    iStep getCurrentStep();
}
