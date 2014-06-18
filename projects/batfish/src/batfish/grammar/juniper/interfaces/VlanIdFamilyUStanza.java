package batfish.grammar.juniper.interfaces;

public class VlanIdFamilyUStanza extends FamilyUStanza {
	private int _vlan;

	public VlanIdFamilyUStanza(int num) {
		_vlan = num;
	}

	public int getVlanID() {
		return _vlan;
	}

	@Override
	public FamilyUType getType() {
		return FamilyUType.VLAN_ID;
	}

}
