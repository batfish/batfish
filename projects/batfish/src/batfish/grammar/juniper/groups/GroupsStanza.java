package batfish.grammar.juniper.groups;

import java.util.HashMap;
import java.util.Map;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.JStanzaType;

public class GroupsStanza extends JStanza {
   
   private Map<String,GroupStanza> _groups;
   
   /* ------------------------------ Constructor ----------------------------*/
   public GroupsStanza () {
      _groups = new HashMap<String, GroupStanza>();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addGroup (GroupStanza g) {
      _groups.put(g.get_name(),g);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/   
   @Override
   public JStanzaType getType() {
      return JStanzaType.GROUPS;
   }  
}
