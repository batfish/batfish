package batfish.representation.juniper;

public class FilterMatch_AddressMask extends FilterMatch {
   
   private String _ipAddress;
   
   /* ------------------------------ Constructor ----------------------------*/
   public FilterMatch_AddressMask (String i) {
      _ipAddress = i;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_ipAddress() {
      return _ipAddress;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public FilterMatchType getType() {
		return FilterMatchType.ADDRESS_MASK;
	}

}
