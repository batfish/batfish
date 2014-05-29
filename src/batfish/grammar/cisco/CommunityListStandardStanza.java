package batfish.grammar.cisco;

import java.util.List;

import batfish.representation.LineAction;
import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.StandardCommunityListLine;

public class CommunityListStandardStanza implements Stanza {

   private LineAction _action;
   private List<Long> _communities;
   private String _name;

   public CommunityListStandardStanza(String name, LineAction action,
         List<Long> communities) {
      _name = name;
      _action = action;
      _communities = communities;
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
      StandardCommunityListLine line = new StandardCommunityListLine(_action,
            _communities);
      c.addStandardCommunityListLine(_name, line);
   }

}
