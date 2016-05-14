package org.batfish.datamodel;

public class PolicyMapSetMetricLine extends PolicyMapSetLine {

   private static final long serialVersionUID = 1L;

   private int _metric;

   public PolicyMapSetMetricLine(int metric) {
      _metric = metric;
   }

   public int getMetric() {
      return _metric;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.METRIC;
   }

}
