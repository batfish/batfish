package batfish.grammar.juniper.interfaces;

import batfish.grammar.juniper.StanzaWithStatus;

public abstract class IFStanza extends StanzaWithStatus{
	public abstract IFType getType();

}
