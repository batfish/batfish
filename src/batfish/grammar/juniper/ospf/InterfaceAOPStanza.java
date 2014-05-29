package batfish.grammar.juniper.ospf;

public class InterfaceAOPStanza extends AOPStanza {
	private String _ifName;

	public InterfaceAOPStanza(String n) {
		_ifName = n;
	}

	public String getInterfaceName() {
		return _ifName;
	}

	@Override
	public AOPType getType() {
		return AOPType.INTERFACE;
	}

}
