package org.batfish.representation.juniper;

import org.batfish.representation.Ip;

public class IpBgpGroup extends BgpGroup {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Ip _remoteAddress;

   public IpBgpGroup(Ip remoteAddress) {
      _remoteAddress = remoteAddress;
   }

   public Ip getRemoteAddress() {
      return _remoteAddress;
   }

}
