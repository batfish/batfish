package org.batfish.question.string_set_expr;

import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface StringSetExpr extends Expr {

   @Override
   public Set<String> evaluate(Environment environment);

}
