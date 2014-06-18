package batfish.grammar.cisco.interfaces;

import batfish.representation.cisco.Interface;

public class ShutdownIFStanza implements IFStanza {

   @Override
   public void process(Interface i) {
      i.setActive(false);
   }
	
}
