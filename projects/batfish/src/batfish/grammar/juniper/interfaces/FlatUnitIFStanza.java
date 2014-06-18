package batfish.grammar.juniper.interfaces;

import java.util.ArrayList;

import batfish.representation.SwitchportMode;
import batfish.util.SubRange;

public class FlatUnitIFStanza extends IFStanza {
   private FamilyType _type1;
   private FamilyUType _type2;
	private int _num;
	private FamilyType _type;
	private String _address;
	private String _subnetMask;
	private SwitchportMode _interfaceMode;
	private int _accessVlan;
	private ArrayList<SubRange> _vlanID;

	public FlatUnitIFStanza(int num) {
		_num = num;
	}

	public void processFamily(FlatFamilyUIFStanza fuifs) {
	   _type1=fuifs.getType();
	   _type2=fuifs.getType1();
		switch (fuifs.getType()) {
		case BRIDGE:
			_type = fuifs.getType();
			_interfaceMode = fuifs.getInterfaceMode();
			_accessVlan = fuifs.getAccessVlan();
			_vlanID = fuifs.getVlanIDList();
			break;

		case INET:
			_type = fuifs.getType();
			String tmpAdd = fuifs.getAddress();
			if (tmpAdd != null) {
				_address = tmpAdd;
				_subnetMask = convertSubnet(fuifs.getSubnetMask());
			} else {
				_address = "";
				_subnetMask = null;
			}
			break;

		case INET6:
			break;

		default:
			System.out.println("bad family type");
			break;
		}
	}

	private String convertSubnet(String s) {
		String result = "";
		int sval = Integer.parseInt(s);
		if ((sval >= 0) && (sval <= 8)) {

			if (sval == 0) {
				result += "0";
			} else if (sval == 1) {
				result += "128";
			} else if (sval == 2) {
				result += "192";
			} else if (sval == 3) {
				result += "224";
			} else if (sval == 4) {
				result += "240";
			} else if (sval == 5) {
				result += "248";
			} else if (sval == 6) {
				result += "252";
			} else if (sval == 7) {
				result += "254";
			} else if (sval == 8) {
				result += "255";
			}
			result += ".0.0.0";

		} else if ((sval >= 9) && (sval <= 16)) {

			result += "255.";
			if (sval == 9) {
				result += "128";
			} else if (sval == 10) {
				result += "192";
			} else if (sval == 11) {
				result += "224";
			} else if (sval == 12) {
				result += "240";
			} else if (sval == 13) {
				result += "248";
			} else if (sval == 14) {
				result += "252";
			} else if (sval == 15) {
				result += "254";
			} else if (sval == 16) {
				result += "255";
			}
			result += ".0.0";

		} else if ((sval >= 17) && (sval <= 24)) {

			result += "255.255.";
			if (sval == 17) {
				result += "128";
			} else if (sval == 18) {
				result += "192";
			} else if (sval == 19) {
				result += "224";
			} else if (sval == 20) {
				result += "240";
			} else if (sval == 21) {
				result += "248";
			} else if (sval == 22) {
				result += "252";
			} else if (sval == 23) {
				result += "254";
			} else if (sval == 24) {
				result += "255";
			}
			result += ".0";

		} else if ((sval >= 25) && (sval <= 32)) {
			result += "255.255.255.";
			if (sval == 25) {
				result += "128";
			} else if (sval == 26) {
				result += "192";
			} else if (sval == 27) {
				result += "224";
			} else if (sval == 28) {
				result += "240";
			} else if (sval == 29) {
				result += "248";
			} else if (sval == 30) {
				result += "252";
			} else if (sval == 31) {
				result += "254";
			} else if (sval == 32) {
				result += "255";
			}

		} else {
			System.out.println("bad subnet value");
		}

		return result;
	}

	public int getUnitNum() {
		return _num;
	}

	public FamilyType getFamilyType() {
		return _type;
	}

	public String getAddress() {
		return _address;
	}

	public String getSubnetMask() {
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
	
	public FamilyType getType1(){
	   return _type1;
	}
	
	public FamilyUType getType2(){
	   return _type2;
	}

	@Override
	public IFType getType() {
		return IFType.UNIT;
	}

}
