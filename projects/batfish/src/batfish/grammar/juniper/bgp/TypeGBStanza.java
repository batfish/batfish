package batfish.grammar.juniper.bgp;

public class TypeGBStanza extends GBStanza {
	private boolean _isExternal;

	public TypeGBStanza(boolean e) {
		_isExternal = e;
	}

	public boolean isExternal() {
		return _isExternal;
	}

	@Override
	public GBType getType() {
		return GBType.TYPE;
	}

}
