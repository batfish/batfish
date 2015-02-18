package org.batfish.representation.cisco;

import java.util.LinkedHashSet;
import java.util.Set;

import org.batfish.representation.Ip;
import org.batfish.representation.Prefix;

public class NamedBgpPeerGroup extends BgpPeerGroup {

   private static final long serialVersionUID = 1L;

   private boolean _created;

   private String _name;

   private Set<Ip> _neighborAddresses;

   private Set<Prefix> _neighborPrefixes;

   private String _peerSession;

   public NamedBgpPeerGroup(String name) {
      _neighborAddresses = new LinkedHashSet<Ip>();
      _neighborPrefixes = new LinkedHashSet<Prefix>();
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

   public String getPeerSession() {
      return _peerSession;
   }

   public void setCreated(boolean b) {
      _created = b;
   }

   public void setPeerSession(String peerSession) {
      _peerSession = peerSession;
   }

}
