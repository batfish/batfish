package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

public class ProtocolFromTPSStanza extends FromTPSStanza {
	private List<String> _protocol;

	public ProtocolFromTPSStanza() {
		_protocol = new ArrayList<String>();
	}

	public void addProtocol(String p) {
		_protocol.add(p);
	}

	public List<String> getProtocol() {
		return _protocol;
	}

	@Override
	public FromType getType() {
		return FromType.PROTOCOL;
	}

}
