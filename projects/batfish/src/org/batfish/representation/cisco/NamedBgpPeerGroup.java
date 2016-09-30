package org.batfish.representation.cisco;

import java.util.LinkedHashSet;
import java.util.Set;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class NamedBgpPeerGroup extends BgpPeerGroup {

   private static final long serialVersionUID = 1L;

   private boolean _created;

   private String _name;

   private Set<Ip> _neighborAddresses;

   private Set<Prefix> _neighborPrefixes;

   public NamedBgpPeerGroup(String name) {
      _neighborAddresses = new LinkedHashSet<>();
      _neighborPrefixes = new LinkedHashSet<>();
      _name = name;
   }

   public void addNeighborAddress(Ip address) {
      _neighborAddresses.add(address);
   }

   public void addNeighborPrefix(Prefix prefix) {
      _neighborPrefixes.add(prefix);
   }

   public boolean getCreated() {
      return _created;
   }

   @Override
   public String getName() {
      return _name;
   }

   public void setCreated(boolean b) {
      _created = b;
   }

}
