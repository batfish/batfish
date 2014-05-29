package batfish.grammar.juniper.bgp;

public class NullGBStanza extends GBStanza {

	@Override
	public GBType getType() {
		return GBType.NULL;
	}

}
