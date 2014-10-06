package batfish.grammar.juniper.routing_options;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RO_RibGroupsStanza extends ROStanza { 
   
   private Map<String, List<String>> _groupsImports;

   /* ------------------------------ Constructor ----------------------------*/
   public RO_RibGroupsStanza() {
      _groupsImports = new HashMap<String, List<String>> ();
      set_postProcessTitle("RIB Groups");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void AddGroup(String n, List<String> l) {
      if (!_groupsImports.containsKey(n)) {
         _groupsImports.put(n,l);
      }
      else {
         _groupsImports.get(n).addAll(l);
      } 
   }

   /* ---------------------------- Getters/Setters --------------------------*/
   public Map<String, List<String>> get_groupsImports () {
      return _groupsImports;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public ROType getType() {
		return ROType.RIB_GROUPS;
	}

}
