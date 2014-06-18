package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;
import batfish.representation.cisco.BgpNetwork;

public class NetworkAFStanza implements AFStanza {
   private String _ip;
   private String _mask;

   public NetworkAFStanza(String ip, String mask) {
      _ip = ip;
      if (mask.equals("")) {
         _mask = getClassMask(ip);
      }
      else {
         _mask = mask;
      }
   }

   private String getClassMask(String ip) {
      String firstOctetStr = ip.substring(0, ip.indexOf("."));
      int firstOctet = Integer.parseInt(firstOctetStr);
      if (firstOctet <= 126) {
         return "255.0.0.0";
      }
      else if (firstOctet >= 128 && firstOctet <= 191) {
         return "255.255.0.0";
      }
      else if (firstOctet >= 192 && firstOctet <= 223) {
         return "255.255.255.0";
      }
      else {
         return null;
      }
   }

   @Override
   public void process(BgpAddressFamily af) {
      af.getNetworks().add(new BgpNetwork(_ip, _mask));
   }
}
