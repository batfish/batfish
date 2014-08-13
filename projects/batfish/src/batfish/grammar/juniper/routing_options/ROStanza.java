package batfish.grammar.juniper.routing_options;

import batfish.grammar.juniper.StanzaWithStatus;

public abstract class ROStanza extends StanzaWithStatus{
	public abstract ROType getType();
}
