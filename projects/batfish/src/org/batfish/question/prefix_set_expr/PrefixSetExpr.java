package org.batfish.question.prefix_set_expr;

import java.util.Set;

import org.batfish.common.datamodel.Prefix;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface PrefixSetExpr extends Expr {

   @Override
   public Set<Prefix> evaluate(Environment environment);

}
