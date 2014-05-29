package batfish.grammar.cisco.interfaces;

import batfish.representation.cisco.Interface;

public class IPAddressSecondaryIFStanza implements IFStanza {

   private String _ip;
   private String _subnet;

   public IPAddressSecondaryIFStanza(String ip, String subnet) {
      _ip = ip;
      _subnet = subnet;
   }

   @Override
   public void process(Interface i) {
      i.getSecondaryIps().put(_ip, _subnet);
   }

}
