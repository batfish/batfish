package batfish.grammar.cisco;

import batfish.representation.LineAction;
import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.StandardAccessListLine;

public class StandardAccessListStanza implements Stanza {

   private StandardAccessListLine _accessListLine;
   private String _id;
   
   public StandardAccessListStanza(LineAction action, String id, String ip,
         String wildcard) {
      _id = id;
      _accessListLine = new StandardAccessListLine(action, ip, wildcard);
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
      c.addAccessListLine(_id, _accessListLine);
   }

}
