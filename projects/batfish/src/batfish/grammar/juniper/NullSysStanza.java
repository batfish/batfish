package batfish.grammar.juniper;

public class NullSysStanza extends SysStanza {

	@Override
	public SysStanzaType getSysType() {
		return SysStanzaType.NULL;
	}

}
