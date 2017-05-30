package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchLocalPreference extends BooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IntComparator _comparator;

   private IntExpr _metric;

   @JsonCreator
   private MatchLocalPreference() {
   }

   public MatchLocalPreference(IntComparator comparator, IntExpr metric) {
      _comparator = comparator;
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
      MatchLocalPreference other = (MatchLocalPreference) obj;
      if (_comparator != other._comparator) {
         return false;
      }
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
   public Result evaluate(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public IntComparator getComparator() {
      return _comparator;
   }

   public IntExpr getMetric() {
      return _metric;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((_comparator == null) ? 0 : _comparator.hashCode());
      result = prime * result + ((_metric == null) ? 0 : _metric.hashCode());
      return result;
   }

   public void setComparator(IntComparator comparator) {
      _comparator = comparator;
   }

   public void setMetric(IntExpr metric) {
      _metric = metric;
   }

}
