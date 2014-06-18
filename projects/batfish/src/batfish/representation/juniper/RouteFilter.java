package batfish.representation.juniper;

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

public class RouteFilter {
	//Name of the filter
	private String _name;
	
	//List of lines that stores the prefix
	private List<RouteFilterLine> _lines;

	public RouteFilter(String n) {
		_name = n;
		_lines = new ArrayList<RouteFilterLine>();
	}

	public void addLine(RouteFilterLine r) {
		_lines.add(r);
	}

	public void addLines(List<RouteFilterLine> r) {
		_lines.addAll(r);
	}

	public String getName() {
		return _name;
	}

	public List<RouteFilterLine> getLines() {
		return _lines;
	}

}
