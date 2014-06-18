package batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;

public class StandardCommunityList {

   private String _name;
   private List<StandardCommunityListLine> _lines;

   public StandardCommunityList(String name) {
      _name = name;
      _lines = new ArrayList<StandardCommunityListLine>();
   }

   public String getName() {
      return _name;
   }

   public List<StandardCommunityListLine> getLines() {
      return _lines;
   }

   public void addLine(StandardCommunityListLine line) {
      _lines.add(line);
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
         ExpandedCommunityListLine newLine = new ExpandedCommunityListLine(line.getAction(), regex);
         newList.addLine(newLine);
      }
      return newList;
   }
}
