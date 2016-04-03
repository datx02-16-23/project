package application.model;

import wrapper.AnnotatedVariable;
import wrapper.Operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import manager.datastructures.DataStructure;

/**
 * Created by Ivar on 2016-03-23.
 */
public class Model implements iModel {
    private List<iStep> steps = new ArrayList<>();
    private List<Operation> operations;
    private int index;


    @Override
    public void addStructure(DataStructure struct) {
        if(struct.rawType.equals("array")){
            steps.get(index).addDataStructure(struct.identifier, Structure.Array);
        }
    }

    @Override
    public void stepForward() {
        iStep prevStep = steps.get(index);
        index += 1;
        if (steps.size() <= index){
            steps.add(new Step(prevStep, operations.get(index)));
        }
    }

    @Override
    public void set(Collection<DataStructure> structs, List<Operation> ops) {
        steps = new ArrayList<>();
        steps.add(new Step());
        operations = ops;
        index = 0;
        structs.forEach(this::addStructure);
    }

    @Override
    public iStep getCurrentStep() {
        return steps.get(index);
    }


}
