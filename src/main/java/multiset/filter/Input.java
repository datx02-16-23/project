package multiset.filter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by cb on 03/05/16.
 */
public class Input {
    private final String firstVar;
    private final String secondVar;

    public Input (String stringInput) {
        stringInput = stringInput.trim();
        String[] split = stringInput.split(",");
        if (split.length != 2) {
            throw new IllegalArgumentException();
        }
        firstVar = split [0].replace(" ", "");
        secondVar = split [1].replace(" ", "");
    }

    public String getFirstVar () {
        return firstVar;
    }

    public String getSecondVar () {
        return secondVar;
    }

    public Set<String> getVars () {
        Set<String> vars = new HashSet<>();
        vars.add(firstVar);
        vars.add(secondVar);
        return vars;
    }

}
