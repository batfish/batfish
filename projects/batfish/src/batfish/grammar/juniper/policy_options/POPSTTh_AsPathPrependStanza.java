package batfish.grammar.juniper.policy_options;

public class POPSTTh_AsPathPrependStanza extends POPST_ThenStanza {
   
   private String _asNumToPrepend;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTTh_AsPathPrependStanza (String s) {
      _asNumToPrepend = s;
      set_postProcessTitle("AS Path Prepend " + _asNumToPrepend);
   }
   public POPSTTh_AsPathPrependStanza (int i) {
      _asNumToPrepend = Integer.toString(i);
      set_postProcessTitle("AS Path Prepend " + _asNumToPrepend);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_asNumToPrepend () {
      return _asNumToPrepend;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_ThenType getType() {
		return POPST_ThenType.AS_PATH_PREPEND;
	}

}
