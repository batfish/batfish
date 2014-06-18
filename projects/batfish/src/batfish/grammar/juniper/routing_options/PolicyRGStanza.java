package batfish.grammar.juniper.routing_options;

public class PolicyRGStanza extends RGStanza {
   private String _listName;

   public PolicyRGStanza(String l) {
      _listName = l;
   }

   public String getListName() {
      return _listName;
   }

   @Override
   public RGType getType() {
      return RGType.POLICY;
   }

}
