package batfish.grammar.cisco.bgp;

//import batfish.representation.cisco.BgpNetwork;
import batfish.representation.cisco.BgpProcess;

public class NetworkRBStanza implements RBStanza {
//   private String _ip;
//   private String _mask;
//   private BgpNetwork _network;

   public NetworkRBStanza(String ip, String mask) {
//      _ip = ip;
//      if (mask.equals("")) {
//         _mask = getClassMask(ip);
//      }
//      else {
//         _mask = mask;
//      }
//      _network = new BgpNetwork(_ip, _mask);
   }

   public String getClassMask(String ip) {
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
   public void process(BgpProcess p) {
//      p.addNetwork(_network);
   }
}
