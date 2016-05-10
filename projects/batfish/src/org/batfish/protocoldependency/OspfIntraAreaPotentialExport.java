package org.batfish.protocoldependency;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;

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
