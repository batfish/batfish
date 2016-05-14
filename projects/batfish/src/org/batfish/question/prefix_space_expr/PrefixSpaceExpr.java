package org.batfish.question.prefix_space_expr;

import org.batfish.datamodel.PrefixSpace;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface PrefixSpaceExpr extends Expr {

   @Override
   public PrefixSpace evaluate(Environment environment);

}
