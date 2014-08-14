package batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import batfish.grammar.cisco.CiscoGrammar.Ip_community_list_expanded_stanzaContext;

public class ExpandedCommunityList implements Serializable {

   private static final long serialVersionUID = 1L;

   private transient Ip_community_list_expanded_stanzaContext _context;
   private List<ExpandedCommunityListLine> _lines;
   private String _name;

   public ExpandedCommunityList(String name) {
      _name = name;
      _lines = new ArrayList<ExpandedCommunityListLine>();
   }

   public void addLine(ExpandedCommunityListLine line) {
      _lines.add(line);
   }

   public Ip_community_list_expanded_stanzaContext getContext() {
      return _context;
   }

   public List<ExpandedCommunityListLine> getLines() {
      return _lines;
   }

   public String getName() {
      return _name;
   }

   public void setContext(Ip_community_list_expanded_stanzaContext ctx) {
      _context = ctx;
   }

}
