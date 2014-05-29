package batfish.grammar.juniper.ospf;

import java.util.ArrayList;

public class FlatAreaOPStanza extends OPStanza {
   private AOPType _type1;
	private int _areaNum;
	private ArrayList<String> _interfaceList;

	public FlatAreaOPStanza() {
		_interfaceList = new ArrayList<String>();
	}

	public void setIDInt(int n) {
		_areaNum = n;
	}

	public void setIDIP(String ip) {
		_areaNum = 0;
	}

	public void processStanza(AOPStanza aops) {
	   _type1=aops.getType();
		switch (aops.getType()) {
		case INTERFACE:
			InterfaceAOPStanza is = (InterfaceAOPStanza) aops;
			_interfaceList.add(is.getInterfaceName());
			break;

		case NULL:
			break;

		default:
			System.out.println("bad area stanza type");
			break;
		}
	}

	public int getAreaNum() {
		return _areaNum;
	}

	public ArrayList<String> getInterfaceList() {
		return _interfaceList;
	}
	
	public AOPType getType1(){
	   return _type1;
	}

	@Override
	public OPType getType() {
		return OPType.AREA;
	}

}
