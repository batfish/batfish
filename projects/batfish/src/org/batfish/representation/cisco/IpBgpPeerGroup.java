package org.batfish.representation.cisco;

import org.batfish.common.datamodel.Ip;
import org.batfish.common.datamodel.Prefix;

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
