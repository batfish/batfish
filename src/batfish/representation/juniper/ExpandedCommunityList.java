package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;

public class ExpandedCommunityList {

   private String _name;
   private List<ExpandedCommunityListLine> _lines;

   public ExpandedCommunityList(String name) {
      _name = name;
      _lines = new ArrayList<ExpandedCommunityListLine>();
   }

   public String getName() {
      return _name;
   }

   public void addLine(ExpandedCommunityListLine line) {
      _lines.add(line);
   }
   
   public List<ExpandedCommunityListLine> getLines(){
      return _lines;
   }

}
