package org.batfish.representation;

public class RouteFilterThroughLine extends RouteFilterLine {

   private static final long serialVersionUID = 1L;

   // IP Prefix
   private Ip _prefix;

   // Range for prefix-length to be matched
   private int _prefixLength;

   // Another IP Prefix if the match type includes a range of prefix
   private Ip _secondPrefix;

   private int _secondPrefixLength;

   public RouteFilterThroughLine(LineAction action, Ip prefix,
         int prefixLength, Ip secondPrefix, int secondPrefixLength) {
      super(action);
      _prefix = prefix;
      _prefixLength = prefixLength;
      _secondPrefix = secondPrefix;
      _secondPrefixLength = secondPrefixLength;
   }

   public Ip getPrefix() {
      return _prefix;
   }

   public int getPrefixLength() {
      return _prefixLength;
   }

   public Ip getSecondPrefix() {
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
