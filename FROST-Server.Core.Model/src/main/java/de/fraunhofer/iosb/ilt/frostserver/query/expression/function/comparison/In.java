/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.ExpressionVisitor;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.Constant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.ConstantList;

/**
 *
 * @author jab
 */
public class In extends ComparisonFunction {

    public In() {
        // Parameters added later...
    }

    public In(Expression... parameters) {
        super(parameters);
    }

    public BooleanConstant eval(Constant p1, ConstantList p2) {
        return new BooleanConstant(p2.contains(p1));
    }

    @Override
    public String toUrl() {
        return "(" + parameters.get(0).toUrl() + " in " + parameters.get(1).toUrl() + ")";
    }

    @Override
    public <O> O accept(ExpressionVisitor<O> visitor) {
        return visitor.visit(this);
    }

}