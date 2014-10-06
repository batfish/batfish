package batfish.representation.juniper;

public class PolicyStatementMatchRouteFilterLine extends PolicyStatement_MatchLine {

   private static final long serialVersionUID = 1L;
   

   private String _prefix;
   private int _prefixLength;
   private FilterMatch _filterMatch;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementMatchRouteFilterLine(String n, int i, FilterMatch m) {
      _prefix = n;
      _prefixLength = i;
      _filterMatch = m;
      // TODO [P0]: apply filter match
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_prefix() {
      return _prefix;
   }
   public int get_prefixLength (){ 
      return _prefixLength;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public PolicyStatement_MatchType getType() {
      return PolicyStatement_MatchType.PREFIX_LIST_FILTER;
   }   
}
