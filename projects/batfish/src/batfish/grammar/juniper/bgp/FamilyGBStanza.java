package batfish.grammar.juniper.bgp;

public class FamilyGBStanza extends GBStanza {
	private boolean _IPv4;

	public FamilyGBStanza(boolean ip) {
		_IPv4 = ip;
	}

	public boolean IsIPv4Family() {
		return _IPv4;
	}

	@Override
	public GBType getType() {
		return GBType.FAMILY;
	}
}
