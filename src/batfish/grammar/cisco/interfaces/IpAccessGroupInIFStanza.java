package batfish.grammar.cisco.interfaces;

import batfish.representation.cisco.Interface;


public class IpAccessGroupInIFStanza implements IFStanza {

   private String _accessListName;

   public IpAccessGroupInIFStanza(String accessListName) {
      _accessListName = accessListName;
   }

   @Override
   public void process(Interface i) {
      i.setIncomingFilter(_accessListName);
   }

}
