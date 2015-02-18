package org.batfish.representation.cisco;

import org.batfish.representation.OspfMetricType;
import org.batfish.representation.RoutingProtocol;

public class OspfRedistributionPolicy extends RedistributionPolicy {

   public static final String BGP_AS = "BGP_AS";

   public static final OspfMetricType DEFAULT_METRIC_TYPE = OspfMetricType.E2;
   public static final int DEFAULT_REDISTRIBUTE_CONNECTED_METRIC = 20;
   public static final int DEFAULT_REDISTRIBUTE_STATIC_METRIC = 20;
   private static final long serialVersionUID = 1L;

   private String _map;
   private Integer _metric;
   private OspfMetricType _metricType;
   private boolean _subnets;
   private Long _tag;

   public OspfRedistributionPolicy(RoutingProtocol sourceProtocol) {
      super(sourceProtocol, RoutingProtocol.OSPF);
   }

   public String getMap() {
      return _map;
   }

   public Integer getMetric() {
      return _metric;
   }

   public OspfMetricType getMetricType() {
      return _metricType;
   }

   public boolean getSubnets() {
      return _subnets;
   }

   public Long getTag() {
      return _tag;
   }

   public void setMap(String name) {
      _map = name;
   }

   public void setMetric(int metric) {
      _metric = metric;
   }

   public void setOspfMetricType(OspfMetricType type) {
      _metricType = type;
   }

   public void setSubnets(boolean b) {
      _subnets = b;
   }

   public void setTag(long tag) {
      _tag = tag;
   }

}
