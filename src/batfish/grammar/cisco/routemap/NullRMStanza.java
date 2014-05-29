package batfish.grammar.cisco.routemap;

import batfish.representation.cisco.RouteMapClause;

public class NullRMStanza implements RMStanza {

   @Override
   public void process(RouteMapClause clause) {
   }

}
