package multiset.Conditional.ArithmeticOperations;

/**
 * Created by cb on 21/04/16.
 */
public class Addition implements iArithmeticOperation {

    @Override
    public double evaluate(double lhs, double rhs) {
        return lhs+rhs;
    }
}
