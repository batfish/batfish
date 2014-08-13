package batfish.grammar.juniper;

import java.util.HashMap;
import batfish.representation.juniper.JuniperVendorConfiguration;

public class FlatJuniperConfiguration {
   private JuniperVendorConfiguration _configuration;
   private String _routerID;

   // second

   public FlatJuniperConfiguration() {
      _configuration = new JuniperVendorConfiguration();
   }

   public String getRouterID() {
      return _routerID;
   }

   public void processStanza(JStanza js) {
      

   }

   public JuniperVendorConfiguration getConfiguration() {
      return _configuration;
   }

}
