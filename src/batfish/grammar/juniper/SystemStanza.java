package batfish.grammar.juniper;

public class SystemStanza extends JStanza {
	private String _name;

	public String getHostName() {
		return _name;
	}

	public void processStanza(SysStanza ss) {
		switch (ss.getSysType()) {
		case HOST_NAME:
			HostNameSysStanza hss = (HostNameSysStanza) ss;
			_name = hss.getName();
			break;

		case NULL:
			break;

		default:
			System.out.println("bad system stanza type");
			break;
		}
	}

	@Override
	public JStanzaType getType() {
		return JStanzaType.SYSTEM;
	}

}
