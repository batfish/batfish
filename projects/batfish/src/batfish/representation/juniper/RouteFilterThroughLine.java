package batfish.representation.juniper;

public class RouteFilterThroughLine extends RouteFilterLine {
   //IP Prefix
   private String _prefix;
   
   //Range for prefix-length to be matched
   private int _prefixLength;
   
   //Another IP Prefix if the match type includes a range of prefix
   private String _secondPrefix;

   private int _secondPrefixLength;

   public RouteFilterThroughLine(String prefix, int prefixLength, String secondPrefix, int secondPrefixLength) {
      _prefix = prefix;
      _prefixLength = prefixLength;
      _secondPrefix = secondPrefix;
      _secondPrefixLength = secondPrefixLength;
   }
   
   public String getPrefix() {
      return _prefix;
   }

   public int getPrefixLength() {
      return _prefixLength;
   }

   public String getSecondPrefix() {
      return _secondPrefix;
   }

   public int getSecondPrefixLength() {
      return _secondPrefixLength;
   }

   @Override
   public RouteFilterLineType getType() {
      return RouteFilterLineType.THROUGH;
   }


}
