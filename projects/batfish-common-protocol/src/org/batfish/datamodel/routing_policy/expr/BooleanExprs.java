package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum BooleanExprs {
   CallExprContext,
   CallStatementContext,
   False,
   True;

   public static class StaticBooleanExpr extends BooleanExpr {
      /**
       *
       */
      private static final long serialVersionUID = 1L;

      private static final String TYPE_VAR = "type";

      private BooleanExprs _type;

      @JsonCreator
      public StaticBooleanExpr(@JsonProperty(TYPE_VAR) BooleanExprs type) {
         _type = type;
      }

      @Override
      public boolean equals(Object rhs) {
         if (rhs instanceof StaticBooleanExpr) {
            return _type.equals(((StaticBooleanExpr) rhs)._type);
         }
         return false;
      }

      @Override
      public Result evaluate(Environment environment) {
         Result result = new Result();
         switch (_type) {
         case CallExprContext:
            result.setBooleanValue(environment.getCallExprContext());
            break;
         case CallStatementContext:
            result.setBooleanValue(environment.getCallStatementContext());
            break;
         case False:
            result.setBooleanValue(false);
            break;
         case True:
            result.setBooleanValue(true);
            break;
         }
         return result;
      }

      @JsonProperty(TYPE_VAR)
      public BooleanExprs getType() {
         return _type;
      }

      @Override
      public int hashCode() {
         return _type.ordinal();
      }

   }

   public StaticBooleanExpr toStaticBooleanExpr() {
      return new StaticBooleanExpr(this);
   }
}
