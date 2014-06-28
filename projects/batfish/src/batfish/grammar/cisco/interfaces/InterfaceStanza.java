package batfish.grammar.cisco.interfaces;

import batfish.grammar.cisco.Stanza;
import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.Interface;

public class InterfaceStanza implements Stanza {

   private Interface _interface; // intentionally package-visible

   public InterfaceStanza(String name) {
      //_interface = new Interface(name);
//      _interface.setBandwidth(getDefaultBandwidth(name));
   }

   public void processStanza(IFStanza ifs) {
      if (ifs != null) {
         ifs.process(_interface);
      }
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
      c.addInterface(_interface);
   }

}
