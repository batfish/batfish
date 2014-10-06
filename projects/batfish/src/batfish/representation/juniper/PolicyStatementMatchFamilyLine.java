package batfish.representation.juniper;

public class PolicyStatementMatchFamilyLine extends PolicyStatement_MatchLine {

   private static final long serialVersionUID = 1L;
   
   private FamilyType _famType;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementMatchFamilyLine(FamilyType ft) {
      _famType = ft;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public FamilyType get_famType() {
      return _famType;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public PolicyStatement_MatchType getType() {
      return PolicyStatement_MatchType.FAMILY;
   }   
}
