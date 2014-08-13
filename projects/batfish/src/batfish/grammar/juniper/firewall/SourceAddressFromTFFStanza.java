package batfish.grammar.juniper.firewall;

import java.util.ArrayList;
import java.util.List;

public class SourceAddressFromTFFStanza extends FromTFFStanza {
   private List<String> _address;
   private List<String> _exceptAddress;

   public SourceAddressFromTFFStanza() {
      _exceptAddress = new ArrayList<String>();
      _address = new ArrayList<String>();
   }
   
   public void addExceptAddress(String a){
      _exceptAddress.add(a);
   } 
   
   public void addAddress(String a){
      _address.add(a);
   }
   
   public List<String> get_address(){
      return _address;
   }
   
   public List<String> getExceptAddress(){
      return _exceptAddress;
   }

   @Override
   public FromTFFType getType() {
      return FromTFFType.SOURCE_ADDRESS;
   }

}
