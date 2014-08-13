package batfish.grammar.juniper.system;

import batfish.grammar.juniper.StanzaWithStatus;

public abstract class SysStanza extends StanzaWithStatus {
	public abstract SysStanzaType getSysType();
}
