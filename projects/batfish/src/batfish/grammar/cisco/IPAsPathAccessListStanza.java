package batfish.grammar.cisco;

import batfish.representation.LineAction;
import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.IpAsPathAccessListLine;

public class IPAsPathAccessListStanza implements Stanza {

   private IpAsPathAccessListLine _line;
   private String _name;

   public IPAsPathAccessListStanza(String name, LineAction action, String regex) {
      _line = new IpAsPathAccessListLine(action, regex);
      _name = name;
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
      c.addAsPathAccessListLine(_name, _line);
   }

}
