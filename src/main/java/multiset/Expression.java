package multiset;

import multiset.Conditional.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cb on 21/04/16.
 */
public class Expression {
    private final Root conditional;

    public Expression(Root conditional) {
        this.conditional = conditional;
    }


    /**
     * The method to be called when a collision occurs
     * @param v1
     * @param v2
     */
    public List<Integer> apply(int v1, int v2){

        if(conditional.evaluate(v1, v2)){
            return result(v1, v2);
        } else if(conditional.evaluate(v2, v1)){
            return result(v2, v1);
        }

        //If no match we return both elements
        return toList(v1, v2);
    }

    private List<Integer> result(int v1, int v2){
        return toList(v1, v2);
    }

    /**
     * Helper class to quickly create a list out of two variables
     * @param v1
     * @param v2
     * @return
     */
    private List<Integer> toList(int v1, int v2){
        List<Integer> l = new ArrayList<>(2);
        l.add(v1);
        l.add(v2);
        return l;
    }
}
