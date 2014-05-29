package batfish.grammar.cisco.routemap;

import batfish.grammar.cisco.Stanza;
import batfish.representation.LineAction;
import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.RouteMapClause;

public class RouteMapStanza implements Stanza {
   
   private RouteMapClause _clause;
   
   public RouteMapStanza(LineAction action, String name, int num) {
      _clause = new RouteMapClause(action, name, num);
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
      c.addRouteMapClause(_clause);
   }

   public void processStanza(RMStanza rms) {
      if (rms == null) {
         return;
      }
      rms.process(_clause);
   }
}
