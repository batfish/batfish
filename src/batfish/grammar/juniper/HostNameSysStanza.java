package batfish.grammar.juniper;

public class HostNameSysStanza extends SysStanza {
	private String _name;

	public HostNameSysStanza(String name) {
		_name = name;
	}

	public String getName() {
		return _name;
	}

	@Override
	public SysStanzaType getSysType() {
		return SysStanzaType.HOST_NAME;
	}

}
