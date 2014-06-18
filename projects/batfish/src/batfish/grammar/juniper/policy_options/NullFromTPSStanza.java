package batfish.grammar.juniper.policy_options;

public class NullFromTPSStanza extends FromTPSStanza {

	@Override
	public FromType getType() {
		return FromType.NULL;
	}

}
