package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class MatchMetric extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _metric;

   public MatchMetric(int metric) {
      _metric = metric;
   }

   @Override
   public Result evaluate(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public int getMetric() {
      return _metric;
   }

   public void setMetric(int metric) {
      _metric = metric;
   }

}
