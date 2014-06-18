package batfish.grammar.cisco.ospf;

import batfish.representation.cisco.OspfProcess;

public class PassiveInterfaceDefaultROStanza implements ROStanza {

   @Override
   public void process(OspfProcess p) {
      p.setPassiveInterfaceDefault(true);
   }

}
