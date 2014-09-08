package batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import batfish.grammar.cisco.CiscoGrammar.Extended_access_list_stanzaContext;

public class ExtendedAccessList implements Serializable {

   private static final long serialVersionUID = 1L;

   private transient Extended_access_list_stanzaContext _context;
   private String _id;
   private List<ExtendedAccessListLine> _lines;

   private boolean _isIpV6;
   
   public ExtendedAccessList(String id) {
      _id = id;
      _lines = new ArrayList<ExtendedAccessListLine>();
      _isIpV6 = false;
      // _lines.add(new ExtendedAccessListLine(LineAction.REJECT, 0,
      // "0.0.0.0", "255.255.255.255", "0.0.0.0", "255.255.255.255", null));
   }
   
   public ExtendedAccessList(String id, boolean isIpV6 )  {
      this(id);
      _isIpV6 = isIpV6;
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

   public boolean IsIpV6() {
      return _isIpV6;
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
