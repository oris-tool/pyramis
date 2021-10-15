package it.unifi.hierarchical.analysis;

import java.math.BigDecimal;

import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.EXP;
import org.oristool.math.function.Function;

/**
 * Sequence of Erlang or Exponential PDFs.
 */
public class ErlangExp implements Function {

    private EXP first;
    private EXP second;

    /**
     * Builds the function {@code e^(-lambda x)} over {@code [0, +infty)}.
     *
     * @param x PDF variable
     * @param lambda rate (before the negation)
     */
    public ErlangExp(BigDecimal lambda1, BigDecimal lambda2) {
    	first = new EXP(Variable.X, lambda1);
    	second = new EXP(Variable.X, lambda2);
    }

   
    public EXP getFirst() {
    	return first;
    }
    public EXP getSecond() {
    	return second;
    }


   

    @Override
    public Expolynomial getDensity() {
        return null;
    }

    @Override
    public DBMZone getDomain() {
        return null;
    }


	@Override
	public String toMathematicaString() {
		// TODO Auto-generated method stub
		return null;
	}
}
