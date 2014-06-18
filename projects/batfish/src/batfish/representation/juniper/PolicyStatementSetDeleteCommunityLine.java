package batfish.representation.juniper;

public class PolicyStatementSetDeleteCommunityLine extends PolicyStatementSetLine {

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
