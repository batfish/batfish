package batfish.grammar.juniper.bgp;

public class LocalAddressGBStanza extends GBStanza {
   private String _localAddress;
   
   public LocalAddressGBStanza(String ip){
      _localAddress = ip;
   }
   
   public String getLocalAddress(){
      return _localAddress;
   }

   @Override
   public GBType getType() {      
      return GBType.LOCAL_ADDRESS;
   }

}
