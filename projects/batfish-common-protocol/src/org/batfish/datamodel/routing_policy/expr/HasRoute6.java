package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class HasRoute6 extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Prefix6SetExpr _expr;

   @JsonCreator
   public HasRoute6() {
   }

   public HasRoute6(Prefix6SetExpr expr) {
      _expr = expr;
   }

   @Override
   public Result evaluate(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public Prefix6SetExpr getExpr() {
      return _expr;
   }

   public void setExpr(Prefix6SetExpr expr) {
      _expr = expr;
   }

}
