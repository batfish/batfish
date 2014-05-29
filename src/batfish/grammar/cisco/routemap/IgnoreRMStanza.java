package batfish.grammar.cisco.routemap;

import batfish.representation.cisco.RouteMapClause;

public class IgnoreRMStanza implements RMStanza {

   @Override
   public void process(RouteMapClause clause) {
      clause.setIgnore(true);
   }

}
