package batfish.grammar.juniper.firewall;

import java.util.ArrayList;
import java.util.List;

public class DestinationAddressFromTFFStanza extends FromTFFStanza {
   private List<String> _address;

   public DestinationAddressFromTFFStanza() {
      _address = new ArrayList<String>();
   }
   
   public void addAddress(String a){
      _address.add(a);
   }
   
   public List<String> get_address(){
      return _address;
   }

   @Override
   public FromTFFType getType() {
      return FromTFFType.DESTINATION_ADDRESS;
   }

}
