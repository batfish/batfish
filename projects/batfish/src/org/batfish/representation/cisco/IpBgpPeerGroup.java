package org.batfish.representation.cisco;

import org.batfish.representation.Ip;

public class IpBgpPeerGroup extends BgpPeerGroup {

   private static final long serialVersionUID = 1L;
   private String _groupName = null;
   private Ip _ip;
   private String _peerTemplateName = null;

   public IpBgpPeerGroup(Ip ip) {
      _ip = ip;
   }

   public String getGroupName() {
      return _groupName;
   }

   public Ip getIp() {
      return _ip;
   }

   @Override
   public String getName() {
      return _ip.toString();
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
