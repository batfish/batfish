package batfish.grammar.juniper.policy_options;

public class RejectThenTPSStanza extends ThenTPSStanza {

	@Override
	public ThenType getType() {
		return ThenType.REJECT;
	}

}
