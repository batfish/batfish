package batfish.grammar.juniper.bgp;

import batfish.grammar.juniper.StanzaWithStatus;

public abstract class BGGR_NStanza extends StanzaWithStatus {
	public abstract BGGR_NType getType();
}
