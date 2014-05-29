package batfish.grammar.cisco;

import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.StaticRoute;

public class IPRouteStanza implements Stanza {

   private StaticRoute _staticRoute;

   public IPRouteStanza(String prefix, String mask, String nextHopIp,
         String nextHopInterface, int distance) {
      _staticRoute = new StaticRoute(prefix, mask, nextHopIp, nextHopInterface,
            distance);
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
      c.addStaticRoute(_staticRoute);

   }

}
