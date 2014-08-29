package batfish.representation.juniper;

import batfish.util.SubRange;

public class RouteFilterSubRangeLine extends RouteFilterLine {
   //IP Prefix
   private static final long serialVersionUID = 1L;
   
   private String _prefix;
   
   private int _prefixLength;
   
   private SubRange _lengthRange;

   public RouteFilterSubRangeLine(String prefix, int prefixLength, SubRange lengthRange) {
      _prefix = prefix;
      _prefixLength = prefixLength;
      _lengthRange = lengthRange;
   }
   
   public String getPrefix() {
      return _prefix;
   }

   public int getPrefixLength() {
      return _prefixLength;
   }

   public SubRange getLengthRange() {
      return _lengthRange;
   }

   @Override
   public RouteFilterLineType getType() {
      return RouteFilterLineType.SUBRANGE;
   }

}
