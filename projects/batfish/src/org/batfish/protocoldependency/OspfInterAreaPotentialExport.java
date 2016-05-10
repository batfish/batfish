package org.batfish.protocoldependency;

import java.util.Set;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;

public final class OspfInterAreaPotentialExport extends PotentialExport {

   private final Set<Long> _areaNumbers;

   public OspfInterAreaPotentialExport(String node, Prefix prefix,
         DependentRoute dependentRoute, Set<Long> areaNumbers) {
      super(node, prefix, RoutingProtocol.OSPF_IA, dependentRoute);
      _areaNumbers = areaNumbers;
   }

   public Set<Long> getAreaNumbers() {
      return _areaNumbers;
   }

}
