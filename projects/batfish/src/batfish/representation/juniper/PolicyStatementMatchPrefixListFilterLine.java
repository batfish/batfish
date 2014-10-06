package batfish.representation.juniper;

public class PolicyStatementMatchPrefixListFilterLine extends PolicyStatement_MatchLine {

   private static final long serialVersionUID = 1L;
   

   private String _listName;
   private FilterMatch _filterMatch;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementMatchPrefixListFilterLine(String n, FilterMatch m) {
      _listName = n;
      _filterMatch = m;
      // TODO [P0]: apply filter match
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_listName() {
      return _listName;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public PolicyStatement_MatchType getType() {
      return PolicyStatement_MatchType.PREFIX_LIST_FILTER;
   }   
}
