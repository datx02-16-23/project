package multiset.Conditional;

import multiset.Conditional.ArithmeticOperations.iArithmeticOperation;

/**
 * Created by cb on 21/04/16.
 */
public class Node implements iMember {
    private final iArithmeticOperation op;
    private final iMember m1;
    private final iMember m2;

    public Node(iArithmeticOperation op, iMember m1, iMember m2){
        this.op = op;
        this.m1 = m1;
        this.m2 = m2;
    }

    @Override
    public double getValue(int v1, int v2) {
        return op.evaluate(m1.getValue(v1, v2), m2.getValue(v1, v2));
    }
}
