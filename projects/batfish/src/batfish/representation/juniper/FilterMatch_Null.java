package batfish.representation.juniper;

public class FilterMatch_Null extends FilterMatch {
   
   FilterMatchType _subType;                              // possibilities: EXACT
   
   /* ------------------------------ Constructor ----------------------------*/
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/

	public FilterMatch_Null(FilterMatchType t) {
	   _subType = t;
	}

	@Override
	public FilterMatchType getType() {
		return _subType;
	}

}
