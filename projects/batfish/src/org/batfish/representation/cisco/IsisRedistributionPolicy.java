package org.batfish.representation.cisco;

import org.batfish.representation.IsisLevel;
import org.batfish.representation.RoutingProtocol;

public class IsisRedistributionPolicy extends RedistributionPolicy {

   public static final IsisLevel DEFAULT_LEVEL = IsisLevel.LEVEL_2;

   public static final Integer DEFAULT_REDISTRIBUTE_CONNECTED_METRIC = 10;

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private IsisLevel _level;

   private String _map;

   private Integer _metric;

   public IsisRedistributionPolicy(RoutingProtocol sourceProtocol) {
      super(sourceProtocol, RoutingProtocol.ISIS);
   }

   public IsisLevel getLevel() {
      return _level;
   }

   public String getMap() {
      return _map;
   }

   public Integer getMetric() {
      return _metric;
   }

   public void setLevel(IsisLevel level) {
      _level = level;
   }

   public void setMap(String map) {
      _map = map;
   }

   public void setMetric(int metric) {
      _metric = metric;
   }

}
