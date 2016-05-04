package multiset.filter;

import multiset.model.iValueContainer;

import java.util.HashSet;
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
  public Set<iValueContainer> evaluate(iValueContainer a, iValueContainer b){

    //conditional.setVariables();
    Set<iValueContainer> remains = new HashSet<>();
    remains.add(a);
    return remains;
  }

}
