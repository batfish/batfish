package org.batfish.representation.cisco;

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

   public String getPeerTemplateName() {
      return _peerTemplateName;
   }

   public void setGroupName(String name) throws IllegalArgumentException {
      if (_peerTemplateName != null) {
         throw new IllegalArgumentException("Peer Template name has been set.");
      }
      _groupName = name;
   }

   public void setPeerTemplateName(String name) throws IllegalArgumentException {
      if (_groupName != null) {
         throw new IllegalArgumentException("Group name has been set.");
      }
      _peerTemplateName = name;
   }

}
