package batfish.grammar.juniper.policy_options;

public class CommunityDeleteThenTPSStanza extends ThenTPSStanza {
   private String _listName;

   public CommunityDeleteThenTPSStanza(String l) {
      _listName = l;
   }

   public String getListName() {
      return _listName;
   }

   @Override
   public ThenType getType() {
      return ThenType.COMMUNITY_DELETE;
   }

}
