package batfish.representation.juniper;

public class PolicyStatementSetLocalPreferenceLine extends PolicyStatement_SetLine {

   private static final long serialVersionUID = 1L;
   
   private int _localPreference;
      
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementSetLocalPreferenceLine(int localPreference) {
      _localPreference = localPreference;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_LocalPreference() {
      return _localPreference;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
    @Override
   public PolicyStatement_SetType getType() {
      return PolicyStatement_SetType.LOCAL_PREF;
   }   
}
