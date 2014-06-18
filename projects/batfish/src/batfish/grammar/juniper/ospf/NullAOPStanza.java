package batfish.grammar.juniper.ospf;

public class NullAOPStanza extends AOPStanza {

	@Override
	public AOPType getType() {
		return AOPType.NULL;
	}

}
