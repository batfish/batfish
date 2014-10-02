package batfish.grammar.juniper.firewall;

public class ThenTFFStanza {
	private ThenTFFType _type;

	public ThenTFFStanza(ThenTFFType t) {
		_type = t;
	}
	public ThenTFFType getType() {
		return _type;
	}
}
