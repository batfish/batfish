package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;

public class NullAFStanza implements AFStanza {

   @Override
   public void process(BgpAddressFamily af) {
   }

}
