package batfish.grammar.cisco.interfaces;

import batfish.representation.cisco.Interface;


public class IpAccessGroupOutIFStanza implements IFStanza {

   private String _accessListName;

   public IpAccessGroupOutIFStanza(String accessListName) {
      _accessListName = accessListName;
   }

   @Override
   public void process(Interface i) {
      i.setOutgoingFilter(_accessListName);
   }

}
