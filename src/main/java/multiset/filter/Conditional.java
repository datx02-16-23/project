package multiset.filter;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by cb on 21/04/16.
 */
public class Conditional {
    private final Expression lhs;
    private final Expression rhs;
    private final BooleanDoubleComparison bdc;

    public Conditional(String conditional, Set<String> variables){
        System.out.println(extractLhs(conditional));
        System.out.println(extractRhs(conditional));
        this.lhs = new ExpressionBuilder("1+2+3").variables(variables).build();
        this.rhs = new ExpressionBuilder("2+3").variables(variables).build();
        bdc = (double a, double b) -> a > b;


    }

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


    private String extractLhs(String conditional){
        return conditional.split(">")[0].trim();
    }

    private String extractRhs(String conditional){
        return conditional.split(">")[1].trim();
    }

}
