package batfish.grammar.cisco.interfaces;

import batfish.representation.SwitchportMode;
import batfish.representation.cisco.Interface;

public class SwitchportAccessIFStanza implements IFStanza {

	private int _vlan;
	
	public SwitchportAccessIFStanza(int vlan) {
		_vlan = vlan;
	}
	
   @Override
   public void process(Interface i) {
      i.setAccessVlan(_vlan);
      i.setSwitchportMode(SwitchportMode.ACCESS);
   }
}
