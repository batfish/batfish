package batfish.grammar.juniper.policy_options;

public class IPv6FromTPSStanza extends FromTPSStanza {

	@Override
	public FromType getType() {
		return FromType.IPV6;
	}

}
