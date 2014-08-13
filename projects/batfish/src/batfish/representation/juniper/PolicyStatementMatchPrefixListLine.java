package batfish.representation.juniper;

public class PolicyStatementMatchPrefixListLine extends PolicyStatement_MatchLine {

   private String _listName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementMatchPrefixListLine(String n) {
      _listName = n;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_listName() {
      return _listName;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public PolicyStatement_MatchType getType() {
      return PolicyStatement_MatchType.PREFIX_LIST;
   }   
}
