package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class HasRoute extends BooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private PrefixSetExpr _expr;

   @JsonCreator
   private HasRoute() {
   }

   public HasRoute(PrefixSetExpr expr) {
      _expr = expr;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      HasRoute other = (HasRoute) obj;
      if (_expr == null) {
         if (other._expr != null) {
            return false;
         }
      }
      else if (!_expr.equals(other._expr)) {
         return false;
      }
      return true;
   }

   @Override
   public Result evaluate(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public PrefixSetExpr getExpr() {
      return _expr;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_expr == null) ? 0 : _expr.hashCode());
      return result;
   }

   public void setExpr(PrefixSetExpr expr) {
      _expr = expr;
   }

}
