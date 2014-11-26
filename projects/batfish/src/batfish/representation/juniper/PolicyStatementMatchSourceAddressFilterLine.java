package batfish.representation.juniper;

public class PolicyStatementMatchSourceAddressFilterLine extends
      PolicyStatement_MatchLine {

   private static final long serialVersionUID = 1L;

   private FilterMatch _filterMatch;
   private int _length;
   private String _listName;

   public PolicyStatementMatchSourceAddressFilterLine(String n, int i,
         FilterMatch m) {
      _listName = n;
      _length = i;
      _filterMatch = m;
      // TODO [P0]: apply filter match
   }

   public String get_listName() {
      return _listName;
   }

   public FilterMatch getFilterMatch() {
      return _filterMatch;
   }

   public int getLength() {
      return _length;
   }

   @Override
   public PolicyStatement_MatchType getType() {
      return PolicyStatement_MatchType.PREFIX_LIST_FILTER;
   }

}
