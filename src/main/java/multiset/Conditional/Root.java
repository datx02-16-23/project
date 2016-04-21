package multiset.Conditional;

import multiset.Conditional.BooleanOperations.iBooleanComparison;

/**
 * Created by cb on 21/04/16.
 */
public class Root {
    iBooleanComparison op;
    iMember m1;
    iMember m2;

    public Root(iMember m1, iMember m2, iBooleanComparison op){
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
