package batfish.grammar.juniper.protocols;

import batfish.grammar.juniper.StanzaWithStatus;

public abstract class PStanza extends StanzaWithStatus {
	public abstract PType getType();
}
