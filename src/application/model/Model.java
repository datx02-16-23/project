package application.model;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import wrapper.Operation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivar on 2016-03-23.
 */
public class Model implements iModel {
    private List<iStep> steps = new ArrayList<>();
    private List<Operation> operations;
    private int index;


    @Override
    public void setOperations(List<Operation> operations) {
        this.operations = operations;
        index = 0;
        createInitialStep();
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
    public void stepBackward() {
        if (index > 0) {
            index -= 1;
        }
    }

    @Override
    public void stepForward(int steps) {
        throw new NotImplementedException();
    }

    @Override
    public void stepBackward(int steps) {
        throw new NotImplementedException();
    }

    @Override
    public void stepToIndex(int index) {
        throw new NotImplementedException();
    }

    @Override
    public iStep getCurrentStep() {
        return steps.get(index);
    }

    /**
     * TODO, should get header data as parameters
     */
    private void createInitialStep(){
        iStep step = new Step();
        step.addDataStructure("a1", Structure.Array);
        step.addDataStructure("a2", Structure.Array);
        steps.add(0, step);
    }


}
