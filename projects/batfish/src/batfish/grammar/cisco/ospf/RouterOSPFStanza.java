package batfish.grammar.cisco.ospf;

import batfish.grammar.cisco.Stanza;
import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.OspfProcess;

public class RouterOSPFStanza implements Stanza {

   private OspfProcess _process;

   public RouterOSPFStanza(int procnum) {
      _process = new OspfProcess(procnum);
   }

   public void processStanza(ROStanza ros) {
      if (ros == null) {
         return;
      }
      ros.process(_process);
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
      _process.computeNetworks(c.getInterfaces());
      c.addOspfProcess(_process);
   }
}
