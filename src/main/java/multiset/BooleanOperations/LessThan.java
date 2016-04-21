package multiset.BooleanOperations;

/**
 * Created by cb on 21/04/16.
 */
public class LessThan implements iBooleanComparison {

    @Override
    public boolean evaluate(double lhs, double rhs) {
        return lhs < rhs;
    }
}
