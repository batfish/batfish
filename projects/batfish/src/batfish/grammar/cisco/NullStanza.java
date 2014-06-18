package batfish.grammar.cisco;

import batfish.representation.cisco.CiscoVendorConfiguration;

public class NullStanza implements Stanza {

   @Override
   public void process(CiscoVendorConfiguration c) {
   }

}
