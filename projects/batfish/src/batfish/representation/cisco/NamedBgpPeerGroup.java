package batfish.representation.cisco;

import java.util.LinkedHashSet;
import java.util.Set;

import batfish.representation.Ip;

public class NamedBgpPeerGroup extends BgpPeerGroup {

   private static final long serialVersionUID = 1L;

   private boolean _created;

   private String _name;

   private Set<Ip> _neighborAddresses;

   public NamedBgpPeerGroup(String name) {
      _neighborAddresses = new LinkedHashSet<Ip>();
      _name = name;
   }

   public void addNeighborAddress(Ip address) {
      _neighborAddresses.add(address);
   }

   public boolean getCreated() {
      return _created;
   }

   @Override
   public String getName() {
      return _name;
   }

   public Set<Ip> getNeighborAddresses() {
      return _neighborAddresses;
   }

   public void setCreated(boolean b) {
      _created = b;
   }

}
