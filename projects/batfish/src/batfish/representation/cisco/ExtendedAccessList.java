package batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.cisco.CiscoGrammar.Extended_access_list_stanzaContext;

public class ExtendedAccessList {
   private Extended_access_list_stanzaContext _context;
   private String _id;
   private List<ExtendedAccessListLine> _lines;

   public ExtendedAccessList(String id) {
      _id = id;
      _lines = new ArrayList<ExtendedAccessListLine>();
      // _lines.add(new ExtendedAccessListLine(LineAction.REJECT, 0,
      // "0.0.0.0", "255.255.255.255", "0.0.0.0", "255.255.255.255", null));
   }

   public void addLine(ExtendedAccessListLine all) {
      _lines.add(all);
   }

   public Extended_access_list_stanzaContext getContext() {
      return _context;
   }

   public String getId() {
      return _id;
   }

   public List<ExtendedAccessListLine> getLines() {
      return _lines;
   }

   public void setContext(Extended_access_list_stanzaContext ctx) {
      _context = ctx;
   }
   
   @Override
   public String toString() {
      String output = super.toString() + "\n" + "Identifier: " + _id;
      for (ExtendedAccessListLine line : _lines) {
         output += "\n" + line;
      }
      return output;
   }
}
