package batfish.grammar.juniper.bgp;

public class NullBPStanza extends BPStanza {

	@Override
	public BPType getType() {
		return BPType.NULL;
	}

}
