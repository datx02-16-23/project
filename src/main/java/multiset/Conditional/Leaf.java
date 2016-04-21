package multiset.Conditional;

/**
 * Created by cb on 21/04/16.
 */
public class Leaf implements iMember {
    private final double val;

    public Leaf(double val){
        this.val = val;
    }

    @Override
    public double getValue(int v1, int v2) {
        return val;
    }
}
