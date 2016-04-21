package multiset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * @param variables
     * @param variables
     */
    public Set<String> apply(Map<String, Double> variables){

        if(conditional.evaluate(variables)){
            return result(variables);
        } else if(conditional.evaluate(variables)){
            return result(variables);
        }

        //If no match we return both elements
        return variables.keySet();
    }

    private Set<String> result(Map<String, Double> variables){
        return variables.keySet();
    }

}
