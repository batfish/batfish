package batfish.grammar.cisco.ospf;

public class OSPFWildcardNetwork {
   private String _networkAddress;
   private String _wildcard;
   private int _area;

   public OSPFWildcardNetwork(String networkAddress, String wildcard, int area) {
      set_networkAddress(networkAddress);
      set_wildcard(wildcard);
      set_area(area);
   }

   public String getNetworkAddress() {
      return _networkAddress;
   }

   private void set_networkAddress(String _networkAddress) {
      this._networkAddress = _networkAddress;
   }

   public int getArea() {
      return _area;
   }

   private void set_area(int _area) {
      this._area = _area;
   }

   public String getWildcard() {
      return _wildcard;
   }

   private void set_wildcard(String _wildcard) {
      this._wildcard = _wildcard;
   }
}
