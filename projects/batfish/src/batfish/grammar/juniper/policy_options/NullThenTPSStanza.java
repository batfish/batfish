package batfish.grammar.juniper.policy_options;

public class NullThenTPSStanza extends ThenTPSStanza {

	@Override
	public ThenType getType() {
		return ThenType.NULL;
	}

}
