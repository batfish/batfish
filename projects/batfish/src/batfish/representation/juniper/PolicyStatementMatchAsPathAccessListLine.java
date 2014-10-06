package batfish.representation.juniper;

public class PolicyStatementMatchAsPathAccessListLine extends PolicyStatement_MatchLine {

   private static final long serialVersionUID = 1L;
   
   private String _listName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementMatchAsPathAccessListLine(String listName) {
      _listName = listName;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_listName() {
      return _listName;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public PolicyStatement_MatchType getType() {
      return PolicyStatement_MatchType.AS_PATH;
   }


   
}
