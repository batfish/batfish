package batfish.representation.cisco;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapSetLine;
import batfish.representation.PolicyMapSetMetricLine;

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
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      return new PolicyMapSetMetricLine(_metric);
   }

   @Override
   public RouteMapSetType getType(){
      return RouteMapSetType.METRIC;
   }
   
}
