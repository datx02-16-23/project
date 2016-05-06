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
    private final iBdc bdc;

    public Conditional(String conditional, Set<String> variables){
        String lhs = extractLhs(conditional);
        String rhs = extractRhs(conditional);
        String bdc = extractBdc(conditional);
        this.lhs = new ExpressionBuilder(lhs).variables(variables).build();
        this.rhs = new ExpressionBuilder(rhs).variables(variables).build();
        this.bdc = Bdc.getBdc(bdc);


    }


    public void setVariables(Map<String, Double> variables){
        lhs.setVariables(variables);
        rhs.setVariables(variables);
    }


    public boolean evaluate(){
        return bdc.compare(lhs.evaluate(), rhs.evaluate());
    }


    private String extractLhs(String conditional){
        for(Bdc bdc:Bdc.values()){
            if(conditional.contains(bdc.getRepresentation())){
                return conditional.split(bdc.getRepresentation())[0].trim();
            }
        }
        throw new IllegalArgumentException("Missing bdc in conditional");
    }

    private String extractRhs(String conditional){
        for(Bdc bdc:Bdc.values()){
            if(conditional.contains(bdc.getRepresentation())){
                return conditional.split(bdc.getRepresentation())[1].trim();
            }
        }
        throw new IllegalArgumentException("Missing bdc in conditional");
    }

    private String extractBdc(String conditional){
        for(Bdc bdc:Bdc.values()){
            if(conditional.contains(bdc.getRepresentation())){
                return bdc.getRepresentation();
            }
        }
        throw new IllegalArgumentException("Missing bdc in conditional");
    }

}
