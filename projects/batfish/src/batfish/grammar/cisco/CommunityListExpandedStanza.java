package batfish.grammar.cisco;

import batfish.representation.LineAction;
import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.ExpandedCommunityListLine;

public class CommunityListExpandedStanza implements Stanza {

   private LineAction _action;
   private String _name;
   private String _regex;

   public CommunityListExpandedStanza(String name, LineAction ala, String regex) {
      _name = name;
      _action = ala;
      _regex = regex;
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
      ExpandedCommunityListLine line = new ExpandedCommunityListLine(_action,
            _regex);
      c.addExpandedCommunityListLine(_name, line);
   }

}
