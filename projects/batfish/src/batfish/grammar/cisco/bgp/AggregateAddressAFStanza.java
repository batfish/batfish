package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;
//import batfish.representation.cisco.BgpNetwork;

public class AggregateAddressAFStanza implements AFStanza {

//   private String _network;
//   private String _subnet;
//   private boolean _summaryOnly;

   public AggregateAddressAFStanza(String network, String subnet,
         boolean summaryOnly) {
//      _network = network;
//      _subnet = subnet;
//      _summaryOnly = summaryOnly;
   }

   @Override
   public void process(BgpAddressFamily af) {
//      BgpNetwork net = new BgpNetwork(_network, _subnet);
//      af.getAggregateNetworks().put(net, _summaryOnly);
   }

}
