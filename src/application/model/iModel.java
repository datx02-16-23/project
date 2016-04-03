package application.model;

import wrapper.Operation;

import java.util.Collection;
import java.util.List;

import manager.datastructures.DataStructure;

/**
 * Created by Ivar on 2016-03-24.
 */
public interface iModel {
    void addStructure(DataStructure structure);
    void stepForward();
    void set(Collection<DataStructure> structs, List<Operation> ops);
    iStep getCurrentStep();
}
