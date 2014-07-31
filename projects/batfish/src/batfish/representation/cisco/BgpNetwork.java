package batfish.representation.cisco;

import java.io.Serializable;

import batfish.representation.Ip;

public class BgpNetwork implements Serializable {

   private static final long serialVersionUID = 1L;
   private Ip _networkAddress;
   private Ip _subnetMask;

   public BgpNetwork(Ip network, Ip subnet) {
      _networkAddress = network;
      _subnetMask = subnet;
   }

   public Ip getNetworkAddress() {
      return _networkAddress;
   }

   public Ip getSubnetMask() {
      return _subnetMask;
   }

}
