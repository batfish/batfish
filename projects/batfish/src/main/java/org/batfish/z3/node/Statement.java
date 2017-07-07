package org.batfish.z3.node;

public abstract class Statement extends Expr {

   @Override
   public Statement simplify() {
      return this;
   }

}
