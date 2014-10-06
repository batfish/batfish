package batfish.grammar.juniper.policy_options;

public class POPSTFr_AsPathStanza extends POPST_FromStanza {
   
   private String _pathName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTFr_AsPathStanza(String s) {
      _pathName = s;
      set_postProcessTitle("AS Path " + s);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_pathname() {
      return _pathName;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public POPST_FromType getType() {
      return POPST_FromType.AS_PATH;
   }

}
