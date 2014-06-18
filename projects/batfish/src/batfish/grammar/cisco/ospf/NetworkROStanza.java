package batfish.grammar.cisco.ospf;

import batfish.representation.cisco.OspfProcess;

public class NetworkROStanza implements ROStanza {

   private int _area;
   private String _networkAddress;
   private String _subnetMask;

   public NetworkROStanza(String networkAddress, String subnetMask, int area) {
      _networkAddress = networkAddress;
      _subnetMask = subnetMask;
      _area = area;
   }

   @Override
   public void process(OspfProcess p) {
      p.getWildcardNetworks().add(
            new OSPFWildcardNetwork(_networkAddress, _subnetMask, _area));
   }
}
