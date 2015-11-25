package org.batfish.protocoldependency;

import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

public class EbgpPotentialExport extends PotentialExport {

   public EbgpPotentialExport(String node, Prefix prefix,
         DependentRoute dependency) {
      super(node, prefix, RoutingProtocol.BGP, dependency);
   }

}
