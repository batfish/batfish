package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

public class POPSTFr_AsPathStanza extends POPST_FromStanza {
   
   private List<String> _names;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTFr_AsPathStanza() {
      _names = new ArrayList<String>();
      set_postProcessTitle("AS Path ");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addName (String n) {
      set_postProcessTitle(get_postProcessTitle() + n + " ");
      _names.add(n);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<String> get_pathnames() {
      return _names;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public POPST_FromType getType() {
      return POPST_FromType.AS_PATH;
   }

}
