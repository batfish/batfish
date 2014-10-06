package batfish.representation.juniper;

public class PolicyStatementMatchSourceAddressFilterLine extends PolicyStatement_MatchLine {

   private static final long serialVersionUID = 1L;
   

   private String _listName;
   private int _length;
   private FilterMatch _filterMatch;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementMatchSourceAddressFilterLine(String n, int i, FilterMatch m) {
      _listName = n;
      _length = i;
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
