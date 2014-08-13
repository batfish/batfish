package batfish.grammar.juniper.groups;

import java.util.ArrayList;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.JStanzaType;

public class ApplyGroupsStanza extends JStanza {
   
   boolean _withExcept;
   ArrayList<String> _groupNames;
   
   /* ------------------------------ Constructor ----------------------------*/
   public ApplyGroupsStanza (boolean we) {
      _withExcept = we;
      _groupNames = new ArrayList<String>();
   }   
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addGroupName (String g) {
      _groupNames.add(g);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public JStanzaType getType() {
      return JStanzaType.APPLY_GROUPS;
   }  
}
