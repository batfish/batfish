package batfish.grammar.cisco;

import java.util.List;

import batfish.representation.LineAction;
import batfish.representation.cisco.ExtendedAccessListLine;
import batfish.util.SubRange;

public class ItemIPAccessListExtendedStanza {
//	private ExtendedAccessListLine _accessListLine;

	public ItemIPAccessListExtendedStanza(LineAction action,
			int protocol, String srcIp, String srcWildcard, String dstIp,
			String dstWildcard, List<SubRange> srcPortRanges, List<SubRange> dstPortRanges) {
//		if (action != null) {
//			_accessListLine = new ExtendedAccessListLine(action, protocol, srcIp,
//					srcWildcard, dstIp, dstWildcard, srcPortRanges, dstPortRanges);
//		} else {
//			_accessListLine = null;
//		}
	}

	public ExtendedAccessListLine getAccessListLine() {
//		return _accessListLine;
	   return null;
	}

}
