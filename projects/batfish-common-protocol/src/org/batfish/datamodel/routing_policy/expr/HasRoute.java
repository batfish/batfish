package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class HasRoute extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private PrefixSetExpr _expr;

   @JsonCreator
   public HasRoute() {
   }

   public HasRoute(PrefixSetExpr expr) {
      _expr = expr;
   }

   @Override
   public Result evaluate(Environment environment,
         AbstractRouteBuilder<?> outputRoute) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public PrefixSetExpr getExpr() {
      return _expr;
   }

   public void setExpr(PrefixSetExpr expr) {
      _expr = expr;
   }

}
