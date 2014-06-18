package batfish.grammar.juniper.policy_options;

public class ASPathFromTPSStanza extends FromTPSStanza {
   private String _listName;

   public ASPathFromTPSStanza(String l) {
      _listName = l;
   }

   public String getASPathName() {
      return _listName;
   }

   @Override
   public FromType getType() {
      return FromType.AS_PATH;
   }

}
