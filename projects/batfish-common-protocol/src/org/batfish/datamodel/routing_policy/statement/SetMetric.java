package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.IntExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetMetric extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IntExpr _metric;

   @JsonCreator
   public SetMetric() {
   }

   public SetMetric(IntExpr metric) {
      _metric = metric;
   }

   @Override
   public Result execute(Environment environment) {
      Result result = new Result();
      int metric = _metric.evaluate(environment);
      environment.getOutputRoute().setMetric(metric);
      return result;
   }

   public IntExpr getMetric() {
      return _metric;
   }

   public void setMetric(IntExpr metric) {
      _metric = metric;
   }

}
