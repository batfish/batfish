package batfish.grammar.juniper.routing_options;

public class ROST_RibGroupStanza extends RO_STStanza {
   
   private String _groupName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public ROST_RibGroupStanza (String g) {
      _groupName = g;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_groupName () {
      return _groupName;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/ 
   @Override
   public RO_STType getType() {
      return RO_STType.RIB_GROUP;
   }
}
