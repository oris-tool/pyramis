/* This program is part of the PYRAMIS library for compositional analysis of hierarchical UML statecharts.
 * Copyright (C) 2019-2023 The PYRAMIS Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.unifi.hierarchical.utils;


import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.EXP;
import org.oristool.math.function.Function;

import java.math.BigDecimal;

/**
 * The hypoExponential PDF with two parameters (i.e., sequence of two Exponential PDFs, each with their own rate).
 */
public class TwoParameterHypoEXP implements Function {

    private final EXP first;
    private final EXP second;

    /**
     * Builds the function {@code (lambda1 lambda2/(lambda1-lambda2)) (e^(-lambda2 x)-e^(-lambda1 x))} over {@code [0, +infty)}.
     *
     * @param lambda1 rate of the first Exponential PDF (before the negation)
     * @param lambda2 rate of the second Exponential PDF (before the negation)
     */
    public TwoParameterHypoEXP(BigDecimal lambda1, BigDecimal lambda2) {
        first = new EXP(Variable.X, lambda1);
        second = new EXP(Variable.X, lambda2);
    }

    public EXP getFirst() {
        return first;
    }

    public EXP getSecond() {
        return second;
    }

    // FIXME: Where is the density function built?
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
        return null;
    }
}
