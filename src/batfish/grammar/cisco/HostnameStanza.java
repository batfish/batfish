package batfish.grammar.cisco;

import batfish.representation.cisco.CiscoVendorConfiguration;

public class HostnameStanza implements Stanza {
   
   private String _hostname;
   
   public HostnameStanza(String hostname) {
      _hostname = hostname;
   }
   
   @Override
   public void process(CiscoVendorConfiguration c) {
      c.setHostname(_hostname);
   }
}
