package org.batfish.question.prefix_set_expr;

import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.Prefix;

public interface PrefixSetExpr extends Expr {

   @Override
   public Set<Prefix> evaluate(Environment environment);

}
