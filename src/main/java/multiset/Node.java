package multiset;

import multiset.ArithmeticOperations.iArithmeticOperation;

import java.util.Map;

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
    public double getValue(Map<String, Double> variables) {
        return op.evaluate(m1.getValue(variables), m2.getValue(variables));
    }
}
