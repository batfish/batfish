package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class DecrementMetric implements IntExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _subtrahend;

   @JsonCreator
   public DecrementMetric() {
   }

   public DecrementMetric(int subtrahend) {
      _subtrahend = subtrahend;
   }

   @Override
   public int evaluate(Environment environment) {
      int oldMetric = environment.getOriginalRoute().getMetric();
      int newVal = oldMetric - _subtrahend;
      return newVal;
   }

   public int getSubtrahend() {
      return _subtrahend;
   }

   public void setSubtrahend(int subtrahend) {
      _subtrahend = subtrahend;
   }

}
