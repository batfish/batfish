package batfish.representation.juniper;

public class OSPFNetwork {

   private String _networkAddress;
   private String _subnetMask;
   private String _interface;
   private int _area;

   public OSPFNetwork(String networkAddress, String subnetMask, int area) {
      _networkAddress = networkAddress;
      _subnetMask = subnetMask;
      _area = area;
   }
   
   public OSPFNetwork(String inf, int area){
      _interface = inf;
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

   public String getInterface() {
      return _interface;
   }

}
