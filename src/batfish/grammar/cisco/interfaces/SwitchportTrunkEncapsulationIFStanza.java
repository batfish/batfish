package batfish.grammar.cisco.interfaces;

import batfish.representation.SwitchportEncapsulationType;
import batfish.representation.cisco.Interface;

public class SwitchportTrunkEncapsulationIFStanza implements IFStanza {

	private SwitchportEncapsulationType _encapsulation;

	public SwitchportTrunkEncapsulationIFStanza(SwitchportEncapsulationType e) {
		_encapsulation = e;
	}

   @Override
   public void process(Interface i) {
      i.setSwitchportTrunkEncapsulation(_encapsulation);
   }

}
