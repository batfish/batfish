package batfish.grammar.juniper.bgp;

public class NullNGBStanza extends NGBStanza {

	@Override
	public NGBType getType() {
		return NGBType.NULL;
	}

}
