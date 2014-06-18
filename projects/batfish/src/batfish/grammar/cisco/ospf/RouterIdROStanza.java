package batfish.grammar.cisco.ospf;

import batfish.representation.cisco.OspfProcess;

public class RouterIdROStanza implements ROStanza {

   private String _id;

   public RouterIdROStanza(String id) {
      _id = id;
   }

   @Override
   public void process(OspfProcess p) {
      p.setRouterId(_id);
   }

}
