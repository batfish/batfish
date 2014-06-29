package batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.cisco.CiscoGrammar.Ip_prefix_list_stanzaContext;

public class PrefixList {
	private Ip_prefix_list_stanzaContext _context;
	
	//List of lines that stores the prefix
	private List<PrefixListLine> _lines;

   //Name of the filter
	private String _name;

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

	public Ip_prefix_list_stanzaContext getContext() {
      return _context;
   }

	public List<PrefixListLine> getLines() {
		return _lines;
	}

   public String getName() {
		return _name;
	}

   public void setContext(Ip_prefix_list_stanzaContext ctx) {
      _context = ctx;
   }
   
}
