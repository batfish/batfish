package batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import batfish.grammar.cisco.CiscoGrammar.Standard_access_list_stanzaContext;

public class StandardAccessList implements Serializable {

   private static final long serialVersionUID = 1L;

   private transient Standard_access_list_stanzaContext _context;
   private String _id;
   private List<StandardAccessListLine> _lines;

   public StandardAccessList(String id) {
      _id = id;
      _lines = new ArrayList<StandardAccessListLine>();
      // _lines.add(new StandardAccessListLine(LineAction.REJECT, "0.0.0.0",
      // "255.255.255.255"));
   }

   public void addLine(StandardAccessListLine all) {
      _lines.add(all);
   }

   public Standard_access_list_stanzaContext getContext() {
      return _context;
   }

   public String getId() {
      return _id;
   }

   public List<StandardAccessListLine> getLines() {
      return _lines;
   }

   public void setContext(Standard_access_list_stanzaContext ctx) {
      _context = ctx;
   }

   public ExtendedAccessList toExtendedAccessList() {
      ExtendedAccessList eal = new ExtendedAccessList(_id);
      eal.getLines().clear();
      for (StandardAccessListLine sall : _lines) {
         eal.addLine(sall.toExtendedAccessListLine());
      }
      return eal;
   }

}
