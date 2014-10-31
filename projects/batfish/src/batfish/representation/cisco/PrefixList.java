package batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import batfish.grammar.cisco.CiscoGrammar.Ip_prefix_list_stanzaContext;

public class PrefixList implements Serializable {

   private static final long serialVersionUID = 1L;

   private transient Ip_prefix_list_stanzaContext _context;

   // List of lines that stores the prefix
   private List<PrefixListLine> _lines;

   // Name of the filter
   private String _name;

   private boolean _isIpV6;

   private PrefixList(String n) {
      _name = n;
      _lines = new ArrayList<PrefixListLine>();
      _isIpV6 = false;
   }

   public PrefixList(String n, boolean isIpV6) {
      this(n);
      _isIpV6 = isIpV6;
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

   public boolean isIpV6() {
      return _isIpV6;
   }

   public void setContext(Ip_prefix_list_stanzaContext ctx) {
      _context = ctx;
   }

}
