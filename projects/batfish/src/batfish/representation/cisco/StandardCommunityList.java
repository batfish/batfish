package batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import batfish.grammar.cisco.CiscoGrammar.Ip_community_list_standard_stanzaContext;

public class StandardCommunityList implements Serializable {

   private static final long serialVersionUID = 1L;

   private transient Ip_community_list_standard_stanzaContext _context;
   private List<StandardCommunityListLine> _lines;
   private String _name;

   public StandardCommunityList(String name) {
      _name = name;
      _lines = new ArrayList<StandardCommunityListLine>();
   }

   public void addLine(StandardCommunityListLine line) {
      _lines.add(line);
   }

   public Ip_community_list_standard_stanzaContext getContext() {
      return _context;
   }

   public List<StandardCommunityListLine> getLines() {
      return _lines;
   }

   public String getName() {
      return _name;
   }

   public void setContext(Ip_community_list_standard_stanzaContext ctx) {
      _context = ctx;
   }

   public ExpandedCommunityList toExpandedCommunityList() {
      ExpandedCommunityList newList = new ExpandedCommunityList(_name);
      for (StandardCommunityListLine line : _lines) {
         List<Long> standardCommunities = line.getCommunities();
         String regex = "(";
         for (Long l : standardCommunities) {
            regex += batfish.util.Util.longToCommunity(l) + "|";
         }
         regex = regex.substring(0, regex.length() - 1) + ")";
         ExpandedCommunityListLine newLine = new ExpandedCommunityListLine(
               line.getAction(), regex);
         newList.addLine(newLine);
      }
      return newList;
   }

}
