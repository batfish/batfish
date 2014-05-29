package batfish.representation.cisco;

public class OspfNetwork {

   private String _networkAddress;
   private String _subnetMask;
   private int _area;

   public OspfNetwork(String networkAddress, String subnetMask, int area) {
      _networkAddress = networkAddress;
      _subnetMask = subnetMask;
      _area = area;
   }
   
   public String getNetworkAddress() {
      return _networkAddress;
   }
   
   public String getSubnetMask() {
      return _subnetMask;
   }
   
   public int getArea() {
      return _area;
   }

}
