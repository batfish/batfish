package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class IncrementMetric implements IntExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _addend;

   @JsonCreator
   public IncrementMetric() {
   }

   public IncrementMetric(int addend) {
      _addend = addend;
   }

   @Override
   public int evaluate(Environment environment) {
      int oldMetric = environment.getOriginalRoute().getMetric();
      int newVal = oldMetric + _addend;
      return newVal;
   }

   public int getAddend() {
      return _addend;
   }

   public void setAddend(int addend) {
      _addend = addend;
   }

}
