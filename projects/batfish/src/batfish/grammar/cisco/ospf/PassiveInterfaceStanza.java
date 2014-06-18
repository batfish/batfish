package batfish.grammar.cisco.ospf;

import batfish.representation.cisco.OspfProcess;

public class PassiveInterfaceStanza implements ROStanza {

   private String _iname;
   private boolean _passive;
   
   public PassiveInterfaceStanza(String iname, boolean passive) {
      _iname = iname;
      _passive = passive;
   }

   @Override
   public void process(OspfProcess p) {
      if (_passive) {
         p.getInterfaceBlacklist().add(_iname);
      }
      else {
         p.getInterfaceWhitelist().add(_iname);
      }
   }

}
