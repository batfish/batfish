package batfish.grammar.juniper.policy_options;

public class NeighborFromTPSStanza extends FromTPSStanza {
	private String _ip;

	public NeighborFromTPSStanza(String ip) {
		_ip = ip;
	}

	public String getNeighborIP() {
		return _ip;
	}

	@Override
	public FromType getType() {
		return FromType.NEIGHBOR;
	}

}
