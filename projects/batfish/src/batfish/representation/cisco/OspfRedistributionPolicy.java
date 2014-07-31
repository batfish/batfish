package batfish.representation.cisco;

import batfish.representation.OspfMetricType;
import batfish.representation.Protocol;

public class OspfRedistributionPolicy extends RedistributionPolicy {

   private static final long serialVersionUID = 1L;

   public static final String BGP_AS = "BGP_AS";
   public static final OspfMetricType DEFAULT_METRIC_TYPE = OspfMetricType.E2;
   public static final int DEFAULT_REDISTRIBUTE_CONNECTED_METRIC = 20;
   public static final int DEFAULT_REDISTRIBUTE_STATIC_METRIC = 20;

   private String _map;
   private Integer _metric;
   private OspfMetricType _metricType;
   private boolean _subnets;
   private Long _tag;

   public OspfRedistributionPolicy(Protocol sourceProtocol) {
      super(sourceProtocol, Protocol.OSPF);
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
