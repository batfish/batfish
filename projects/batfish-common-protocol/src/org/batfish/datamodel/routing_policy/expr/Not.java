package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Not extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private BooleanExpr _expr;

   @JsonCreator
   public Not() {
   }

   public Not(BooleanExpr expr) {
      _expr = expr;
   }

   public BooleanExpr getExpr() {
      return _expr;
   }

   public void setExpr(BooleanExpr expr) {
      _expr = expr;
   }

}
