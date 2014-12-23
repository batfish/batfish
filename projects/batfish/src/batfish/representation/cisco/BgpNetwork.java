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

   @Override
   public boolean equals(Object o) {
      if (o == null) {
         return false;
      }
      BgpNetwork rhs = (BgpNetwork) o;
      return _networkAddress.equals(rhs.getNetworkAddress())
            && _subnetMask.equals(rhs.getSubnetMask());
   }

   public Ip getNetworkAddress() {
      return _networkAddress;
   }

   public Ip getSubnetMask() {
      return _subnetMask;
   }

   @Override
   public int hashCode() {
      return _networkAddress.hashCode() | _subnetMask.hashCode();
   }

}
