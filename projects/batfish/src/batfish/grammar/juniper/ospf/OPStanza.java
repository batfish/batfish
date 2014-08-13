package batfish.grammar.juniper.ospf;

import batfish.grammar.juniper.StanzaWithStatus;

public abstract class OPStanza extends StanzaWithStatus {
	public abstract OPType getType();
}
