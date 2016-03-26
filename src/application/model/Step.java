package application.model;

import wrapper.Operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ivar on 2016-03-24.
 */
public class Step implements iStep {
    private final Map<String, List<Integer>> structs;

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
        structs.put(identifier, new ArrayList<>());
    }

    /**
     * Returns a deep copy of the structs in the step
     * @return
     */
    @Override
    public Map<String, List<Integer>> getStructures() {
        Map<String, List<Integer>> copy = new HashMap<>();
        for (String key:structs.keySet()){
            List<Integer> original = structs.get(key);
            List<Integer> listCopy = new ArrayList<>(original.size());
            for (Integer value:original){
                listCopy.add(value);
            }
            copy.put(key, listCopy);
        }
        return copy;
    }

}
