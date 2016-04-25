package multiset;

/**
 * Created by cb on 23/04/16.
 */
public class LessThan implements BooleanDoubleComparison {


    @Override
    public boolean compare(double a, double b) {
        return a < b;
    }
}
