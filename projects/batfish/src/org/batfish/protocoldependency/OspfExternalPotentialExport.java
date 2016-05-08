package org.batfish.protocoldependency;

import org.batfish.common.datamodel.Prefix;
import org.batfish.common.datamodel.RoutingProtocol;

public class OspfExternalPotentialExport extends PotentialExport {

   public OspfExternalPotentialExport(String node, Prefix prefix,
         RoutingProtocol protocol, DependentRoute dependency) {
      super(node, prefix, protocol, dependency);
   }

}
