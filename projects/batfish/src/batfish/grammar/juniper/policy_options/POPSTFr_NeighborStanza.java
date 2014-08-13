package batfish.grammar.juniper.policy_options;

public class POPSTFr_NeighborStanza extends POPST_FromStanza {
   
	private String _ip;
   
   /* ------------------------------ Constructor ----------------------------*/
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
	public void set_ip(String i) {
      _ip = i;
   }
   public String get_ip() {
      return _ip;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_FromType getType() {
		return POPST_FromType.NEIGHBOR;
	}

}
