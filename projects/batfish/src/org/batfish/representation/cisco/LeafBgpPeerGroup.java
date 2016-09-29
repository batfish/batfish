package org.batfish.representation.cisco;

import org.batfish.datamodel.Prefix;

public abstract class LeafBgpPeerGroup extends BgpPeerGroup {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _groupName = null;

   private String _peerSession = null;

   public String getGroupName() {
      return _groupName;
   }

   public abstract Prefix getNeighborPrefix();

   @Override
   protected BgpPeerGroup getParent(BgpProcess proc,
         CiscoVendorConfiguration cv) {
      BgpPeerGroup parent = null;
      if (_groupName != null) {
         parent = proc.getNamedPeerGroups().get(_groupName);
         if (parent == null) {
            cv.undefined(
                  "Reference to undefined parent peer group: '" + _groupName
                        + "'",
                  CiscoVendorConfiguration.BGP_PEER_GROUP, _groupName);
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

   public void setGroupName(String name) throws IllegalArgumentException {
      if (_groupName != null) {
         throw new IllegalArgumentException("Group name has been set.");
      }
      _groupName = name;
   }

   public void setPeerSession(String peerSession) {
      if (_peerSession != null) {
         throw new IllegalArgumentException("Peer-session name has been set.");
      }
      _peerSession = peerSession;
   }

}
