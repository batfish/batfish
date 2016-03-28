package org.batfish.representation.cisco;

import org.batfish.representation.Ip;
import org.batfish.representation.Prefix;

public class IpBgpPeerGroup extends LeafBgpPeerGroup {

   private static final long serialVersionUID = 1L;

   private Ip _ip;

   public IpBgpPeerGroup(Ip ip) {
      _ip = ip;
   }

   public Ip getIp() {
      return _ip;
   }

   @Override
   public String getName() {
      return _ip.toString();
   }

   @Override
   public Prefix getNeighborPrefix() {
      return new Prefix(_ip, 32);
   }

}
