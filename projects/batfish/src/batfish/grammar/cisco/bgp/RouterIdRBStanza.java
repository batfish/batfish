package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpProcess;

public class RouterIdRBStanza implements RBStanza {

//   private String _routerId;

   public RouterIdRBStanza(String id) {
//      _routerId = id;
   }

   @Override
   public void process(BgpProcess p) {
//      p.setRouterId(_routerId);
   }

}
