package batfish.grammar.cisco.ospf;

import batfish.grammar.cisco.Stanza;
import batfish.representation.cisco.CiscoVendorConfiguration;

public class IPv6RouterOSPFStanza implements Stanza {

   public IPv6RouterOSPFStanza(int procnum) {
      // TODO Auto-generated constructor stub
   }

   public void processStanza(IPv6ROStanza ros) {
      if (ros == null) {
         return;
      }
      switch (ros.getType()) {
         case NULL:
            break;

         default:
            System.out.println("bad ROSType");
            break;
      }
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
      // TODO: implement
   }

}
