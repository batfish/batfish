package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class IncrementMetric extends IntExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _addend;

   @JsonCreator
   private IncrementMetric() {
   }

   public IncrementMetric(int addend) {
      _addend = addend;
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
      IncrementMetric other = (IncrementMetric) obj;
      if (_addend != other._addend) {
         return false;
      }
      return true;
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

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _addend;
      return result;
   }

   public void setAddend(int addend) {
      _addend = addend;
   }

}
