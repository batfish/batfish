package batfish.grammar.cisco.bgp;

import batfish.grammar.cisco.Stanza;
import batfish.representation.cisco.BgpProcess;
import batfish.representation.cisco.CiscoVendorConfiguration;

public class RouterBGPStanza implements Stanza {

   private BgpProcess _process;

   public RouterBGPStanza(int procnum) {
      _process = new BgpProcess(procnum);
   }

   public void processStanza(RBStanza rbs) {
      if (rbs == null) {
         return;
      }
      rbs.process(_process);
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
      c.setBgpProcess(_process);
   }
}
