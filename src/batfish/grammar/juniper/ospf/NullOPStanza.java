package batfish.grammar.juniper.ospf;

public class NullOPStanza extends OPStanza {

	@Override
	public OPType getType() {
		return OPType.NULL;
	}

}
