package multiset;

import java.util.Map;

/**
 * Created by cb on 21/04/16.
 */
public class VariableLeaf implements iMember {
    private final String name;

    public VariableLeaf(String name) {
        this.name = name;
    }

    @Override
    public double getValue(Map<String, Double> variables) {
        return variables.get(name);
    }
}
