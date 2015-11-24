package org.batfish.protocoldependency;

import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

public final class OspfIntraAreaPotentialExport extends PotentialExport {

   private final long _areaNum;

   public OspfIntraAreaPotentialExport(String node, Prefix prefix,
         DependentRoute dependentRoute, long areaNum) {
      super(node, prefix, RoutingProtocol.OSPF, dependentRoute);
      _areaNum = areaNum;
   }

   public long getAreaNum() {
      return _areaNum;
   }

}
