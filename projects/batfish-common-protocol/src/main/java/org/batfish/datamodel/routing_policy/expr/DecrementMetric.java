package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class DecrementMetric extends IntExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _subtrahend;

   @JsonCreator
   private DecrementMetric() {
   }

   public DecrementMetric(int subtrahend) {
      _subtrahend = subtrahend;
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
      DecrementMetric other = (DecrementMetric) obj;
      if (_subtrahend != other._subtrahend) {
         return false;
      }
      return true;
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

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _subtrahend;
      return result;
   }

   public void setSubtrahend(int subtrahend) {
      _subtrahend = subtrahend;
   }

}
