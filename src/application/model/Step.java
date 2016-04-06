package application.model;

import manager.datastructures.DataStructure;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import wrapper.Operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import manager.datastructures.DataStructure;

/**
 * Created by Ivar on 2016-03-24.
 */
public class Step implements iStep {
    private final Map<String, List<DataStructure>> structs;

    public Step(){
        structs = new HashMap<>();
    }

    public Step(iStep prevStep, Operation op){
        //Copy prevStep structs
        structs = prevStep.getStructures();
        //Apply operation

    }


    @Override
    public void addDataStructure(String identifier, Structure struct) {
        if(struct.equals(Structure.Array)) {
            structs.put(identifier, new ArrayList<DataStructure>());
        } else {
            throw new NotImplementedException();
        }
    }

    /**
     * Returns a deep copy of the structs in the step
     * @return
     */
    @Override
    public Map<String, List<DataStructure>> getStructures() {
        Map<String, List<DataStructure>> copy = new HashMap<>();
        for (String key:structs.keySet()){
            List<DataStructure> original = structs.get(key);
            List<DataStructure> listCopy = new ArrayList<>(original.size());
            for (DataStructure value:original){
                listCopy.add(value);

            }
            copy.put(key, listCopy);
        }
        return copy;
    }

}
