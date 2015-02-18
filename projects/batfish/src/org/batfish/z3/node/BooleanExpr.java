package org.batfish.z3.node;

public abstract class BooleanExpr extends Expr {

   @Override
   public BooleanExpr simplify() {
      return this;
   }

}
