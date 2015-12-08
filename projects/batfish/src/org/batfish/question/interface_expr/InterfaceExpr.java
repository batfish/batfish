package org.batfish.question.interface_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.Interface;

public interface InterfaceExpr extends Expr {

   @Override
   public Interface evaluate(Environment environment);

}
