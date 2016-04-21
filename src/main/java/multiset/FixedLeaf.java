package multiset;

import java.util.Map;

/**
 * Created by cb on 21/04/16.
 */
public class FixedLeaf implements iMember {
    private final double val;

    public FixedLeaf(double val) {
        this.val = val;
    }


    @Override
    public double getValue(Map<String, Double> variables) {
        return val;
    }
}
