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

   private String _peerSession;

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

   @Override
   protected final BgpPeerGroup getParent(BgpProcess proc,
         CiscoVendorConfiguration cv) {
      BgpPeerGroup parent = null;
      if (_peerSession != null) {
         parent = proc.getPeerSessions().get(_peerSession);
         if (parent == null) {
            cv.undefined(
                  "Reference to undefined peer-session: '" + _peerSession + "'",
                  CiscoVendorConfiguration.BGP_PEER_GROUP, _peerSession);
         }
      }
      if (parent == null) {
         parent = proc.getMasterBgpPeerGroup();
      }
      return parent;
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
