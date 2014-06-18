package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;

public interface AFStanza {
	void process(BgpAddressFamily af);
}
