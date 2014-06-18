package batfish.grammar.cisco.routemap;

import batfish.representation.cisco.RouteMapClause;
import batfish.representation.cisco.RouteMapSetLine;

public class SetRMStanza implements RMStanza {
   private RouteMapSetLine _line;

   public SetRMStanza(RouteMapSetLine line) {
      _line = line;
   }

   @Override
   public void process(RouteMapClause clause) {
      clause.addSetLine(_line);
   }

}
