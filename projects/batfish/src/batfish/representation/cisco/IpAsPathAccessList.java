package batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.cisco.CiscoGrammar.Ip_as_path_access_list_stanzaContext;

public class IpAsPathAccessList {
   private Ip_as_path_access_list_stanzaContext _context;
   private List<IpAsPathAccessListLine> _lines;
   private String _name;

   public IpAsPathAccessList(String name) {
      _name = name;
      _lines = new ArrayList<IpAsPathAccessListLine>();
   }

   public void addLine(IpAsPathAccessListLine line) {
      _lines.add(line);
   }

   public Ip_as_path_access_list_stanzaContext getContext() {
      return _context;
   }

   public List<IpAsPathAccessListLine> getLines() {
      return _lines;
   }

   public String getName() {
      return _name;
   }

   public void setContext(Ip_as_path_access_list_stanzaContext ctx) {
      _context = ctx;
   }

}
