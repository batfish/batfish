package org.batfish.z3.node;

public abstract class ComplexExprPrinter extends ExprPrinter {

   protected ComplexExpr _expr;

   public ComplexExprPrinter(ComplexExpr expr) {
      _expr = expr;
   }

}
