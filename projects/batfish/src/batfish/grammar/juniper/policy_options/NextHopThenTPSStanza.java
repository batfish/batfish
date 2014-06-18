package batfish.grammar.juniper.policy_options;

public class NextHopThenTPSStanza extends ThenTPSStanza {
	private String _ip;

	public NextHopThenTPSStanza(String ip) {
		_ip = ip;
	}

	public String getNextHop() {
		return _ip;
	}

	@Override
	public ThenType getType() {
		return ThenType.NEXT_HOP;
	}

}
