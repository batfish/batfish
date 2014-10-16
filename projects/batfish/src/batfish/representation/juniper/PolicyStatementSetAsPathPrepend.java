package batfish.representation.juniper;

public class PolicyStatementSetAsPathPrepend extends PolicyStatement_SetLine {

   private static final long serialVersionUID = 1L;
   

   private String _asPrepend;
   // TODO [Ask Ari]: these are not always integers, sometimes they are multiple ASNums delimited by spaces
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementSetAsPathPrepend(String a) {
      _asPrepend = a;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_asPrepend () {
      return _asPrepend;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public PolicyStatement_SetType getType() {
      return PolicyStatement_SetType.AS_PATH_PREPEND;
   }   
}
