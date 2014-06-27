package batfish.grammar.cisco.bgp;

//import batfish.representation.cisco.BgpNetwork;
import batfish.representation.cisco.BgpProcess;

public class AggregateAddressRBStanza implements RBStanza {
   
//   private String _network;
//   private String _subnet;
//   private boolean _summaryOnly;

   public AggregateAddressRBStanza(String network, String subnet,
         boolean summaryOnly) {
//      _network = network;
//      _subnet = subnet;
//      _summaryOnly = summaryOnly;
   }

   @Override
   public void process(BgpProcess p) {
//      BgpNetwork net = new BgpNetwork(_network, _subnet);
//      p.getAggregateNetworks().put(net, _summaryOnly);

   }

}
