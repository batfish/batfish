package org.batfish.protocoldependency;

import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

public class OspfExternalPotentialExport extends PotentialExport {

   public OspfExternalPotentialExport(String node, Prefix prefix,
         RoutingProtocol protocol, DependentRoute dependency) {
      super(node, prefix, protocol, dependency);
   }

}
