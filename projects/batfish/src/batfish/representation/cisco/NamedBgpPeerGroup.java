package batfish.representation.cisco;

import java.util.LinkedHashSet;
import java.util.Set;

import batfish.representation.Ip;

public class NamedBgpPeerGroup extends BgpPeerGroup {

   private String _name;
   private Set<Ip> _neighborAddresses;

   public NamedBgpPeerGroup(String name) {
      _neighborAddresses = new LinkedHashSet<Ip>();
      _routeReflectorClient = false;
      _defaultOriginate = false;
      _sendCommunity = false;
      _name = name;
   }

   public void addNeighborAddress(Ip address) {
      _neighborAddresses.add(address);
   }

   @Override
   public String getName() {
      return _name;
   }

   public Set<Ip> getNeighborAddresses() {
      return _neighborAddresses;
   }

}
