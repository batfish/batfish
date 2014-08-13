package batfish.representation.juniper;

public class FilterMatch_PrefixLengthRange extends FilterMatch {
   
   private int _beginLength;
   private int _endLength;
   
   /* ------------------------------ Constructor ----------------------------*/
   public FilterMatch_PrefixLengthRange(int l1, int l2) {
      _beginLength = l1;
      _endLength = l2;      
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_beginLength() {
      return _beginLength;
   }
   public int get_endLength() {
      return _endLength;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public FilterMatchType getType() {
      return FilterMatchType.PREFIX_LENGTH_RANGE;
   }

}
