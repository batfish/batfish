package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.IntExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetWeight extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IntExpr _weight;

   @JsonCreator
   public SetWeight() {
   }

   public SetWeight(IntExpr weight) {
      _weight = weight;
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

   public void setWeight(IntExpr weight) {
      _weight = weight;
   }

}
