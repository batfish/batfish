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


   @Override
   public void process(BgpProcess p) {
//      p.addNetwork(_network);
   }
}
