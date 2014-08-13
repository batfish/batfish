package batfish.grammar.juniper.policy_options;

public class POPSTTh_LocalPreferenceStanza extends POPST_ThenStanza {
   
	private int _localPref;
	POPSTTh_LocalPreferenceType _lpType;
   
   /* ------------------------------ Constructor ----------------------------*/
	public POPSTTh_LocalPreferenceStanza() {
      _lpType = POPSTTh_LocalPreferenceType.LP_SET;
   }
	
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
	public void set_localPref(int lp) {
      _localPref = lp;   
   }
   public void set_lpType(POPSTTh_LocalPreferenceType l) {
      _lpType = l;   
   }
   public int get_localPref() {
      return _localPref;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_ThenType getType() {
		return POPST_ThenType.LOCAL_PREF;
	}

}
