package batfish.representation.juniper;

public class PolicyStatementSetInstallNextHopLine extends PolicyStatement_SetLine {

   private static final long serialVersionUID = 1L;
   

   private String _hopName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementSetInstallNextHopLine(String hn) {
      _hopName = hn;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_hopName() {
      return _hopName;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public PolicyStatement_SetType getType() {
      return PolicyStatement_SetType.INSTALL_NEXT_HOP;
   }   
}
