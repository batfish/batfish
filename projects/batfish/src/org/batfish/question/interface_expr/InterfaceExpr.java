package org.batfish.question.interface_expr;

import org.batfish.datamodel.Interface;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface InterfaceExpr extends Expr {

   @Override
   public Interface evaluate(Environment environment);

}
