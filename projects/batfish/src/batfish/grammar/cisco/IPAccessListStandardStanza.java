package batfish.grammar.cisco;

import java.util.ArrayList;

import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.StandardAccessListLine;

public class IPAccessListStandardStanza implements Stanza {
   private String _listIdentifier;
   private ArrayList<StandardAccessListLine> _lines;

   public IPAccessListStandardStanza(String id) {
      _listIdentifier = id;
      _lines = new ArrayList<StandardAccessListLine>();
   }

   public void addLine(StandardAccessListLine all) {
      _lines.add(all);
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
      for (StandardAccessListLine line : _lines) {
         c.addAccessListLine(_listIdentifier, line);
      }
   }

}
