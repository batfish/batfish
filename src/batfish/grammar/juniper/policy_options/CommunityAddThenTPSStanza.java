package batfish.grammar.juniper.policy_options;

public class CommunityAddThenTPSStanza extends ThenTPSStanza {
   private String _listName;

   public CommunityAddThenTPSStanza(String l) {
      _listName = l;
   }

   public String getListName() {
      return _listName;
   }

   @Override
   public ThenType getType() {
      return ThenType.COMMUNITY_ADD;
   }

}
