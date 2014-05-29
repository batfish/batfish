package batfish.grammar.juniper.policy_options;

public class AcceptThenTPSStanza extends ThenTPSStanza {

	@Override
	public ThenType getType() {
		return ThenType.ACCEPT;
	}

}
