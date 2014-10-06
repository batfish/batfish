package batfish.representation.juniper;

public class PolicyStatementSetNextHopLine extends PolicyStatement_SetLine {

   private static final long serialVersionUID = 1L;
   
   private String _hopName;
   private PolicyStatement_HopType _hopType;

   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementSetNextHopLine(String hn, PolicyStatement_HopType ht) {
      _hopName = hn;
      _hopType = ht;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_hopName() {
      return _hopName;
   }
   public PolicyStatement_HopType get_hopType() {
      return _hopType;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public PolicyStatement_SetType getType() {
      return PolicyStatement_SetType.NEXT_HOP;
   }
   
}
