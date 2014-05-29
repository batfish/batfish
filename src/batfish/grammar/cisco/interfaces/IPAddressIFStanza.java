package batfish.grammar.cisco.interfaces;

import batfish.representation.cisco.Interface;

public class IPAddressIFStanza implements IFStanza {
   
   private String _ip;
   private String _subnet;
   
   public IPAddressIFStanza(String ip, String subnet) {
      _ip = ip;
      _subnet = subnet;
   }
   
   @Override
   public void process(Interface i) {
      i.setIP(_ip);
      i.setSubnetMask(_subnet);
   }
}
