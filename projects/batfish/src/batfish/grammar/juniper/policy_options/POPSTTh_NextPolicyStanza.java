package batfish.grammar.juniper.policy_options;

public class POPSTTh_NextPolicyStanza extends POPST_ThenStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTTh_NextPolicyStanza() {
      set_postProcessTitle("Next Policy");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_ThenType getType() {
		return POPST_ThenType.NEXT_POLICY;
	}

}
