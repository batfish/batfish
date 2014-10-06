package batfish.grammar.juniper.policy_options;

public class POPSTTh_RejectStanza extends POPST_ThenStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTTh_RejectStanza () {
      set_postProcessTitle("Reject");
   }
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_ThenType getType() {
		return POPST_ThenType.REJECT;
	}

}
