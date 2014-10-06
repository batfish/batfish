package batfish.grammar.juniper.policy_options;

public class POPSTTh_AcceptStanza extends POPST_ThenStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTTh_AcceptStanza () {
      set_postProcessTitle("Accept");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_ThenType getType() {
		return POPST_ThenType.ACCEPT;
	}

}
