package multiset.filter;

import multiset.model.iValueContainer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by cb on 26/04/16.
 */
public class Filter implements iFilter{
  private final Input input;
  private final String result;
  private final Conditional conditional;

  public Filter (String input, String result, String conditional){
    this.input = new Input(input);
    this.result = result;
    this.conditional = new Conditional(conditional, this.input.getVars());
  }


  /**
   * Evaluates the expression with given two inputs, tries both values as both variables in expression
   * @return The set of iValueContainers that remains after the evaluation
   */
  public Set<iValueContainer> filter(iValueContainer a, iValueContainer b){
    setVars(a, b);
    if (conditional.evaluate()){
      return evaluate(a, b);
    }

    setVars(b, a);
    if (conditional.evaluate()){
      return evaluate(b, a);
    }

    Set<iValueContainer> remains = new HashSet<>();
    remains.add(a);
    remains.add(b);
    return remains;
  }

  private Set<iValueContainer> evaluate(iValueContainer a, iValueContainer b) {
    Set<iValueContainer> values = new HashSet<>();
    if (result.equals(input.getFirstVar())) {
      values.add(a);
    } else if (result.equals(input.getSecondVar())) {
      values.add(b);
    }
    if (values.size() == 0) {
      throw new RuntimeException("Critical error in result part of input");
    }
    return values;
  }

  private Map<String, Double> setVars(iValueContainer a, iValueContainer b) {
    Map<String, Double> vars = new HashMap<>();
    vars.put(input.getFirstVar(), a.getValue());
    vars.put(input.getSecondVar(), b.getValue());
    conditional.setVariables(vars);
    return vars;
  }
}
