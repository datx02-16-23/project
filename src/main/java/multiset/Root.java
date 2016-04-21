package multiset;


import multiset.BooleanOperations.iBooleanComparison;

import java.util.Map;

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

    public boolean evaluate(Map<String, Double> variables){
        //Calculate value of left hand side
        double lhs = m1.getValue(variables);
        //Calculate value of right hand side
        double rhs = m2.getValue(variables);
        return op.evaluate(lhs, rhs);
    }


}
