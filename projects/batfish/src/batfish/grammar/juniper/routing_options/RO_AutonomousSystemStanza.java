package batfish.grammar.juniper.routing_options;

public class RO_AutonomousSystemStanza extends ROStanza {
   
	private int _asNum;

   /* ------------------------------ Constructor ----------------------------*/
   public RO_AutonomousSystemStanza(int as) {
      _asNum = as;
      set_postProcessTitle("AS " + _asNum);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/

   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_asNum () {
      return _asNum;
   }

   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public ROType getType() {
		return ROType.AS;
	}

}
