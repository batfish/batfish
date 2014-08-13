package batfish.grammar.juniper.policy_options;

import batfish.grammar.juniper.StanzaWithStatus;

public abstract class POStanza extends StanzaWithStatus {	
	public abstract POType getType();
}
