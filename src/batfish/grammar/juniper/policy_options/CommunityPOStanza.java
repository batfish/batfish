package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.LineAction;
import batfish.representation.juniper.ExpandedCommunityListLine;

public class CommunityPOStanza extends POStanza {
   private String _name;
   private List<ExpandedCommunityListLine> _line;

   public CommunityPOStanza(String n) {
      _name = n; 
      _line = new ArrayList<ExpandedCommunityListLine>();
   }

   public void addMember(String m) {
      ExpandedCommunityListLine l = new ExpandedCommunityListLine(LineAction.ACCEPT, m);
      _line.add(l);
   }

   public String getName() {
      return _name;
   }

   public List<ExpandedCommunityListLine> getLines() {
      return _line;
   }

   @Override
   public POType getType() {
      return POType.COMMUNITY;
   }

}
