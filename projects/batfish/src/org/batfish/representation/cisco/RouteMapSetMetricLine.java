package org.batfish.representation.cisco;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapSetLine;
import org.batfish.representation.PolicyMapSetMetricLine;

public class RouteMapSetMetricLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private int _metric;

   public RouteMapSetMetricLine(int metric) {
      _metric = metric;
   }

   public int getMetric() {
      return _metric;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.METRIC;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      return new PolicyMapSetMetricLine(_metric);
   }

}
