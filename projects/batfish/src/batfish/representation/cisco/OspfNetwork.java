package batfish.representation.cisco;

import batfish.representation.Ip;

public class OspfNetwork {

   private Ip _networkAddress;
   private Ip _subnetMask;
   private int _area;

   public OspfNetwork(Ip networkAddress, Ip subnetMask, int area) {
      _networkAddress = networkAddress;
      _subnetMask = subnetMask;
      _area = area;
   }
   
   public Ip getNetworkAddress() {
      return _networkAddress;
   }
   
   public Ip getSubnetMask() {
      return _subnetMask;
   }
   
   public int getArea() {
      return _area;
   }

}
