package batfish.grammar.juniper.routing_options;

public class AutonomousSystemROStanza extends ROStanza {
	private int _asNum;

	public AutonomousSystemROStanza(int as) {
		_asNum = as;
	}

	public int getASNum() {
		return _asNum;
	}

	@Override
	public ROType getType() {
		return ROType.AS;
	}

}
