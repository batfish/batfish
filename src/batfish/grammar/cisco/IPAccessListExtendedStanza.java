package batfish.grammar.cisco;

import java.util.ArrayList;

import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.ExtendedAccessListLine;

public class IPAccessListExtendedStanza implements Stanza {
   private String _listIdentifier;
   private ArrayList<ExtendedAccessListLine> _lines;

   public IPAccessListExtendedStanza(String id) {
      _listIdentifier = id;
      _lines = new ArrayList<ExtendedAccessListLine>();
   }

   public void addLine(ExtendedAccessListLine all) {
      _lines.add(all);
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
      for (ExtendedAccessListLine line : _lines) {
         c.addExtendedAccessListLine(_listIdentifier, line);
      }
   }

}
