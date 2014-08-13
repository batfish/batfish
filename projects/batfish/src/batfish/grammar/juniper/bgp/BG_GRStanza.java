package batfish.grammar.juniper.bgp;

import batfish.grammar.juniper.StanzaWithStatus;

public abstract class BG_GRStanza extends StanzaWithStatus{
	public abstract BG_GRType getType();
}
