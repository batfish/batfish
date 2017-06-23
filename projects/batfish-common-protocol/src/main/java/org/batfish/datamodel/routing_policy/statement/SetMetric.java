package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.IntExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetMetric extends Statement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IntExpr _metric;

   @JsonCreator
   private SetMetric() {
   }

   public SetMetric(IntExpr metric) {
      _metric = metric;
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
      SetMetric other = (SetMetric) obj;
      if (_metric == null) {
         if (other._metric != null) {
            return false;
         }
      }
      else if (!_metric.equals(other._metric)) {
         return false;
      }
      return true;
   }

   @Override
   public Result execute(Environment environment) {
      Result result = new Result();
      int metric = _metric.evaluate(environment);
      environment.getOutputRoute().setMetric(metric);
      if (environment.getWriteToIntermediateBgpAttributes()) {
         environment.getIntermediateBgpAttributes().setMetric(metric);
      }
      return result;
   }

   public IntExpr getMetric() {
      return _metric;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_metric == null) ? 0 : _metric.hashCode());
      return result;
   }

   public void setMetric(IntExpr metric) {
      _metric = metric;
   }

}
