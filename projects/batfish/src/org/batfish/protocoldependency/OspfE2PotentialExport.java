package org.batfish.protocoldependency;

import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

public final class OspfE2PotentialExport extends PotentialExport {

   public OspfE2PotentialExport(String node, Prefix prefix,
         DependentRoute dependentRoute) {
      super(node, prefix, RoutingProtocol.OSPF_E2, dependentRoute);
   }

}
