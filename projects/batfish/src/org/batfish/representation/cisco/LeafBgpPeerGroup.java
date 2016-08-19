package org.batfish.representation.cisco;

import org.batfish.datamodel.Prefix;

public abstract class LeafBgpPeerGroup extends BgpPeerGroup {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _groupName = null;

   private String _peerTemplateName = null;

   public String getGroupName() {
      return _groupName;
   }

   public abstract Prefix getNeighborPrefix();

   @Override
   protected BgpPeerGroup getParent(BgpProcess proc, CiscoVendorConfiguration cv) {
      BgpPeerGroup parent = null;
      if (_groupName != null) {
         parent = proc.getNamedPeerGroups().get(_groupName);
         if (parent == null) {
            cv.undefined("Reference to undefined parent peer group: '"
                  + _groupName + "'", CiscoVendorConfiguration.BGP_PEER_GROUP,
                  _groupName);
         }
      }
      if (parent == null) {
         parent = proc.getMasterBgpPeerGroup();
      }
      return parent;
   }

   public void setGroupName(String name) throws IllegalArgumentException {
      if (_peerTemplateName != null) {
         throw new IllegalArgumentException("Group name has been set.");
      }
      _groupName = name;
   }

}
