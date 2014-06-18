package batfish.grammar.cisco.interfaces;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.cisco.Interface;
import batfish.util.SubRange;

public class SwitchportTrunkAllowedIFStanza implements IFStanza {

	private ArrayList<SubRange> _ranges;
	
	public SwitchportTrunkAllowedIFStanza(List<SubRange> r) {
		_ranges = new ArrayList<SubRange>();
		_ranges.addAll(r);
	}

	@Override
   public void process(Interface i) {
      i.addAllowedRanges(_ranges);
   }
	
}
