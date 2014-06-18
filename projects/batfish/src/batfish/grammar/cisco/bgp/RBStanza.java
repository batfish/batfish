package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpProcess;

public interface RBStanza {
	void process(BgpProcess p);
}
