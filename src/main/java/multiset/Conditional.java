package multiset;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by cb on 21/04/16.
 */
public class Conditional {
    private final Expression lhs;
    private final Expression rhs;
    private final BooleanDoubleComparison bdc;

    public Conditional(String lhs, String rhs, Set<String> variables){
        this.lhs = new ExpressionBuilder(lhs).variables(variables).build();
        this.rhs = new ExpressionBuilder(rhs).variables(variables).build();
        bdc = (double a, double b) -> a > b;

    }

    public void setVariables(Map<String, Double> variables){
        lhs.setVariables(variables);
        rhs.setVariables(variables);
    }

    public boolean evaluate(){
        return bdc.compare(lhs.evaluate(), rhs.evaluate());
    }


    public static void main(String[] args){
        Map<String, Double> variables = new HashMap<>();
        variables.put("a", 3.0);
        variables.put("b", 4.0);
        Conditional c = new Conditional("a+b", "a+a", variables.keySet());
        c.setVariables(variables);
        System.out.println(c.evaluate());

    }



}
