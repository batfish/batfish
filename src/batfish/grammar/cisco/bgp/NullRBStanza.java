package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpProcess;

public class NullRBStanza implements RBStanza {

   @Override
   public void process(BgpProcess p) {
   }

}
