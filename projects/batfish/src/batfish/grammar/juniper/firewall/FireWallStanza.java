package batfish.grammar.juniper.firewall;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.JStanzaType;
import batfish.representation.juniper.ExtendedAccessList;

public class FireWallStanza extends JStanza {
   private List<ExtendedAccessList> _filters;

   public FireWallStanza() {
      _filters = new ArrayList<ExtendedAccessList>();
   }

   public void processStanza(FilterFStanza fs) {
      _filters.add(fs.getFilter());
   }

   public List<ExtendedAccessList> getFilters() {
      return _filters;
   }

   @Override
   public JStanzaType getType() {
      return JStanzaType.FIREWALL;
   }

}
