package org.batfish.z3.node;

public class TrueExpr extends BooleanExpr {

   public static final TrueExpr INSTANCE = new TrueExpr();

   private TrueExpr() {
      _printer = new SimpleExprPrinter("true");
   }

}
