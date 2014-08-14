package batfish.representation;

import java.io.Serializable;
import java.util.Set;

public class PolicyMapClause implements Serializable {

   private static final long serialVersionUID = 1L;

   private String _mapName;
   private Set<PolicyMapMatchLine> _matchList;
   private Set<PolicyMapSetLine> _setList;
   private PolicyMapAction _type;

   public PolicyMapClause(PolicyMapAction action, String name,
         Set<PolicyMapMatchLine> mlist, Set<PolicyMapSetLine> slist) {
      _type = action;
      _mapName = name;
      _matchList = mlist;
      _setList = slist;
   }

   public PolicyMapAction getAction() {
      return _type;
   }

   public String getMapName() {
      return _mapName;
   }

   public Set<PolicyMapMatchLine> getMatchLines() {
      return _matchList;
   }

   public Set<PolicyMapSetLine> getSetLines() {
      return _setList;
   }

}
