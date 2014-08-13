package batfish.representation.juniper;

public class FilterMatch_Through extends FilterMatch {

   private String _ipAddress;
   private int _ipLength;
   
   /* ------------------------------ Constructor ----------------------------*/
   public FilterMatch_Through (String i, int l) {
      _ipAddress = i;
      _ipLength = l;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_ipLength() {
      return _ipLength;
   }
   public String get_ipAddress() {
      return _ipAddress;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public FilterMatchType getType() {
      return FilterMatchType.THROUGH;
   }

}
