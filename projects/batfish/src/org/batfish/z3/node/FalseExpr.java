package org.batfish.z3.node;

public class FalseExpr extends BooleanExpr {

   public static final FalseExpr INSTANCE = new FalseExpr();

   private FalseExpr() {
      _printer = new SimpleExprPrinter("false");
   }

}
