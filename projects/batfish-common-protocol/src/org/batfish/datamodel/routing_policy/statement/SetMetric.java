package org.batfish.datamodel.routing_policy.statement;

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

   public int getMetric() {
      return _metric;
   }

   public void setMetric(int metric) {
      _metric = metric;
   }

}
