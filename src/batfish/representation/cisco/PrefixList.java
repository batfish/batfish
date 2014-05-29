package batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * A data structure that represents a list of prefix with their prefix-length to
 * be matched
 * Used for route filter and prefix list in Juniper JunOS
 * Used for prefix list in Cisco IOS
 * 
 */

public class PrefixList {
	//Name of the filter
	private String _name;
	
	//List of lines that stores the prefix
	private List<PrefixListLine> _lines;

	public PrefixList(String n) {
		_name = n;
		_lines = new ArrayList<PrefixListLine>();
	}

	public void addLine(PrefixListLine r) {
		_lines.add(r);
	}

	public void addLines(List<PrefixListLine> r) {
		_lines.addAll(r);
	}

	public String getName() {
		return _name;
	}

	public List<PrefixListLine> getLines() {
		return _lines;
	}

}
