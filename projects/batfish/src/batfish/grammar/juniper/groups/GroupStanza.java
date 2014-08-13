package batfish.grammar.juniper.groups;

import java.util.ArrayList;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.StanzaWithStatus;

public class GroupStanza extends StanzaWithStatus {
   
   private ArrayList<JStanza> _grStanzas;
   private String _name;
   
   /* ------------------------------ Constructor ----------------------------*/
   public GroupStanza (String n) {
      _grStanzas = new ArrayList<JStanza>();
      _name = n;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addSubstanza(JStanza g) {
      _grStanzas.add(g);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_name () {
      return _name;
   }
   /* --------------------------- Inherited Methods -------------------------*/
}
