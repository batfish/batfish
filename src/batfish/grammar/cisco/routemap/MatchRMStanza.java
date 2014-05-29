package batfish.grammar.cisco.routemap;

import batfish.representation.cisco.RouteMapClause;
import batfish.representation.cisco.RouteMapMatchLine;

public class MatchRMStanza implements RMStanza {
   private RouteMapMatchLine _line;

   public MatchRMStanza(RouteMapMatchLine line) {
      _line = line;
   }

   @Override
   public void process(RouteMapClause clause) {
      clause.addMatchLine(_line);
   }

}
