package application.model;

import manager.datastructures.DataStructure;

import java.util.List;
import java.util.Map;

import manager.datastructures.DataStructure;

/**
 * Created by Ivar on 2016-03-24.
 */
public interface iStep {
    void addDataStructure(String identifier, Structure struct);
    Map<String, List<DataStructure>> getStructures();
}
