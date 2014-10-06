package batfish.grammar.juniper.policy_options;

public class POPSTTh_InstallNextHopStanza extends POPST_ThenStanza {
   
	private String _hopName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTTh_InstallNextHopStanza(String s) {
      _hopName = s; 
      set_postProcessTitle("Install Next Hop " + _hopName);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_hopName () {
      return _hopName;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_ThenType getType() {
		return POPST_ThenType.INSTALL_NEXT_HOP;
	}

}
