package batfish.grammar.juniper.policy_options;

import batfish.representation.juniper.FilterMatch;

public class POPSTTh_FilterMatchStanza extends POPST_ThenStanza {
   
   FilterMatch _filterMatch;
   
   /* ------------------------------ Constructor ----------------------------*/
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public void set_filterMatch (FilterMatch f) {
      _filterMatch = f;
   }
   public FilterMatch get_filterMatch () {
      return _filterMatch;
   }

   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_ThenType getType() {
		return POPST_ThenType.NEXT_TERM;
	}

}
