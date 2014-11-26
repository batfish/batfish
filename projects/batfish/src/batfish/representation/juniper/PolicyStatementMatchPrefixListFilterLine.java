package batfish.representation.juniper;

public class PolicyStatementMatchPrefixListFilterLine extends PolicyStatement_MatchLine {

   private static final long serialVersionUID = 1L;


   private FilterMatch _filterMatch;
   private String _listName;

   public PolicyStatementMatchPrefixListFilterLine(String n, FilterMatch m) {
      _listName = n;
      _filterMatch = m;
      // TODO [P0]: apply filter match
   }

   public String get_listName() {
      return _listName;
   }

   public FilterMatch getFilterMatch() {
      return _filterMatch;
   }

   @Override
   public PolicyStatement_MatchType getType() {
      return PolicyStatement_MatchType.PREFIX_LIST_FILTER;
   }
}
