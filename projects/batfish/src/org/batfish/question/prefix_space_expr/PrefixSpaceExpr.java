package org.batfish.question.prefix_space_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.PrefixSpace;

public interface PrefixSpaceExpr extends Expr {

   @Override
   public PrefixSpace evaluate(Environment environment);

}
