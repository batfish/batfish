package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapSetMetricLine extends PolicyMapSetLine {

   private static final String METRIC_VAR = "metric";

   private static final long serialVersionUID = 1L;

   private final int _metric;

   @JsonCreator
   public PolicyMapSetMetricLine(@JsonProperty(METRIC_VAR) int metric) {
      _metric = metric;
   }

   @JsonProperty(METRIC_VAR)
   public int getMetric() {
      return _metric;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.METRIC;
   }

}
