package batfish.grammar.juniper.routing_options;

public class RO_RouterIDStanza extends ROStanza {
   
	private String _routerID;

   /* ------------------------------ Constructor ----------------------------*/
   public RO_RouterIDStanza(String id) {
      _routerID = id;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/

   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_routerID () {
      return _routerID;
   }

   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public ROType getType() {
		return ROType.ROUTER_ID;
	}
}
