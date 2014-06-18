package batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;

public class StandardAccessList {
	private List<StandardAccessListLine> _lines;
	private String _id;

	public StandardAccessList(String id) {
		_id = id;
		_lines = new ArrayList<StandardAccessListLine>();
		//_lines.add(new StandardAccessListLine(LineAction.REJECT, "0.0.0.0", "255.255.255.255"));
	}

	public String getId() {
		return _id;
	}
	
	public void addLine(StandardAccessListLine all) {
		_lines.add(all);
	}
	
	public List<StandardAccessListLine> getLines() {
		return _lines;
	}
	
	public ExtendedAccessList toExtendedAccessList() {
	   ExtendedAccessList eal = new ExtendedAccessList(_id);
	   eal.getLines().clear();
	   for (StandardAccessListLine sall : _lines) {
	      eal.addLine(sall.toExtendedAccessListLine());
	   }
	   return eal;
	}
	
}
