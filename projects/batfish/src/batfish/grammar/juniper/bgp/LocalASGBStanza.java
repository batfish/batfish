package batfish.grammar.juniper.bgp;

public class LocalASGBStanza extends GBStanza {
	private int _localASNum;

	public LocalASGBStanza(int a) {
		_localASNum = a;
	}

	public int getLocalASNum() {
		return _localASNum;
	}

	@Override
	public GBType getType() {
		return GBType.LOCAL_AS;
	}

}
