package multiset.filter;

import java.util.Map;
import java.util.Set;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Created by cb on 21/04/16.
 */
public class Conditional {
    private final Expression lhs;
    private final Expression rhs;
    private final iBdc       bdc;

    public Conditional (String conditional, Set<String> variables) {
        String lhs = this.extractLhs(conditional);
        String rhs = this.extractRhs(conditional);
        String bdc = this.extractBdc(conditional);
        this.lhs = new ExpressionBuilder(lhs).variables(variables).build();
        this.rhs = new ExpressionBuilder(rhs).variables(variables).build();
        this.bdc = Bdc.getBdc(bdc);

    }

    public void setVariables (Map<String, Double> variables) {
        this.lhs.setVariables(variables);
        this.rhs.setVariables(variables);
    }

    public boolean evaluate () {
        return this.bdc.compare(this.lhs.evaluate(), this.rhs.evaluate());
    }

    private String extractLhs (String conditional) {
        for (Bdc bdc : Bdc.values()) {
            if (conditional.contains(bdc.getRepresentation())) {
                return conditional.split(bdc.getRepresentation()) [0].trim();
            }
        }
        throw new IllegalArgumentException("Missing bdc in conditional");
    }

    private String extractRhs (String conditional) {
        for (Bdc bdc : Bdc.values()) {
            if (conditional.contains(bdc.getRepresentation())) {
                return conditional.split(bdc.getRepresentation()) [1].trim();
            }
        }
        throw new IllegalArgumentException("Missing bdc in conditional");
    }

    private String extractBdc (String conditional) {
        for (Bdc bdc : Bdc.values()) {
            if (conditional.contains(bdc.getRepresentation())) {
                return bdc.getRepresentation();
            }
        }
        throw new IllegalArgumentException("Missing bdc in conditional");
    }

}
