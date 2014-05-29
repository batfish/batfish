package batfish.grammar.cisco.ospf;

import batfish.representation.cisco.OspfProcess;

public interface ROStanza {
   void process(OspfProcess p);
}
