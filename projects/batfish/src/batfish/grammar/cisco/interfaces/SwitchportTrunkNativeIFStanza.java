package batfish.grammar.cisco.interfaces;

import batfish.representation.cisco.Interface;

public class SwitchportTrunkNativeIFStanza implements IFStanza {

	private int _vlan;
	
	public SwitchportTrunkNativeIFStanza(int vlan) {
		_vlan = vlan;
	}

   @Override
   public void process(Interface i) {
      i.setNativeVlan(_vlan);
   }
	
}
