package batfish.grammar.cisco.interfaces;

import batfish.representation.cisco.Interface;

public class IPOSPFDeadIntervalIFStanza implements IFStanza {

	private int _seconds;
	private int _multiplier;

	public IPOSPFDeadIntervalIFStanza(int seconds, int multiplier) {
		_seconds = seconds;
		_multiplier = multiplier;
	}

   @Override
   public void process(Interface i) {
      i.setOSPFDeadInterval(_seconds);
      i.setOSPFHelloMultiplier(_multiplier);
   }

}
