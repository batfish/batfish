package org.batfish.protocoldependency;

import java.util.Set;

import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

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
