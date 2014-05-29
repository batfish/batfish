package batfish.grammar.cisco.routemap;

import batfish.representation.cisco.RouteMapClause;

public interface RMStanza {
   void process(RouteMapClause clause);
}
