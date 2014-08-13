package batfish.grammar.juniper.ospf;

import batfish.grammar.juniper.StanzaWithStatus;

public abstract class OP_ARStanza extends StanzaWithStatus {
	public abstract OP_ARType getType();
}
