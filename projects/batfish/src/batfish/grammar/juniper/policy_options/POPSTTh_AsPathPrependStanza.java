package batfish.grammar.juniper.policy_options;

public class POPSTTh_AsPathPrependStanza extends POPST_ThenStanza {
   
   private int _asNum;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTTh_AsPathPrependStanza (int i) {
      _asNum = i;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_asNum () {
      return _asNum;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_ThenType getType() {
		return POPST_ThenType.AS_PATH_PREPEND;
	}

}
