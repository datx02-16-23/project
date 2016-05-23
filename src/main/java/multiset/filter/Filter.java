package multiset.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import multiset.model.iValueContainer;

/**
 * Created by cb on 26/04/16.
 */
public class Filter implements iFilter {
    private final Input       input;
    private final String      result;
    private final Conditional conditional;

    public Filter (String input, String result, String conditional) {
        this.input = new Input(input);
        this.result = result;
        this.conditional = new Conditional(conditional, this.input.getVars());
    }

    /**
     * Evaluates the expression with given two inputs, tries both values as both
     * variables in expression
     * 
     * @return The set of iValueContainers that remains after the evaluation
     */
    @Override
    public Set<iValueContainer> filter (iValueContainer a, iValueContainer b) {
        this.setVars(a, b);
        if (this.conditional.evaluate()) {
            return this.evaluate(a, b);
        }

        this.setVars(b, a);
        if (this.conditional.evaluate()) {
            return this.evaluate(b, a);
        }

        Set<iValueContainer> remains = new HashSet<>();
        remains.add(a);
        remains.add(b);
        return remains;
    }

    private Set<iValueContainer> evaluate (iValueContainer a, iValueContainer b) {
        Set<iValueContainer> values = new HashSet<>();
        if (this.result.equals(this.input.getFirstVar())) {
            values.add(a);
        } else if (this.result.equals(this.input.getSecondVar())) {
            values.add(b);
        }
        if (values.size() == 0) {
            throw new RuntimeException("Critical error in result part of input");
        }
        return values;
    }

    private Map<String, Double> setVars (iValueContainer a, iValueContainer b) {
        Map<String, Double> vars = new HashMap<>();
        vars.put(this.input.getFirstVar(), a.getValue());
        vars.put(this.input.getSecondVar(), b.getValue());
        this.conditional.setVariables(vars);
        return vars;
    }
}
