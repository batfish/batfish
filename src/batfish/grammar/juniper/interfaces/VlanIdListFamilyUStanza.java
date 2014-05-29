package batfish.grammar.juniper.interfaces;

import java.util.ArrayList;

import batfish.util.SubRange;

public class VlanIdListFamilyUStanza extends FamilyUStanza {
	private ArrayList<SubRange> _vlanList;

	public VlanIdListFamilyUStanza() {
		_vlanList = new ArrayList<SubRange>();
	}

	public void addPair(int s, int e) {
		SubRange vlan = new SubRange(s, e);
		_vlanList.add(vlan);
	}
	
	public ArrayList<SubRange> getVlanList(){
		return _vlanList;
	}

	@Override
	public FamilyUType getType() {
		return FamilyUType.VLAN_ID_LIST;
	}

}
