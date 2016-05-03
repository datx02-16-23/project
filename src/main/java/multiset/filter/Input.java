package multiset.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cb on 03/05/16.
 */
public class Input {
  private final String firstVar;
  private final String secondVar;

  public Input(String stringInput){
    stringInput = stringInput.trim();
    String[] split = stringInput.split(",");
    if (split.length != 2){
      throw new IllegalArgumentException();
    }
    firstVar = split[0].trim();
    secondVar = split[0].trim();
  }

  public String getFirstVar(){
    return firstVar;
  }

  public String getSecondVar(){
    return secondVar;
  }

  public List<String> getVars(){
    List<String> vars = new ArrayList<>();
    vars.add(firstVar);
    vars.add(secondVar);
    return vars;
  }


}
