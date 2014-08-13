package batfish.grammar.juniper.bgp;

import batfish.grammar.juniper.StanzaWithStatus;

public abstract class BGStanza extends StanzaWithStatus{
	public abstract BGType getType();

}
