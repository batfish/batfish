package batfish.grammar.juniper.policy_options;

import batfish.representation.juniper.ASPathAccessList;

public class PO_AsPathStanza extends POStanza {
   
   private String _name;
   private String _regex;
   private ASPathAccessList _asPathAccessList;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PO_AsPathStanza(String n, String r) {
      _name = n; 
      _regex = r;
      set_postProcessTitle("AS Path " + _name + _regex);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public ASPathAccessList get_ASPathAccessList() {
      return _asPathAccessList;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public void postProcessStanza() {
      _asPathAccessList = new ASPathAccessList(_name, _regex);
      super.postProcessStanza();
   }
   
   @Override
   public POType getType() {
      return POType.AS_PATH;
   }

}
