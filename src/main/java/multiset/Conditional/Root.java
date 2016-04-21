package multiset.Conditional;

/**
 * Created by cb on 21/04/16.
 */
public class Root {
    BooleanOperation op;
    Member m1;
    Member m2;

    public Root(Member m1, Member m2, BooleanOperation op){
        this.op = op;
        this.m1 = m1;
        this.m2 = m2;
    }

    public boolean evaluate(int v1, int v2){
        //Calculate value of left hand side
        double lhs = m1.getValue(v1, v2);
        //Calculate value of right hand side
        double rhs = m2.getValue(v1, v2);
        return op.evaluate(lhs, rhs);
    }


}
