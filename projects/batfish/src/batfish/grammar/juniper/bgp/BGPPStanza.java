package batfish.grammar.juniper.bgp;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.PStanza;
import batfish.grammar.juniper.PType;
import batfish.representation.juniper.BGPGroup;

public class BGPPStanza extends PStanza {
	private List<BGPGroup> _groupList;
	private List<String> _activatedNeighbor;

	public BGPPStanza() {
		_groupList = new ArrayList<BGPGroup>();
		_activatedNeighbor = new ArrayList<String>();
	}

	public void processStanza(BPStanza bps) {
		switch (bps.getType()) {
		case GROUP:
			GroupBPStanza gbps = (GroupBPStanza) bps;
			if (gbps.isIPV4()) {
				_groupList.add(gbps.getGroup());
				_activatedNeighbor.addAll(gbps.getActivatedNeighbor());
			}
			break;

		case NULL:
			break;

		default:
			System.out.println("bad bgp stanza type");
			break;
		}
	}

	public List<BGPGroup> getGroupList() {
		return _groupList;
	}

	public List<String> getActivatedNeighbor() {
		return _activatedNeighbor;
	}

	@Override
	public PType getType() {
		return PType.BGP;
	}

}
