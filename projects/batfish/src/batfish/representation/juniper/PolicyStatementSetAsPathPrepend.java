package batfish.representation.juniper;

public class PolicyStatementSetAsPathPrepend extends PolicyStatement_SetLine {

   private static final long serialVersionUID = 1L;
   

   private int _asNum;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementSetAsPathPrepend(int a) {
      _asNum = a;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_asNum () {
      return _asNum;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public PolicyStatement_SetType getType() {
      return PolicyStatement_SetType.AS_PATH_PREPEND;
   }   
}
