package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.datamodel.PolicyMapSetMetricLine;
import org.batfish.main.Warnings;

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
   public PolicyMapSetLine toPolicyMapSetLine(CiscoConfiguration v,
         Configuration c, Warnings w) {
      return new PolicyMapSetMetricLine(_metric);
   }

}
