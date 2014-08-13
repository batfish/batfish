package batfish.representation.juniper;

public class FilterMatch_UpTo extends FilterMatch {

   private int _endLength;
   
   /* ------------------------------ Constructor ----------------------------*/
   public FilterMatch_UpTo(int l) {
      _endLength = l;      
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_endLength() {
      return _endLength;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public FilterMatchType getType() {
      return FilterMatchType.UPTO;
   }

}
