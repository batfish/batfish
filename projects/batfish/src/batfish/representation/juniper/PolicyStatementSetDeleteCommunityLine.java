package batfish.representation.juniper;

public class PolicyStatementSetDeleteCommunityLine extends PolicyStatementSetLine {

   private static final long serialVersionUID = 1L;
   
   private String _listName;

   public PolicyStatementSetDeleteCommunityLine(String listName) {
      _listName = listName;
   }

   @Override
   public SetType getSetType() {
      return SetType.DELETE_COMMUNITY;
   }

   public String getListName() {
      return _listName;
   }
}
