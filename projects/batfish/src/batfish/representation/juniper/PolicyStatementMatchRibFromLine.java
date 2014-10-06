package batfish.representation.juniper;

public class PolicyStatementMatchRibFromLine extends PolicyStatement_MatchLine {

   private static final long serialVersionUID = 1L;
   

   private String _ribName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementMatchRibFromLine(String n) {
      _ribName = n;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_ribName() {
      return _ribName;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public PolicyStatement_MatchType getType() {
      return PolicyStatement_MatchType.RIB_FROM;
   }   
}
