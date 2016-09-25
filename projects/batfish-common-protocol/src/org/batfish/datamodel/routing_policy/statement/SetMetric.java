package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetMetric extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private int _metric;

   @JsonCreator
   public SetMetric() {
   }

   public SetMetric(int metric) {
      _metric = metric;
   }

   @Override
   public Result execute(Environment environment) {
      Result result = new Result();
      environment.getOutputRoute().setMetric(_metric);
      return result;
   }

   public int getMetric() {
      return _metric;
   }

   public void setMetric(int metric) {
      _metric = metric;
   }

}
