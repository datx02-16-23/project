package multiset.filter;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Map;
import java.util.Set;

/**
 * Created by cb on 21/04/16.
 */
public class Conditional {
    private final Expression lhs;
    private final Expression rhs;
    private final Bdc.iBdc bdc;

    public Conditional(String conditional, Set<String> variables){
        String lhs = extractLhs(conditional);
        String rhs = extractRhs(conditional);
        String bdc = extractBdc(conditional);
        this.lhs = new ExpressionBuilder(lhs).variables(variables).build();
        this.rhs = new ExpressionBuilder(rhs).variables(variables).build();
        this.bdc = Bdc.getBDC(bdc);


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

    private String extractBdc(String conditional){
        for(Bdc bdc:Bdc.values()){
            if(conditional.contains(bdc.getRepresentation()){
                return bdc.getRepresentation();
            }
        }
    }

}
