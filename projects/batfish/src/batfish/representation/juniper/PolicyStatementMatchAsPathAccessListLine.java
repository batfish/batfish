package batfish.representation.juniper;

public class PolicyStatementMatchAsPathAccessListLine extends PolicyStatementMatchLine {

   private static final long serialVersionUID = 1L;
   
   private String _listName;

   public PolicyStatementMatchAsPathAccessListLine(String listName) {
      _listName = listName;
   }

   @Override
   public MatchType getType() {
      return MatchType.AS_PATH_ACCESS_LIST;
   }

   public String getListName() {
      return _listName;
   }
   
}
