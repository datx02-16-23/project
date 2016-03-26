package application.model;

import java.util.List;
import java.util.Map;

/**
 * Created by Ivar on 2016-03-24.
 */
public interface iStep {
    void addDataStructure(String identifier, Structure struct);
    Map<String, List<Integer>> getStructures();
}
