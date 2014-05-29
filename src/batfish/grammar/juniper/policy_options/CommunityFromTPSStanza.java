package batfish.grammar.juniper.policy_options;

public class CommunityFromTPSStanza extends FromTPSStanza {
   private String _listName;

   public CommunityFromTPSStanza(String l) {
      _listName = l;
   }

   public String getListName() {
      return _listName;
   }

   @Override
   public FromType getType() {
      return FromType.COMMUNITY;
   }

}
