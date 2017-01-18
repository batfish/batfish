package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.IntExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetWeight extends Statement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IntExpr _weight;

   @JsonCreator
   private SetWeight() {
   }

   public SetWeight(IntExpr weight) {
      _weight = weight;
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
      SetWeight other = (SetWeight) obj;
      if (_weight == null) {
         if (other._weight != null) {
            return false;
         }
      }
      else if (!_weight.equals(other._weight)) {
         return false;
      }
      return true;
   }

   @Override
   public Result execute(Environment environment) {
      Result result = new Result();
      int weight = _weight.evaluate(environment);
      BgpRoute.Builder bgpRouteBuilder = (BgpRoute.Builder) environment
            .getOutputRoute();
      bgpRouteBuilder.setWeight(weight);
      return result;
   }

   public IntExpr getWeight() {
      return _weight;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_weight == null) ? 0 : _weight.hashCode());
      return result;
   }

   public void setWeight(IntExpr weight) {
      _weight = weight;
   }

}
