package batfish.grammar.cisco.interfaces;

import batfish.grammar.cisco.Stanza;
import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.Interface;

public class InterfaceStanza implements Stanza {

   private Interface _interface; // intentionally package-visible

   public InterfaceStanza(String name) {
      _interface = new Interface(name);
      _interface.setBandwidth(getDefaultBandwidth(name));
   }

   private static Double getDefaultBandwidth(String name) {
      Double bandwidth = null;
      if (name.startsWith("FastEthernet")) {
         bandwidth = 100E6;
      }
      else if (name.startsWith("GigabitEthernet")) {
         bandwidth = 1E9;
      }
      else if (name.startsWith("TenGigabitEthernet")) {
         bandwidth = 10E9;
      }
      else if (name.startsWith("Vlan")) {
         bandwidth = null;
      }
      else if (name.startsWith("Loopback")) {
         bandwidth = 1E12; // dirty hack: just chose a very large number
      }
      if (bandwidth == null) {
         bandwidth = 1.0;
      }
      return bandwidth;
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
