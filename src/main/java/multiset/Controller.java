package multiset;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by cb on 24/04/16.
 */
public class Controller {

    public Controller(){
        String variables = "X, Y";
        String result = "X";
        String lhs = "X";
        String rhs = "Y";

        Conditional conditional = new Conditional(lhs, rhs, extractVariables(variables));
    }


    private Set<String> extractVariables(String input){
        Set<String> variables = new HashSet<>();
        for(String variable : input.split(",")){
            variables.add(variable.replace(" ", ""));
        }
        return variables;

    }
}
