package batfish.representation;

import batfish.util.Util;

public class RouteFilterThroughLine extends RouteFilterLine {
   //IP Prefix
   private Ip _prefix;
   
   //Range for prefix-length to be matched
   private int _prefixLength;
   
   //Another IP Prefix if the match type includes a range of prefix
   private Ip _secondPrefix;

   private int _secondPrefixLength;

   public RouteFilterThroughLine(LineAction action, Ip prefix, int prefixLength, Ip secondPrefix, int secondPrefixLength) {
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

   @Override
   public boolean sameParseTree(RouteFilterLine line) {
      if(line.getType() != RouteFilterLineType.THROUGH){
         System.out.print("RouteFilterThroughLine:Type ");
         return false;
      }
      RouteFilterThroughLine rhs = (RouteFilterThroughLine) line;
      return (getAction() == rhs.getAction()) && (_prefix.equals(rhs._prefix)) && (_prefixLength == rhs._prefixLength) && (_secondPrefix.equals(rhs._secondPrefix)) && (_secondPrefixLength == rhs._secondPrefixLength);

   }


   @Override 
   public String getIFString(int indentLevel) {
	   String retString = Util.getIndentString(indentLevel) + String.format("RouteFilterThroughLine Prefix %s PrefixLength %s SecondPrefix %s SecondPrefixLength %s Action %s", 
			   																_prefix, _prefixLength, _secondPrefix, _secondPrefixLength, getAction()); 
	   return retString;
   }

}
