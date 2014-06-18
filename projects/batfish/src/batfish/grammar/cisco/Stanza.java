package batfish.grammar.cisco;

import batfish.representation.cisco.CiscoVendorConfiguration;

public interface Stanza {
   void process(CiscoVendorConfiguration c);
}
