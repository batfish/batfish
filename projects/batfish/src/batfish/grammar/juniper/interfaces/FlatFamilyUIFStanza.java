package batfish.grammar.juniper.interfaces;

import java.util.ArrayList;

import batfish.util.SubRange;
import batfish.representation.SwitchportMode;

public class FlatFamilyUIFStanza {
   private FamilyUType _type1;
	private FamilyType _type;
	private String _address;
	private String _subnetMask;
	private SwitchportMode _interfaceMode;
	private int _accessVlan;
	private ArrayList<SubRange> _vlanID;

	public FlatFamilyUIFStanza(FamilyType t) {
		_type = t;
	}

	public void processFamilyUStanza(FamilyUStanza fus) {
	   _type1=fus.getType();
		switch (fus.getType()) {
		case ADDRESS:
			AddressFamilyUStanza afus = (AddressFamilyUStanza) fus;
			_address = afus.getAddress();
            _subnetMask = afus.getSubnetMask();
			break;

		case INTERFACE_MODE:
			InterfaceModeFamilyUStanza imfus = (InterfaceModeFamilyUStanza) fus;
			_interfaceMode = imfus.getMode();
			break;

		case NULL:
			break;

		case VLAN_ID:
			VlanIdFamilyUStanza vfus = (VlanIdFamilyUStanza) fus;
			_accessVlan = vfus.getVlanID();
			break;

		case VLAN_ID_LIST:
			VlanIdListFamilyUStanza vlfus = (VlanIdListFamilyUStanza) fus;
			_vlanID = vlfus.getVlanList();
			break;

		case NATIVE_VLAN:
         throw new Error("to implement");
      case FILTER:

		default:
			System.out.println("bad family stanza type");
			break;
		}

	}

	public FamilyType getType() {
		return _type;
	}

	public String getAddress() {
		return _address;
	}
	
	public String getSubnetMask(){
		return _subnetMask;
	}

	public SwitchportMode getInterfaceMode() {
		return _interfaceMode;
	}

	public int getAccessVlan() {
		return _accessVlan;
	}

	public ArrayList<SubRange> getVlanIDList() {
		return _vlanID;
	}
	
	public FamilyUType getType1(){
	   return _type1;
	}
}
