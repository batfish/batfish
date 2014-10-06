package batfish.grammar.juniper.policy_options;

public class POPSTTh_NextTermStanza extends POPST_ThenStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTTh_NextTermStanza() {
      set_postProcessTitle("Next Term");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/	
	public POPST_ThenType getType() {
		return POPST_ThenType.NEXT_TERM;
	}

}
